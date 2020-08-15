////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2020 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package org.sonar.plugins.checkstyle.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.ModuleDetails;
import org.example.ModulePropertyDetails;
import org.example.ModuleType;
import org.example.XMLMetaReader;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.debt.internal.DefaultDebtRemediationFunction;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class CheckstyleMetadata {
    private static final String CHECK_STRING = "Check";
    private static final String OPTION_STRING = "Option";
    private static final String COMMA_STRING = ",";
    private static final int PARAM_TYPE_DB_COLUMN_TYPE_SIZE_LIMIT = 512;
    private final RulesDefinition.NewRepository repository;
    private final Map<String, ModuleDetails> metadataRepo;

    public CheckstyleMetadata(RulesDefinition.NewRepository repository) {
        this.repository = repository;
        metadataRepo = new HashMap<>();
        new XMLMetaReader().readAllModulesIncludingThirdPartyIfAny()
                .forEach(moduleDetails -> { //NOSONAR
                    metadataRepo.put(moduleDetails.getFullQualifiedName(),
                            moduleDetails);
                });
    }

    public void updateRulesWithMetadata() {
        repository.rules().forEach(rule -> {
            final ModuleDetails moduleDetails = metadataRepo.get(rule.key());
            if (moduleDetails != null) {
                rule.setHtmlDescription(moduleDetails.getDescription());
                rule.setName(convertName(moduleDetails.getName() + CHECK_STRING));
                rule.setInternalKey(convertInternalKey(moduleDetails));

                rule.params().forEach(param -> { //NOSONAR
                    if (!"tabWidth".equals(param.key())) {
                        constructParams(moduleDetails.getName(), param,
                                moduleDetails.getModulePropertyByKey(param.key()));
                    }
                    }
                );
            }
        });
    }

    public void createRulesWithMetadata() {
        final Set<String> existingChecks = repository.rules().stream()
                .map(RulesDefinition.NewRule::key)
                .collect(Collectors.toSet());

        final Map<String, SonarRulePropertyLoader.AdditionalRuleProperties> additionalRuleData =
                getAdditionalDetails("rules-meta.yml");
        final DebtRemediationFunction debtRemediationFunction =
                new DefaultDebtRemediationFunction(DebtRemediationFunction.Type.CONSTANT_ISSUE,
                        null, "0d 0h 5min");

        metadataRepo.keySet().stream()
                .filter(check -> { //NOSONAR
                    return !existingChecks.contains(check)
                            && metadataRepo.get(check).getModuleType() == ModuleType.CHECK
                            // these checks are not available in checkstyle 8.35, these conditions
                            // should be removed when upgrading to 8.36
                            && !check.contains("RecordTypeParameterNameCheck")
                            && !check.contains("PatternVariableNameCheck");
                })
                .forEach(newCheck -> {
                    final ModuleDetails moduleDetails = metadataRepo.get(newCheck);
                    if (moduleDetails != null) {
                        final SonarRulePropertyLoader.AdditionalRuleProperties additionalDetails =
                                additionalRuleData.get(newCheck);
                        final RulesDefinition.NewRule rule =
                                repository.createRule(moduleDetails.getFullQualifiedName());
                        rule.setHtmlDescription(moduleDetails.getDescription())
                                .setName(convertName(moduleDetails.getName() + CHECK_STRING))
                                .setInternalKey(convertInternalKey(moduleDetails))
                                .setDebtRemediationFunction(debtRemediationFunction)
                                .setSeverity("MINOR")
                                .setStatus(RuleStatus.READY);
                        final String tag = getRuleTag(moduleDetails.getFullQualifiedName(),
                                additionalDetails);
                        if (tag != null) {
                            rule.setTags(tag);
                        }
                        if (isTemplateRule(moduleDetails.getFullQualifiedName())) {
                            rule.setTemplate(true);
                        }

                        for (ModulePropertyDetails property : moduleDetails.getProperties()) {
                            constructParams(moduleDetails.getName(),
                                    rule.createParam(property.getName()),
                                    property);
                        }
                    }

                });
    }

    private Class<?> getClass(String checkName) {
        final ClassLoader loader = getClass().getClassLoader();
        try {
            return Class.forName(checkName, true, loader);
        }
        catch (ClassNotFoundException ex) {
            throw new IllegalStateException("exception occured during getClass for " + checkName,
                    ex);
        }
    }

    private boolean isTemplateRule(String checkName) {
        return Arrays.stream(getClass(checkName).getMethods())
                .anyMatch(CheckstyleMetadata::isSetter);
    }

    private List<String> getEnumValues(String checkName) {
        final Class<?> loadedClass = getClass(checkName);
        final Object[] vals = loadedClass.getEnumConstants();
        final List<String> enumVals = new ArrayList<>();
        for (Object val : vals) {
            enumVals.add(val.toString());
        }
        return enumVals;
    }

    private void constructParams(String checkName, RulesDefinition.NewParam param,
                                        ModulePropertyDetails modulePropertyDetails) {
        param.setDescription(modulePropertyDetails.getDescription())
                .setDefaultValue(modulePropertyDetails.getDefaultValue());
        String paramType = modulePropertyDetails.getType();
        if (modulePropertyDetails.getValidationType() != null
            && "tokenSet".equals(modulePropertyDetails.getValidationType())) {
            final Object[] valuesArray = CheckUtil.getAcceptableTokens(checkName)
                    .split(COMMA_STRING);
            final String[] valuesStringArray = Arrays.copyOf(valuesArray, valuesArray.length,
                    String[].class);

            int totalByteSize = 0;
            for (String x : valuesStringArray) {
                final String tokenString = x + COMMA_STRING;
                totalByteSize += tokenString.getBytes(StandardCharsets.UTF_8).length;
            }
            totalByteSize += "'SINGLE_SELECT_LIST,multiple=true,values=\""
                    .getBytes(StandardCharsets.UTF_8).length;
            // This check is required since the PARAM_TYPE column has size 512, and exceeding it
            // will result in an error in DB updates
            if (totalByteSize > PARAM_TYPE_DB_COLUMN_TYPE_SIZE_LIMIT) {
                param.setType(RuleParamType.STRING);
            }
            else {
                param.setType(RuleParamType.multipleListOfValues(valuesStringArray));
            }
        }
        else if (paramType.endsWith(OPTION_STRING)) {
            // the enum class names have been updated in later releases.
            // this condition should be removed when upgraded to 8.35
            if (paramType.contains("AnnotationUseStyleCheck")) {
                paramType = paramType.substring(0, paramType.length() - OPTION_STRING.length());
            }
            final Object[] valuesArray = getEnumValues(paramType).toArray();
            param.setType(RuleParamType.singleListOfValues(Arrays.copyOf(
                    valuesArray, valuesArray.length, String[].class)));
        }
        else {
            param.setType(RuleParamType.STRING);
        }
    }

    private static String getRuleTag(String checkPackage,
                              SonarRulePropertyLoader.AdditionalRuleProperties configData) {
        String result = null;
        if (configData == null || configData.getTag() == null) {
            final String[] packageTokens = checkPackage.split("\\.");
            if ("puppycrawl".equals(packageTokens[1])) {
                final String temp = packageTokens[packageTokens.length - 2];
                if ("checks".equals(temp)) {
                    result = "misc";
                }
                else {
                    result = temp;
                }
            }
        }
        else {
            result = configData.getTag();
        }
        return result;
    }

    private static Map<String, SonarRulePropertyLoader.AdditionalRuleProperties>
        getAdditionalDetails(String fileName) {

        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        final SonarRulePropertyLoader sonarRulePropertyLoader;
        try (InputStream inputStream = CheckstyleMetadata.class.getResourceAsStream(fileName)) {
            sonarRulePropertyLoader = mapper.readValue(inputStream, SonarRulePropertyLoader.class);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Exception during yaml loading of " + fileName, ex);
        }
        final Map<String, SonarRulePropertyLoader.AdditionalRuleProperties> additionalDetails =
                new HashMap<>();
        for (SonarRulePropertyLoader.AdditionalRuleProperties additionalRuleProperties
                : sonarRulePropertyLoader.getRules()) {
            additionalDetails.put(additionalRuleProperties.getRule(), additionalRuleProperties);
        }
        return additionalDetails;
    }

    public static boolean isSetter(Method method) {
        return method.getName().startsWith("set")
                && method.getParameterTypes().length == 1;
    }

    /**
     * It converts the name received from ModuleDetails to Sonar rule name format
     * e.g. RightCurlyCheck -> Right Curly Check
     *
     * @param name the name fetched from ModuleDetails
     * @return modifiedName
     */
    public static String convertName(String name) {
        final int capacity = 1024;
        final StringBuilder result = new StringBuilder(capacity);
        for (int i = 0; i < name.length(); i++) {
            result.append(name.charAt(i));
            if (i + 1 < name.length() && Character.isUpperCase(name.charAt(i + 1))) {
                result.append(' ');
            }
        }
        return result.toString();
    }

    public static String convertInternalKey(ModuleDetails moduleDetails) {
        String result = "Checker/";
        if ("com.puppycrawl.tools.checkstyle.Checker".equals(moduleDetails.getParent())) {
            result += moduleDetails.getName();
        }
        else {
            result += "TreeWalker/" + moduleDetails.getName();
        }
        if (moduleDetails.getModuleType() == ModuleType.CHECK) {
            result += CHECK_STRING;
        }
        return result;
    }
}
