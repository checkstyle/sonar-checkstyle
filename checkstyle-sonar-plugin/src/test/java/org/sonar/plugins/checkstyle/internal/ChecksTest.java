/*
 * SonarQube Checkstyle Plugin
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.checkstyle.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;

public final class ChecksTest {
    private static final String RULES_PATH =
            "src/main/resources/org/sonar/plugins/checkstyle/rules.xml";
    private static final String MODULE_PROPERTIES_PATH =
            "src/main/resources/org/sonar/l10n/checkstyle.properties";

    private static final Set<String> CHECK_PROPERTIES = getProperties(AbstractCheck.class);
    private static final Set<String> JAVADOC_CHECK_PROPERTIES =
            getProperties(AbstractJavadocCheck.class);
    private static final Set<String> FILESET_PROPERTIES = getProperties(AbstractFileSetCheck.class);

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

    @Test
    public void verifyTestConfigurationFiles() throws Exception {
        final Set<Class<?>> modules = CheckUtil.getCheckstyleModules();

        Assert.assertTrue("no modules", !modules.isEmpty());

        validateSonarRules(new HashSet<>(modules));
        validateSonarProperties(new HashSet<>(modules));
    }

    private static void validateSonarRules(Set<Class<?>> modules)
            throws ParserConfigurationException, IOException {
        final File rulesFile = new File(RULES_PATH);

        Assert.assertTrue("'rules.xml' must exist", rulesFile.exists());

        final String input = new String(Files.readAllBytes(rulesFile.toPath()), UTF_8);

        final Document document = XmlUtil.getRawXml(rulesFile.getAbsolutePath(), input, input);

        validateSonarRules(document, modules);
    }

    private static void validateSonarRules(Document document, Set<Class<?>> modules)
            {
        final NodeList rules = document.getElementsByTagName("rule");

        for (int position = 0; position < rules.getLength(); position++) {
            final Node rule = rules.item(position);
            final Set<Node> children = XmlUtil.getChildrenElements(rule);

            final String key = rule.getAttributes().getNamedItem("key").getTextContent();

            final Class<?> module = findModule(modules, key);

            Assert.assertNotNull("Unknown class found in sonar rules: " + key, module);

            if (CheckUtil.isFilterModule(module)) {
                Assert.fail("Module should not be in sonar rules: " + module.getCanonicalName());
            }

            modules.remove(module);

            final String moduleName = module.getName();
            final String moduleSimpleName = module.getSimpleName();
            final Node name = XmlUtil.findElementByTag(children, "name");

            Assert.assertNotNull(moduleName + " requires a name in sonar rules", name);
            Assert.assertFalse(moduleName + " requires a name in sonar rules", name
                    .getTextContent().isEmpty());

            final Node configKey = XmlUtil.findElementByTag(children, "configKey");
            final String expectedConfigKey;

            if (AbstractCheck.class.isAssignableFrom(module)) {
                expectedConfigKey = "Checker/TreeWalker/"
                        + moduleSimpleName.replaceAll("Check$", "");
            }
            else {
                expectedConfigKey = "Checker/" + moduleSimpleName.replaceAll("Check$", "");
            }

            Assert.assertNotNull(moduleName
                    + " requires a configKey in sonar rules", configKey);
            Assert.assertEquals(moduleName + " requires a valid configKey in sonar rules",
                    expectedConfigKey, configKey.getTextContent());

            validateSonarRuleProperties(module, XmlUtil.findElementsByTag(children, "param"));
        }

        for (Class<?> module : modules) {
            if (!CheckUtil.isFilterModule(module) && module != TreeWalker.class) {
                Assert.fail("Module not found in sonar rules: " + module.getCanonicalName());
            }
        }
    }

    private static void validateSonarRuleProperties(Class<?> module, Set<Node> parameters)
            {
        final String moduleName = module.getName();
        final Set<String> properties = getFinalProperties(module);

        for (Node parameter : parameters) {
            final NamedNodeMap attributes = parameter.getAttributes();
            final Node paramKeyNode = attributes.getNamedItem("key");

            Assert.assertNotNull(moduleName
                    + " requires a key for unknown parameter in sonar rules", paramKeyNode);

            final String paramKey = paramKeyNode.getTextContent();

            Assert.assertFalse(moduleName
                    + " requires a valid key for unknown parameter in sonar rules",
                    paramKey.isEmpty());

            Assert.assertTrue(moduleName + " has an unknown parameter in sonar rules: "
                            + paramKey, properties.remove(paramKey));
        }

        for (String property : properties) {
            Assert.fail(moduleName + " parameter not found in sonar rules: " + property);
        }
    }

    private static void validateSonarProperties(Set<Class<?>> modules) throws IOException {
        final File propertiesFile = new File(MODULE_PROPERTIES_PATH);

        Assert.assertTrue("'checkstyle.properties' must exist", propertiesFile.exists());

        final Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));

        validateSonarProperties(properties, modules);
    }

    private static void validateSonarProperties(Properties properties, Set<Class<?>> modules)
            {
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
                            + keyName, keyValue.matches(".*[^'](\\{|\\})[^'].*"));

            final String moduleName;

            if (keyName.endsWith(".name")) {
                moduleName = keyName.substring(16, keyName.length() - 5);
            }
            else {
                moduleName = keyName.substring(16, keyName.indexOf(".param."));
            }

            final Class<?> module = findModule(modules, moduleName);

            Assert.assertNotNull("Unknown class found in sonar properties: " + moduleName,
                    module);

            if (CheckUtil.isFilterModule(module)) {
                Assert.fail("Module should not be in sonar properties: "
                        + module.getCanonicalName());
            }

            if (lastModule != module) {
                if (lastModule != null) {
                    modules.remove(lastModule);
                }
                if (moduleProperties != null) {
                    for (String property : moduleProperties) {
                        Assert.fail(lastModule.getCanonicalName()
                                + " property not found in sonar properties: " + property);
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
            if (!CheckUtil.isFilterModule(module) && module != TreeWalker.class) {
                Assert.fail("Module not found in sonar properties: " + module.getCanonicalName());
            }
        }
    }

    private static void validateSonarPropertyProperties(Class<?> module,
            Set<String> moduleProperties, String keyName) {
        final String moduleName = module.getName();
        final String propertyName = keyName.substring(keyName.indexOf(".param.") + 7);

        Assert.assertTrue(moduleName + " has an unknown property in sonar properties: "
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

        // remove undocumented properties
        new HashSet<>(properties).stream()
            .filter(p -> UNDOCUMENTED_PROPERTIES.contains(clss.getSimpleName() + "." + p))
            .forEach(properties::remove);

        if (AbstractCheck.class.isAssignableFrom(clss)) {
            final AbstractCheck check;
            try {
                check = (AbstractCheck) clss.getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
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

        return properties;
    }

    private static Set<String> getProperties(Class<?> clss) {
        final Set<String> result = new TreeSet<>();
        final PropertyDescriptor[] map = PropertyUtils.getPropertyDescriptors(clss);

        for (PropertyDescriptor p : map) {
            if (p.getWriteMethod() != null) {
                result.add(p.getName());
            }
        }

        return result;
    }
}
