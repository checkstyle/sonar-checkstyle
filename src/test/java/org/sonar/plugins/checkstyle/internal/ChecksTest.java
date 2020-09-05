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

package org.sonar.plugins.checkstyle.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.PropertyUtils;
import org.fest.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.checks.indentation.IndentationCheck;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck;
import com.puppycrawl.tools.checkstyle.meta.JavadocMetadataScraper;

public class ChecksTest {
    private static final String RULES_PATH =
            "src/main/resources/org/sonar/plugins/checkstyle/rules.xml";
    private static final String MODULE_PROPERTIES_PATH =
            "src/main/resources/org/sonar/l10n/checkstyle.properties";

    private static final Set<String> CHECK_PROPERTIES = getProperties(AbstractCheck.class);
    private static final Set<String> JAVADOC_CHECK_PROPERTIES =
            getProperties(AbstractJavadocCheck.class);
    private static final Set<String> FILESET_PROPERTIES = getProperties(AbstractFileSetCheck.class);

    private static final Set<Class<?>> UNDOCUMENTED_MODULES = Collections.set(
            TreeWalker.class,
            Checker.class,
            JavadocMetadataScraper.class
    );

    private static final List<String> UNDOCUMENTED_PROPERTIES = Arrays.asList(
            "Checker.classLoader",
            "Checker.classloader",
            "Checker.moduleClassLoader",
            "Checker.moduleFactory",
            "TreeWalker.classLoader",
            "TreeWalker.moduleFactory",
            "TreeWalker.cacheFile",
            "TreeWalker.upChild",
            "SuppressWithNearbyCommentFilter.fileContents",
            "SuppressionCommentFilter.fileContents"
    );

    /**
     * From Checkstyle 8.36 onwards metadata would be fetched directly from checkstyle and won't
     * be manually edited any more in org/sonar/plugins/checkstyle/rules.xml.
     * So, these are modules which were not updated in 8.36, and hence data is consistent with
     * the XML file.
     */
    private static final Set<String> PRE_CHECKSTYLE_8_36_MODULES =
            java.util.Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "com.puppycrawl.tools.checkstyle.checks.header.HeaderCheck",
        "com.puppycrawl.tools.checkstyle.checks.header.RegexpHeaderCheck",
        "com.puppycrawl.tools.checkstyle.checks.annotation.AnnotationUseStyleCheck",
        "com.puppycrawl.tools.checkstyle.checks.annotation.MissingDeprecatedCheck",
        "com.puppycrawl.tools.checkstyle.checks.annotation.MissingOverrideCheck",
        "com.puppycrawl.tools.checkstyle.checks.annotation.PackageAnnotationCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.EqualsAvoidNullCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NoCloneCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NoEnumTrailingCommaCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NoFinalizerCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.AvoidStaticImportCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.MissingJavadocPackageCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.InvalidJavadocPositionCheck",
        "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck",
        "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpOnFilenameCheck",
        "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck",
        "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineJavaCheck",
        "com.puppycrawl.tools.checkstyle.checks.sizes.OuterTypeNumberCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.FileTabCharacterCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.GenericWhitespaceCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.AbstractClassNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.sizes.AnonInnerLengthCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.ArrayTrailingCommaCheck",
        "com.puppycrawl.tools.checkstyle.checks.ArrayTypeStyleCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.AvoidInlineConditionalsCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.AvoidNoArgumentSuperConstructorCallCheck",
        "com.puppycrawl.tools.checkstyle.checks.blocks.AvoidNestedBlocksCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.AvoidStarImportCheck",
        "com.puppycrawl.tools.checkstyle.checks.metrics.BooleanExpressionComplexityCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.CatchParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.metrics.ClassDataAbstractionCouplingCheck",
        "com.puppycrawl.tools.checkstyle.checks.metrics.ClassFanOutComplexityCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.ConstantNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.CovariantEqualsCheck",
        "com.puppycrawl.tools.checkstyle.checks.metrics.CyclomaticComplexityCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.DeclarationOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.DefaultComesLastCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.DesignForExtensionCheck",
        "com.puppycrawl.tools.checkstyle.checks.blocks.EmptyBlockCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.EmptyForInitializerPadCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.EmptyForIteratorPadCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.EmptyStatementCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.EqualsHashCodeCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.ExplicitInitializationCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.FallThroughCheck",
        "com.puppycrawl.tools.checkstyle.checks.sizes.FileLengthCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.FinalClassCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.FinalLocalVariableCheck",
        "com.puppycrawl.tools.checkstyle.checks.FinalParametersCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.HideUtilityClassConstructorCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.IllegalCatchCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.IllegalImportCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.IllegalInstantiationCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.IllegalThrowsCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.IllegalTokenCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.ImportOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.indentation.IndentationCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.InnerAssignmentCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.InterfaceIsTypeCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocMissingWhitespaceAfterAsteriskCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocBlockTagLocationCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocContentLocationCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocVariableCheck",
        "com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.LocalFinalVariableNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.LocalVariableNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.MagicNumberCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.MemberNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.MethodNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.MissingCtorCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.MissingSwitchDefaultCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.ModifiedControlVariableCheck",
        "com.puppycrawl.tools.checkstyle.checks.modifier.ModifierOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.MultipleStringLiteralsCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.MultipleVariableDeclarationsCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.MutableExceptionCheck",
        "com.puppycrawl.tools.checkstyle.checks.metrics.NPathComplexityCheck",
        "com.puppycrawl.tools.checkstyle.checks.blocks.NeedBracesCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NestedIfDepthCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NestedTryDepthCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NoArrayTrailingCommaCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.AvoidDoubleBraceInitializationCheck",
        "com.puppycrawl.tools.checkstyle.checks.NewlineAtEndOfFileCheck",
        "com.puppycrawl.tools.checkstyle.checks.NoCodeInFileCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.NoWhitespaceAfterCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.NoWhitespaceBeforeCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.OperatorWrapCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.PackageDeclarationCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.PackageNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.ParameterAssignmentCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.ParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.LambdaParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.sizes.ParameterNumberCheck",
        "com.puppycrawl.tools.checkstyle.checks.modifier.RedundantModifierCheck",
        "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.RequireThisCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.ReturnCountCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanReturnCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.StaticVariableNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.StringLiteralEqualityCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.SuperCloneCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.SuperFinalizeCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.ThrowsCountCheck",
        "com.puppycrawl.tools.checkstyle.checks.TodoCommentCheck",
        "com.puppycrawl.tools.checkstyle.checks.TrailingCommentCheck",
        "com.puppycrawl.tools.checkstyle.checks.TranslationCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.SingleSpaceSeparatorCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.TypecastParenPadCheck",
        "com.puppycrawl.tools.checkstyle.checks.UncommentedMainCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck",
        "com.puppycrawl.tools.checkstyle.checks.UpperEllCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.VisibilityModifierCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAfterCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAroundCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.InnerTypeLastCheck",
        "com.puppycrawl.tools.checkstyle.checks.OuterTypeFilenameCheck",
        "com.puppycrawl.tools.checkstyle.checks.OrderedPropertiesCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.NestedForDepthCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.OneStatementPerLineCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.ClassTypeParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.MethodTypeParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.UniquePropertiesCheck",
        "com.puppycrawl.tools.checkstyle.checks.AvoidEscapedUnicodeCharactersCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.CustomImportOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.naming.InterfaceTypeParameterNameCheck",
        "com.puppycrawl.tools.checkstyle.checks.design.OneTopLevelClassCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.OverloadMethodsDeclarationOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.VariableDeclarationUsageDistanceCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.UnnecessarySemicolonInTryWithResourcesCheck",
        "com.puppycrawl.tools.checkstyle.checks.coding.UnnecessarySemicolonInEnumerationCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.AtclauseOrderCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.NonEmptyAtclauseDescriptionCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocParagraphCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTagContinuationIndentationCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.SingleLineJavadocCheck",
        "com.puppycrawl.tools.checkstyle.checks.javadoc.SummaryJavadocCheck",
        "com.puppycrawl.tools.checkstyle.checks.blocks.EmptyCatchBlockCheck",
        "com.puppycrawl.tools.checkstyle.checks.imports.ImportControlCheck",
        "com.puppycrawl.tools.checkstyle.checks.indentation.CommentsIndentationCheck",
        "com.puppycrawl.tools.checkstyle.checks.whitespace.SeparatorWrapCheck",
        "com.puppycrawl.tools.checkstyle.checks.SuppressWarningsHolder"
        )));

    @Test
    public void verifyTestConfigurationFiles() throws Exception {
        final Set<Class<?>> modules = CheckUtil.getCheckstyleModules();

        Assert.assertFalse("no modules", modules.isEmpty());

        validateSonarRules(new HashSet<>(modules));
        validateSonarProperties(new HashSet<>(modules));
    }

    private static void validateSonarRules(Set<Class<?>> modules)
            throws ParserConfigurationException {
        final File rulesFile = new File(RULES_PATH);

        Assert.assertTrue(RULES_PATH + " must exist", rulesFile.exists());

        try {
            final String input = new String(Files.readAllBytes(rulesFile.toPath()), UTF_8);
            final Document document = XmlUtil.getRawXml(rulesFile.getAbsolutePath(), input, input);
            validateSonarRules(document, modules);
        }
        catch (IOException ignored) {
            Assert.fail("Failed to read rulesFile.");
        }
    }

    private static void validateSonarRules(Document document, Set<Class<?>> modules) {
        final NodeList rules = document.getElementsByTagName("rule");

        for (int position = 0; position < rules.getLength(); position++) {
            final Node rule = rules.item(position);
            final Set<Node> children = XmlUtil.getChildrenElements(rule);

            final String key = rule.getAttributes().getNamedItem("key").getTextContent();

            // if the module present in rules.xml was modified in 8.36 onwards, continue, as
            // rules.xml data was not updated, so asserts will fail.
            if (!PRE_CHECKSTYLE_8_36_MODULES.contains(key)) {
                continue;
            }

            final Class<?> module = findModule(modules, key);

            Assert.assertNotNull("Unknown class found in sonar rules"
                    + " (" + RULES_PATH + ")" + " :" + key, module);

            if (CheckUtil.isFilterModule(module)) {
                Assert.fail("Module should not be in sonar rules: " + module.getCanonicalName());
            }

            modules.remove(module);

            final String moduleName = module.getName();
            final String moduleSimpleName = module.getSimpleName();
            final Node name = XmlUtil.findElementByTag(children, "name");

            Assert.assertNotNull(moduleName + " requires a name in sonar rules"
                    + " (" + RULES_PATH + ")", name);
            Assert.assertFalse(moduleName + " requires a name in sonar rules"
                    + " (" + RULES_PATH + ")", name
                    .getTextContent().isEmpty());

            final Node configKey = XmlUtil.findElementByTag(children, "configKey");
            final String expectedConfigKey;

            if (CheckUtil.isCheckstyleCheck(module)) {
                expectedConfigKey = "Checker/TreeWalker/"
                        + moduleSimpleName.replaceAll("Check$", "");
            }
            else {
                expectedConfigKey = "Checker/" + moduleSimpleName.replaceAll("Check$", "");
            }

            Assert.assertNotNull(moduleName
                    + " requires a configKey in sonar rules"
                    + " (" + RULES_PATH + ")", configKey);
            Assert.assertEquals(moduleName + " requires a valid configKey in sonar rules"
                            + " (" + RULES_PATH + ")",
                    expectedConfigKey, configKey.getTextContent());

            validateSonarRuleProperties(module, XmlUtil.findElementsByTag(children, "param"));
        }

        for (Class<?> module : modules) {
            if (!UNDOCUMENTED_MODULES.contains(module) && !CheckUtil.isFilterModule(module)
                    && !CheckUtil.isFileFilterModule(module)
                    // Skip checking for newly introduced modules in checkstyle 8.36, as rules.xml
                    // was not updated.
                    && PRE_CHECKSTYLE_8_36_MODULES.contains(module.getName())) {
                Assert.fail("Module not found in sonar rules: " + module.getCanonicalName());
            }
        }
    }

    private static void validateSonarRuleProperties(Class<?> module, Set<Node> parameters) {
        final Object instance;

        try {
            instance = module.getConstructor().newInstance();
        }
        catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }

        final String moduleName = module.getName();
        final Set<String> properties = getFinalProperties(module);

        for (Node parameter : parameters) {
            final NamedNodeMap attributes = parameter.getAttributes();
            final Node paramKeyNode = attributes.getNamedItem("key");

            Assert.assertNotNull(moduleName
                    + " requires a key for unknown parameter in sonar rules"
                    + " (" + RULES_PATH + ")", paramKeyNode);

            final String paramKey = paramKeyNode.getTextContent();

            Assert.assertFalse(moduleName
                    + " requires a valid key for unknown parameter in sonar rules"
                            + " (" + RULES_PATH + ")",
                    paramKey.isEmpty());

            Assert.assertTrue(moduleName + " has an unknown parameter in sonar rules"
                    + " (" + RULES_PATH + ")" + ": " + paramKey, properties.remove(paramKey));

            final Node typeNode = parameter.getAttributes().getNamedItem("type");

            Assert.assertNotNull(moduleName + " has no parameter type in sonar rules"
                            + " (" + RULES_PATH + ")" + ": " + paramKey,
                    typeNode);

            if ("tokens".equals(paramKey) || "javadocTokens".equals(paramKey)) {
                String expectedTokenType;

                if ("tokens".equals(paramKey)) {
                    expectedTokenType = "s[" + CheckUtil.getTokenText(
                            ((AbstractCheck) instance).getAcceptableTokens(),
                            ((AbstractCheck) instance).getRequiredTokens()) + "]";
                }
                else {
                    expectedTokenType = "s[" + CheckUtil.getJavadocTokenText(
                            ((AbstractJavadocCheck) instance).getAcceptableJavadocTokens(),
                            ((AbstractJavadocCheck) instance).getRequiredJavadocTokens()) + "]";
                }

                // Type can't be too long as it is stored in a database field with a max limit
                // sonar adds its own text to the type affecting the limit of data we can store
                // see https://github.com/checkstyle/sonar-checkstyle/issues/75 and
                // https://github.com/checkstyle/sonar-checkstyle/pull/77#issuecomment-281247278
                if (expectedTokenType.length() + 44 > 512) {
                    expectedTokenType = "STRING";
                }

                final String type = typeNode.getTextContent();
                Assert.assertEquals(moduleName + " has the parameter '" + paramKey
                        + "' in sonar rules with the incorrect type", expectedTokenType, type);

                final Set<Node> values = XmlUtil.getChildrenElements(parameter);

                Assert.assertEquals(moduleName + " has the parameter '" + paramKey
                        + "' in sonar rules with no defaultValue", 1, values.size());

                final String expectedDefaultTokens;

                if ("tokens".equals(paramKey)) {
                    expectedDefaultTokens = CheckUtil.getTokenText(
                            ((AbstractCheck) instance).getDefaultTokens(),
                            ((AbstractCheck) instance).getRequiredTokens());
                }
                else {
                    expectedDefaultTokens = CheckUtil.getJavadocTokenText(
                            ((AbstractJavadocCheck) instance).getDefaultJavadocTokens(),
                            ((AbstractJavadocCheck) instance).getRequiredJavadocTokens());
                }

                Assert.assertEquals(moduleName + " has the parameter '" + paramKey
                        + "' in sonar rules with the incorrect defaultValue", expectedDefaultTokens,
                        values.iterator().next().getTextContent());
            }
        }

        for (String property : properties) {
            Assert.fail(moduleName + " parameter not found in sonar rules"
                    + " (" + RULES_PATH + ")" + ": " + property);
        }
    }

    private static void validateSonarProperties(Set<Class<?>> modules) throws IOException {
        final File propertiesFile = new File(MODULE_PROPERTIES_PATH);

        Assert.assertTrue("'checkstyle.properties' must exist", propertiesFile.exists());

        final Properties properties = new Properties();
        try (InputStream stream = new FileInputStream(propertiesFile)) {
            properties.load(stream);
        }

        validateSonarProperties(properties, modules);
    }

    private static void validateSonarProperties(Map<Object, Object> properties,
            Set<Class<?>> modules) {
        Class<?> lastModule = null;
        Set<String> moduleProperties = null;

        for (Object key : new TreeSet<>(properties.keySet())) {
            final String keyName = key.toString();

            Assert.assertTrue("sonar properties must start with 'rule.checkstyle.': "
                            + keyName, keyName.startsWith("rule.checkstyle."));

            final String keyValue = properties.get(keyName).toString();

            Assert.assertFalse("sonar properties value must not be empty: " + keyName,
                    keyValue.isEmpty());
            Assert.assertFalse("sonar properties value must not have single quote: "
                            + keyName, keyValue.matches(".*[^'{}]'[^'{}].*"));
            Assert.assertFalse("sonar properties value must not have unescaped braces: "
                            + keyName, keyValue.matches(".*[^']([{}])[^'].*"));

            final String moduleName;

            if (keyName.endsWith(".name")) {
                moduleName = keyName.substring(16, keyName.length() - 5);
            }
            else {
                moduleName = keyName.substring(16, keyName.indexOf(".param."));
            }

            final Class<?> module = findModule(modules, moduleName);

            Assert.assertNotNull("Unknown class found in sonar properties"
                    + " (" + MODULE_PROPERTIES_PATH + ")" + ": " + moduleName,
                    module);

            if (CheckUtil.isFilterModule(module)) {
                Assert.fail("Module should not be in sonar properties"
                        + " (" + MODULE_PROPERTIES_PATH + ")" + ": "
                        + module.getCanonicalName());
            }

            if (lastModule != module) {
                if (lastModule != null) {
                    modules.remove(lastModule);
                }
                if (moduleProperties != null) {
                    for (String property : moduleProperties) {
                        // If the module's property was modified in checkstyle 8.36, then skip as
                        // rules.xml was not updated
                        if (PRE_CHECKSTYLE_8_36_MODULES.contains(lastModule.getCanonicalName())) {
                            Assert.fail(lastModule.getCanonicalName()
                                    + " property not found in sonar properties"
                                    + " (" + MODULE_PROPERTIES_PATH + ")" + ": " + property);
                        }
                    }
                }

                moduleProperties = getFinalProperties(module);
            }
            lastModule = module;

            if (!keyName.endsWith(".name")) {
                validateSonarPropertyProperties(module, moduleProperties, keyName);
            }
        }

        if (lastModule != null) {
            modules.remove(lastModule);
        }

        for (Class<?> module : modules) {
            if (!UNDOCUMENTED_MODULES.contains(module) && !CheckUtil.isFilterModule(module)
                    && !CheckUtil.isFileFilterModule(module)
                    // Skip checking for newly introduced modules in checkstyle 8.36, as rules.xml
                    // was not updated.
                    && PRE_CHECKSTYLE_8_36_MODULES.contains(module.getName())) {
                Assert.fail("Module not found in sonar properties"
                        + " (" + MODULE_PROPERTIES_PATH + ")" + ": " + module.getCanonicalName());
            }
        }
    }

    private static void validateSonarPropertyProperties(Class<?> module,
            Set<String> moduleProperties, String keyName) {
        final String moduleName = module.getName();
        final String propertyName = keyName.substring(keyName.indexOf(".param.") + 7);

        Assert.assertTrue(moduleName + " has an unknown property in sonar properties"
                + " (" + MODULE_PROPERTIES_PATH + ")" + ": "
                + propertyName, moduleProperties.remove(propertyName));
    }

    private static Class<?> findModule(Set<Class<?>> modules, String classPath) {
        Class<?> result = null;

        for (Class<?> module : modules) {
            if (module.getCanonicalName().equals(classPath)) {
                result = module;
                break;
            }
        }

        return result;
    }

    private static Set<String> getFinalProperties(Class<?> clss) {
        final Set<String> properties = getProperties(clss);

        if (AbstractJavadocCheck.class.isAssignableFrom(clss)) {
            properties.removeAll(JAVADOC_CHECK_PROPERTIES);
        }
        else if (AbstractCheck.class.isAssignableFrom(clss)) {
            properties.removeAll(CHECK_PROPERTIES);
        }
        else if (AbstractFileSetCheck.class.isAssignableFrom(clss)) {
            properties.removeAll(FILESET_PROPERTIES);

            // override
            properties.add("fileExtensions");
        }

        // overrides
        if (LineLengthCheck.class.isAssignableFrom(clss)
                || IndentationCheck.class.isAssignableFrom(clss)) {
            // all checks have this property, but not many use it
            // until https://github.com/checkstyle/checkstyle/issues/4111
            properties.add("tabWidth");
        }

        // remove undocumented properties
        new HashSet<>(properties)
                .stream()
                .filter(property -> {
                    return UNDOCUMENTED_PROPERTIES
                            .contains(clss.getSimpleName() + "." + property);
                })
                .forEach(properties::remove);

        if (AbstractCheck.class.isAssignableFrom(clss)) {
            final AbstractCheck check;
            try {
                check = (AbstractCheck) clss.getConstructor().newInstance();
            }
            catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }

            final int[] acceptableTokens = check.getAcceptableTokens();
            Arrays.sort(acceptableTokens);
            final int[] defaultTokens = check.getDefaultTokens();
            Arrays.sort(defaultTokens);
            final int[] requiredTokens = check.getRequiredTokens();
            Arrays.sort(requiredTokens);

            if (!Arrays.equals(acceptableTokens, defaultTokens)
                    || !Arrays.equals(acceptableTokens, requiredTokens)) {
                properties.add("tokens");
            }
        }

        if (AbstractJavadocCheck.class.isAssignableFrom(clss)) {
            final AbstractJavadocCheck check;
            try {
                check = (AbstractJavadocCheck) clss.getConstructor().newInstance();
            }
            catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }

            final int[] acceptableJavadocTokens = check.getAcceptableJavadocTokens();
            Arrays.sort(acceptableJavadocTokens);
            final int[] defaultJavadocTokens = check.getDefaultJavadocTokens();
            Arrays.sort(defaultJavadocTokens);
            final int[] requiredJavadocTokens = check.getRequiredJavadocTokens();
            Arrays.sort(requiredJavadocTokens);

            if (!Arrays.equals(acceptableJavadocTokens, defaultJavadocTokens)
                    || !Arrays.equals(acceptableJavadocTokens, requiredJavadocTokens)) {
                properties.add("javadocTokens");
            }
        }

        return properties;
    }

    private static Set<String> getProperties(Class<?> clss) {
        final Set<String> result = new TreeSet<>();
        final PropertyDescriptor[] map = PropertyUtils.getPropertyDescriptors(clss);

        for (PropertyDescriptor p : map) {
            if (p.getWriteMethod() != null
                    && !p.getWriteMethod().isAnnotationPresent(Deprecated.class)) {
                result.add(p.getName());
            }
        }

        return result;
    }
}
