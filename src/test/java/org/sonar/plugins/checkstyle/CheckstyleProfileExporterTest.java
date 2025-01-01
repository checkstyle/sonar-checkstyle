////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2025 the original author or authors.
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

package org.sonar.plugins.checkstyle;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.DefaultActiveRules;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.System2;

public class CheckstyleProfileExporterTest {

    private final ActiveRule testActiveRule = new TestActiveRule(
                    RuleKey.of(CheckstyleConstants.REPOSITORY_KEY,
                    "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck"),
                    "Checker/JavadocPackage", "TEMPLATE", "abcde");

    private Configuration settings;

    @Before
    public void prepare() {
        initSettings(null, null);
        System.setProperty("javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
    }

    @After
    public void tearDown() {
        System.setProperty("javax.xml.transform.TransformerFactory", "");
    }

    @Test
    public void noCheckstyleActiveRulesToExport() {
        final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
        Mockito.when(activeRules.findByRepository(CheckstyleConstants.REPOSITORY_KEY))
                .thenReturn(Collections.emptyList());

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(activeRules, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "noCheckstyleActiveRulesToExport.xml",
                sanitizeForTests(writer.toString()));
    }

    @Test
    public void singleCheckstyleActiveRulesToExport() {
        final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
        Mockito.when(activeRules.findByRepository(CheckstyleConstants.REPOSITORY_KEY))
                .thenReturn(Collections.singletonList(testActiveRule));

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(activeRules, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "singleCheckstyleActiveRulesToExport.xml",
                sanitizeForTests(writer.toString()));
    }

    @Test
    public void treewalkerCheckstyleActiveRulesToExport() {
        final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
        Mockito.when(activeRules.findByRepository(CheckstyleConstants.REPOSITORY_KEY))
                .thenReturn(Collections.singletonList(new TestActiveRule(
                    RuleKey.of(CheckstyleConstants.REPOSITORY_KEY,
                    "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck"),
                    "Checker/TreeWalker/JavadocPackage", "TEMPLATE", null)));

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(activeRules, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "treewalkerCheckstyleActiveRulesToExport.xml",
                sanitizeForTests(writer.toString()));
    }

    @Test
    public void sameCheckstyleActiveRulesToExport() {
        final List<ActiveRule> rules = new ArrayList<>();
        rules.add(testActiveRule);
        rules.add(new TestActiveRule(RuleKey.of(CheckstyleConstants.REPOSITORY_KEY,
                    "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck"),
                    "Checker/JavadocPackage", "TEMPLATE", "fghij"));
        final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
        Mockito.when(activeRules.findByRepository(CheckstyleConstants.REPOSITORY_KEY))
                .thenReturn(rules);

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(activeRules, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "sameCheckstyleActiveRulesToExport.xml",
                sanitizeForTests(writer.toString()));
    }

    @Test
    public void noCheckstyleTemplateActiveRulesToExport() {
        final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
        Mockito.when(activeRules.findByRepository(CheckstyleConstants.REPOSITORY_KEY))
                .thenReturn(Collections.singletonList(new TestActiveRule(
                    RuleKey.of(CheckstyleConstants.REPOSITORY_KEY,
                    "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck"),
                    "Checker/JavadocPackage", null, "fghij")));

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(activeRules, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "noCheckstyleTemplateActiveRulesToExport.xml",
                sanitizeForTests(writer.toString()));
    }

    @Test
    public void blankParamCheckstyleActiveRulesToExport() {
        final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
        Mockito.when(activeRules.findByRepository(CheckstyleConstants.REPOSITORY_KEY))
                .thenReturn(Collections.singletonList(new TestActiveRule(
                    RuleKey.of(CheckstyleConstants.REPOSITORY_KEY,
                    "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck"),
                    "Checker/JavadocPackage", "TEMPLATE", "")));

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(activeRules, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "blankParamCheckstyleActiveRulesToExport.xml",
                sanitizeForTests(writer.toString()));
    }

    @Test
    public void activeRulesThrowsException() {
        try {
            new CheckstyleProfileExporter(settings).exportProfile(
                    new DefaultActiveRules(Collections.emptyList()), new IoExceptionWriter());
            Assert.fail("IOException while writing should not be ignored");
        }
        catch (IllegalStateException ex) {
            Assertions.assertThat(ex.getMessage()).isEqualTo("Fail to export active rules.");
        }
    }

    @Test
    public void addCustomCheckerFilters() {
        initSettings(CheckstyleConstants.CHECKER_FILTERS_KEY,
                "<module name=\"SuppressionCommentFilter\">"
                        + "<property name=\"offCommentFormat\" value=\"BEGIN GENERATED CODE\"/>"
                        + "<property name=\"onCommentFormat\" value=\"END GENERATED CODE\"/>"
                        + "</module>" + "<module name=\"SuppressWithNearbyCommentFilter\">"
                        + "<property name=\"commentFormat\""
                        + " value=\"CHECKSTYLE IGNORE (\\w+) FOR NEXT (\\d+) LINES\"/>"
                        + "<property name=\"checkFormat\" value=\"$1\"/>"
                        + "<property name=\"messageFormat\" value=\"$2\"/>" + "</module>");

        final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
        Mockito.when(activeRules.findByRepository(CheckstyleConstants.REPOSITORY_KEY))
                .thenReturn(Collections.singletonList(testActiveRule));
        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(activeRules, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "addCustomFilters.xml", sanitizeForTests(writer.toString()));
    }

    @Test
    public void addCustomTreewalkerFilters() {
        initSettings(CheckstyleConstants.TREEWALKER_FILTERS_KEY,
                "<module name=\"SuppressWithNearbyCommentFilter\"/>");

        final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
        Mockito.when(activeRules.findByRepository(CheckstyleConstants.REPOSITORY_KEY))
                .thenReturn(Collections.singletonList(testActiveRule));
        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(activeRules, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "addCustomTreewalkerFilters.xml", sanitizeForTests(writer.toString()));
    }

    @Test
    public void addTabWidthProperty() {
        initSettings(CheckstyleConstants.CHECKER_TAB_WIDTH, "8");

        final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
        Mockito.when(activeRules.findByRepository(CheckstyleConstants.REPOSITORY_KEY))
                .thenReturn(Collections.singletonList(testActiveRule));
        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(activeRules, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "addTabWidthProperty.xml", sanitizeForTests(writer.toString()));
    }

    @SuppressWarnings("unchecked")
    private void initSettings(@Nullable String key, @Nullable String property) {
        final MapSettings mapSettings = new MapSettings(
                new PropertyDefinitions(System2.INSTANCE, CheckstylePlugin.getExtensions()));
        if (Objects.nonNull(key)) {
            mapSettings.setProperty(key, property);
        }
        settings = new ConfigurationBridge(mapSettings);
    }

    private static String sanitizeForTests(String xml) {
        // remove the doctype declaration, else the unit test fails when
        // executed offline
        return StringUtils.remove(xml, CheckstyleProfileExporter.DOCTYPE_DECLARATION);
    }

    private static final class IoExceptionWriter extends Writer {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            throw new IOException("test exception handling");
        }

        @Override
        public void flush() throws IOException {
            throw new IOException("test exception handling");
        }

        @Override
        public void close() throws IOException {
            throw new IOException("test exception handling");
        }
    }

    /**
     * Creation of {@link ActiveRule} for testing purposes.
     */
    private static final class TestActiveRule implements ActiveRule {
        private final RuleKey activeRuleKey;
        private final String ruleInternalKey;
        private final String templateActiveRuleKey;
        private final Map<String, String> parameters = new HashMap<>();

        TestActiveRule(RuleKey activeRuleKey,
                       String ruleInternalKey,
                       String templateActiveRuleKey,
                       String paramValue) {
            this.activeRuleKey = activeRuleKey;
            this.ruleInternalKey = ruleInternalKey;
            this.templateActiveRuleKey = templateActiveRuleKey;
            parameters.put("format", paramValue);
        }

        @Override
        public RuleKey ruleKey() {
            return activeRuleKey;
        }

        @Override
        public String severity() {
            return "MAJOR";
        }

        @Override
        public String language() {
            return null;
        }

        @CheckForNull
        @Override
        public String param(String paramKey) {
            return null;
        }

        @CheckForNull
        @Override
        public String qpKey() {
            return null;
        }

        @Override
        public Map<String, String> params() {
            return Collections.unmodifiableMap(parameters);
        }

        @CheckForNull
        @Override
        public String internalKey() {
            return ruleInternalKey;
        }

        @CheckForNull
        @Override
        public String templateRuleKey() {
            return templateActiveRuleKey;
        }
    }
}
