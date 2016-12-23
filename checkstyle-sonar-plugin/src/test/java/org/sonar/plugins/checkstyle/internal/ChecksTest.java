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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;

public final class ChecksTest {
    private static final String RULES_PATH = "src/main/resources/org/sonar/plugins/checkstyle/rules.xml";

    private static final Set<String> CHECK_PROPERTIES = getProperties(AbstractCheck.class);
    private static final Set<String> JAVADOC_CHECK_PROPERTIES = getProperties(AbstractJavadocCheck.class);
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

    @SuppressWarnings("static-method")
	@Ignore
    @Test
    public void verifyTestConfigurationFiles() throws Exception {
        final Set<Class<?>> modules = CheckUtil.getCheckstyleModules();

        Assert.assertTrue("no modules", modules.size() > 0);

        // sonar

        final File rulesFile = new File(RULES_PATH);

        Assert.assertTrue("'rules.xml' must exist", rulesFile.exists());

        final String input = new String(Files.readAllBytes(rulesFile.toPath()), UTF_8);

        final Document document = XmlUtil.getRawXml(rulesFile.getAbsolutePath(), input, input);

        validateSonarFile(document, modules);
    }

    private static void validateSonarFile(Document document, Set<Class<?>> modules) throws Exception {
        final NodeList rules = document.getElementsByTagName("rule");

        for (int position = 0; position < rules.getLength(); position++) {
            final Node rule = rules.item(position);
            final Set<Node> children = XmlUtil.getChildrenElements(rule);

            final String key = rule.getAttributes().getNamedItem("key").getTextContent();

            final Class<?> module = findModule(modules, key);
            modules.remove(module);

            Assert.assertNotNull("Unknown class found in sonar: " + key, module);

            final String moduleName = module.getName();
            final String moduleSimpleName = module.getSimpleName();
            final Node name = XmlUtil.findElementByTag(children, "name");

            Assert.assertNotNull(moduleName + " requires a name in sonar", name);
            Assert.assertFalse(moduleName + " requires a name in sonar", name.getTextContent()
                    .isEmpty());

            final Node configKey = XmlUtil.findElementByTag(children, "configKey");
            final String expectedConfigKey;

            if (AbstractCheck.class.isAssignableFrom(module)) {
                expectedConfigKey = "Checker/TreeWalker/" + moduleSimpleName.replaceAll("Check$", "");
            }
            else {
                expectedConfigKey = "Checker/" + moduleSimpleName.replaceAll("Check$", "");;
            }

            Assert.assertNotNull(moduleName + " requires a configKey in sonar", configKey);
            Assert.assertEquals(moduleName + " requires a valid configKey in sonar",
                    expectedConfigKey, configKey.getTextContent());

            validateSonarProperties(module, XmlUtil.findElementsByTag(children, "param"));
        }

        for (Class<?> module : modules) {
            Assert.fail("Module not found in sonar: " + module.getCanonicalName());
        }
    }

    private static void validateSonarProperties(Class<?> module, Set<Node> parameters) throws Exception {
        final String moduleName = module.getName();
        final Set<String> properties = getFinalProperties(module);

        for (Node parameter : parameters) {
            final NamedNodeMap attributes = parameter.getAttributes();
            final Node paramKeyNode = attributes.getNamedItem("key");

            Assert.assertNotNull(moduleName + " requires a key for unknown parameter in sonar",
                    paramKeyNode);

            final String paramKey = paramKeyNode.getTextContent();

            Assert.assertFalse(moduleName + " requires a valid key for unknown parameter in sonar",
                    paramKey.isEmpty());

            Assert.assertTrue(moduleName + " has an unknown parameter in sonar: " + paramKey,
                    properties.remove(paramKey));
        }

        for (String property : properties) {
            Assert.fail(moduleName + " parameter not found in sonar: " + property);
        }
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

    private static Set<String> getFinalProperties(Class<?> clss) throws Exception {
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
        Iterator<String> iter = properties.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            if(UNDOCUMENTED_PROPERTIES.contains(clss.getSimpleName() + "." + value)){
                iter.remove();
            }
        }

        if (AbstractCheck.class.isAssignableFrom(clss)) {
            final AbstractCheck check = (AbstractCheck) clss.newInstance();

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
