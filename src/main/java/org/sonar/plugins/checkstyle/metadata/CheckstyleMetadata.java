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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sonar.api.rule.RuleStatus;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.debt.internal.DefaultDebtRemediationFunction;
import org.sonar.api.server.rule.RuleParamType;
import org.sonar.api.server.rule.RulesDefinition;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.puppycrawl.tools.checkstyle.meta.ModuleDetails;
import com.puppycrawl.tools.checkstyle.meta.ModulePropertyDetails;
import com.puppycrawl.tools.checkstyle.meta.ModuleType;
import com.puppycrawl.tools.checkstyle.meta.XmlMetaReader;

public class CheckstyleMetadata {
    private static final List<String> NO_SQALE = ImmutableList.of(
            "com.puppycrawl.tools.checkstyle.checks.TranslationCheck",
            "com.puppycrawl.tools.checkstyle.checks.TodoCommentCheck",
            "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck",
            "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineJavaCheck",
            "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck",
            "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpOnFilenameCheck",
            "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpCheck",
            "com.puppycrawl.tools.checkstyle.checks.header.RegexpHeaderCheck",
            "com.puppycrawl.tools.checkstyle.checks.imports.ImportControlCheck",
            "com.puppycrawl.tools.checkstyle.checks.annotation.AnnotationLocationCheck",
            "com.puppycrawl.tools.checkstyle.checks.SuppressWarningsHolder"
    );

    private static final Map<String, String> MODULE_NAME_EXCEPTIONS = ImmutableMap.of(
            "JavaNCSS", "Java NCSS",
            "NPathComplexity", "NPath Complexity"
    );

    private static final String OPTION_STRING = "Option";
    private static final String COMMA_STRING = ",";
    private static final int PARAM_TYPE_DB_COLUMN_TYPE_SIZE_LIMIT = 512;

    private final RulesDefinition.NewRepository repository;
    private final Map<String, ModuleDetails> metadataRepo;

    public CheckstyleMetadata(RulesDefinition.NewRepository repository) {
        this.repository = repository;
        metadataRepo = new HashMap<>();
        XmlMetaReader.readAllModulesIncludingThirdPartyIfAny()
                .forEach(moduleDetails -> { // NOSONAR
                    metadataRepo.put(moduleDetails.getFullQualifiedName(),
                            moduleDetails);
                });
    }

    /**
     * Create checkstyle metadata for checks.
     */
    public void createRulesWithMetadata() {

        final Map<String, SonarRulePropertyLoader.AdditionalRuleProperties> additionalRuleData =
                getAdditionalDetails("rules-meta.yml");
        final DebtRemediationFunction debtRemediationFunction =
                new DefaultDebtRemediationFunction(DebtRemediationFunction.Type.CONSTANT_ISSUE,
                        null, "0d 0h 5min");

        metadataRepo.entrySet().stream()
                .filter(entry -> entry.getValue().getModuleType() == ModuleType.CHECK)
                .forEach(check -> {
                    final ModuleDetails moduleDetails = check.getValue();
                    final SonarRulePropertyLoader.AdditionalRuleProperties additionalDetails =
                            additionalRuleData.get(check.getKey());
                    final RulesDefinition.NewRule rule =
                            repository.createRule(moduleDetails.getFullQualifiedName());
                    rule.setHtmlDescription(moduleDetails.getDescription())
                            .setName(getFullCheckName(moduleDetails.getName()))
                            .setInternalKey(getInternalKey(moduleDetails))
                            .setSeverity("MINOR")
                            .setStatus(RuleStatus.READY);
                    if (!NO_SQALE.contains(rule.key())) {
                        rule.setDebtRemediationFunction(debtRemediationFunction);
                    }
                    final String tag = getRuleTag(moduleDetails.getFullQualifiedName(),
                            additionalDetails);
                    if (tag != null) {
                        rule.setTags(tag);
                    }
                    if (isTemplateRule(moduleDetails)) {
                        rule.setTemplate(true);
                    }

                    for (ModulePropertyDetails property : moduleDetails.getProperties()) {
                        constructParams(moduleDetails.getName(),
                                rule.createParam(property.getName()),
                                property);
                    }
                });
    }

    /**
     * Get class with the given check name.
     *
     * @param checkName check name
     * @return check class
     */
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

    /**
     * Determine whether the check is a template rule based on the number of the properties of
     * check.
     *
     * @param moduleDetails module
     * @return true if check is a template rule
     */
    private static boolean isTemplateRule(ModuleDetails moduleDetails) {
        return !moduleDetails.getProperties().isEmpty();
    }

    /**
     * Get enum values for the provided enum class name.
     *
     * @param enumName enum class name
     * @return enum values
     */
    private List<String> getEnumValues(String enumName) {
        final Class<?> loadedClass = getClass(enumName);
        final Object[] vals = loadedClass.getEnumConstants();
        final List<String> enumVals = new ArrayList<>();
        for (Object val : vals) {
            enumVals.add(val.toString());
            enumVals.add(val.toString().toLowerCase(Locale.ENGLISH));
        }
        return enumVals;
    }

    /**
     * Construct check parameter metadata.
     *
     * @param checkName check name
     * @param param parameter details fetched from sonar database
     * @param modulePropertyDetails constructed new parameter metadata
     */
    private void constructParams(String checkName, RulesDefinition.NewParam param,
                                        ModulePropertyDetails modulePropertyDetails) {
        param.setDescription(modulePropertyDetails.getDescription())
                .setDefaultValue(modulePropertyDetails.getDefaultValue());
        final String paramType = modulePropertyDetails.getType();
        if (modulePropertyDetails.getValidationType() != null
            && "tokenSet".equals(modulePropertyDetails.getValidationType())) {
            final Object[] valuesArray = CheckUtil.getModifiableTokens(checkName)
                    .split(COMMA_STRING);
            final String[] valuesStringArray = Arrays.copyOf(valuesArray, valuesArray.length,
                    String[].class);

            if (isMoreThanVarCharSizeLimit(valuesStringArray)) {
                param.setType(RuleParamType.STRING);
            }
            else {
                param.setType(RuleParamType.multipleListOfValues(valuesStringArray));
            }
        }
        else if (paramType.endsWith(OPTION_STRING)) {
            final Object[] valuesArray = getEnumValues(paramType).toArray();
            param.setType(RuleParamType.singleListOfValues(Arrays.copyOf(
                    valuesArray, valuesArray.length, String[].class)));
        }
        else if ("anyTokenTypesSet".equals(paramType)) {
            param.setType(RuleParamType.STRING);
        }
        else {
            param.setType(getPropertyType(modulePropertyDetails));
        }
    }

    /**
     * This check is required since the PARAM_TYPE column has size 512, and exceeding it
     * will result in an error in DB updates
     * @param values array of values
     * @return true if the size has exceeded the limit
     */
    private static boolean isMoreThanVarCharSizeLimit(String... values) {
        int totalByteSize = 0;
        for (String x : values) {
            final String tokenString = x + COMMA_STRING;
            totalByteSize += tokenString.getBytes(StandardCharsets.UTF_8).length;
        }
        totalByteSize += "'SINGLE_SELECT_LIST,multiple=true,values=\""
                .getBytes(StandardCharsets.UTF_8).length;
        boolean result = false;
        if (totalByteSize > PARAM_TYPE_DB_COLUMN_TYPE_SIZE_LIMIT) {
            result = true;
        }
        return result;
    }

    /**
     * Get Sonar specific property type from module property type.
     *
     * @param modulePropertyDetails module property details
     * @return sonar property type
     */
    private static RuleParamType getPropertyType(ModulePropertyDetails modulePropertyDetails) {
        final RuleParamType result;
        switch (modulePropertyDetails.getType()) {
            case "boolean":
                result = RuleParamType.BOOLEAN;
                break;
            case "int":
            case "long":
                result = RuleParamType.INTEGER;
                break;
            case "float":
            case "double":
                result = RuleParamType.FLOAT;
                break;
            default:
                result = RuleParamType.STRING;
        }
        return result;
    }

    /**
     * Get rule tags for checks, either based on package type or from the YML config(if provided).
     *
     * @param checkPackage check name
     * @param configData additional metadata from YML config
     * @return the determined rule tag
     */
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

    /**
     * Fetch additional module details from a YML config.
     *
     * @param fileName YML config file
     * @return map of additional metadata
     */
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

    /**
     * It converts the name received from ModuleDetails to Sonar rule name format
     * e.g. RightCurlyCheck -> Right Curly Check
     *
     * @param checkName the name fetched from ModuleDetails
     * @return modifiedName
     */
    public static String getFullCheckName(String checkName) {
        final int capacity = 1024;
        final StringBuilder result = new StringBuilder(capacity);

        if (MODULE_NAME_EXCEPTIONS.containsKey(checkName)) {
            result.append(MODULE_NAME_EXCEPTIONS.get(checkName));
        }
        else {
            for (int i = 0; i < checkName.length(); i++) {
                result.append(checkName.charAt(i));
                if (i + 1 < checkName.length() && Character.isUpperCase(checkName.charAt(i + 1))) {
                    result.append(' ');
                }
            }
        }
        return result.toString();
    }

    /**
     * Create internal key composed of parents of module.
     *
     * @param moduleDetails module metadata
     * @return internalKey
     */
    public static String getInternalKey(ModuleDetails moduleDetails) {
        String result = "Checker/";
        if ("com.puppycrawl.tools.checkstyle.Checker".equals(moduleDetails.getParent())) {
            result += moduleDetails.getName();
        }
        else {
            result += "TreeWalker/" + moduleDetails.getName();
        }
        return result;
    }
}
