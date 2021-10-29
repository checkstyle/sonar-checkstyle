////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2021 the original author or authors.
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
import java.util.Collections;
import java.util.HashMap;
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
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.System2;

public class CheckstyleProfileExporterTest {

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
    public void alwaysSetSuppressionCommentFilter() {
        final RulesProfile profile = RulesProfile.create("sonar way", "java");

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(profile, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "alwaysSetSuppressionCommentFilter.xml",
                sanitizeForTests(writer.toString()));
    }

    @Test
    public void noCheckstyleRulesToExport() {
        final RulesProfile profile = RulesProfile.create("sonar way", "java");

        // this is a PMD rule
        profile.activateRule(Rule.create("pmd", "PmdRule1", "PMD rule one"), null);

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(profile, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "noCheckstyleRulesToExport.xml", sanitizeForTests(writer.toString()));
    }

    @Test
    public void singleCheckstyleRulesToExport() {
        final RulesProfile profile = RulesProfile.create("sonar way", "java");
        profile.activateRule(Rule.create("pmd", "PmdRule1", "PMD rule one"), null);
        profile.activateRule(
                Rule.create("checkstyle",
                        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck",
                        "Javadoc").setConfigKey("Checker/JavadocPackage"), RulePriority.MAJOR);
        profile.activateRule(
                Rule.create(
                        "checkstyle",
                        "com.puppycrawl.tools.checkstyle.checks.naming.LocalFinalVariableNameCheck",
                        "Local Variable").setConfigKey(
                        "Checker/TreeWalker/Checker/TreeWalker/LocalFinalVariableName"),
                RulePriority.MINOR);

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(profile, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "singleCheckstyleRulesToExport.xml", sanitizeForTests(writer.toString()));
    }

    @Test
    public void ruleThrowsException() {
        final RulesProfile profile = RulesProfile.create("sonar way", "java");
        try {
            new CheckstyleProfileExporter(settings).exportProfile(profile, new IoExceptionWriter());
            Assert.fail("IOException while writing should not be ignored");
        }
        catch (IllegalStateException ex) {
            Assertions.assertThat(ex.getMessage())
                    .isEqualTo("Fail to export the profile " + profile);
        }
    }

    @Test
    public void singleCheckstyleActiveRulesToExport() {
        final ActiveRules activeRules = Mockito.mock(ActiveRules.class);
        Mockito.when(activeRules.findByRepository(CheckstyleConstants.REPOSITORY_KEY))
                .thenReturn(Collections.singletonList(new TestActiveRule()));

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(activeRules, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "singleCheckstyleActiveRulesToExport.xml",
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
    public void addTheIdPropertyWhenManyInstancesWithTheSameConfigKey() {
        final RulesProfile profile = RulesProfile.create("sonar way", "java");
        final Rule rule1 = Rule.create("checkstyle",
                "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck", "Javadoc")
                .setConfigKey("Checker/JavadocPackage");
        final Rule rule2 = Rule
                .create("checkstyle",
                        "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck_12345",
                        "Javadoc").setConfigKey("Checker/JavadocPackage").setParent(rule1);

        profile.activateRule(rule1, RulePriority.MAJOR);
        profile.activateRule(rule2, RulePriority.CRITICAL);

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(profile, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "addTheIdPropertyWhenManyInstancesWithTheSameConfigKey.xml",
                sanitizeForTests(writer.toString()));
    }

    @Test
    public void exportParameters() {
        final RulesProfile profile = RulesProfile.create("sonar way", "java");
        final Rule rule = Rule.create("checkstyle",
                "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck", "Javadoc")
                .setConfigKey("Checker/JavadocPackage");
        rule.createParameter("format");
        // not set in the profile and no default value => not exported in
        // checkstyle
        rule.createParameter("message");
        rule.createParameter("ignore");

        profile.activateRule(rule, RulePriority.MAJOR).setParameter("format", "abcde");

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(profile, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "exportParameters.xml", sanitizeForTests(writer.toString()));
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

        final RulesProfile profile = RulesProfile.create("sonar way", "java");
        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(profile, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "addCustomFilters.xml", sanitizeForTests(writer.toString()));
    }

    @Test
    public void addCustomTreewalkerFilters() {
        initSettings(CheckstyleConstants.TREEWALKER_FILTERS_KEY,
                "<module name=\"SuppressWithNearbyCommentFilter\"/>");

        final RulesProfile profile = RulesProfile.create("sonar way", "java");
        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(profile, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "addCustomTreewalkerFilters.xml", sanitizeForTests(writer.toString()));
    }

    @Test
    public void addTabWidthProperty() {
        initSettings(CheckstyleConstants.CHECKER_TAB_WIDTH, "8");

        final RulesProfile profile = RulesProfile.create("sonar way", "java");
        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(profile, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "addTabWidthProperty.xml", sanitizeForTests(writer.toString()));
    }

    @SuppressWarnings("unchecked")
    private void initSettings(@Nullable String key, @Nullable String property) {
        final MapSettings mapSettings = new MapSettings(
                new PropertyDefinitions(System2.INSTANCE, new CheckstylePlugin().getExtensions()));
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

    private static class IoExceptionWriter extends Writer {

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
     * Creation of {@link ActiveRule} has changed with SQ 7.5
     * to make use of a builder instead of direct instantiation.
     * However, SQ < 7.5 doesn't support the builder yet and,
     * to keep compatibility down, we have to create a dummy
     * implementation for testing it.
     *
     * Once we increase compatibility to 7.X LTS,
     * we can remove this class and use the builder pattern directly.
     */
    private static class TestActiveRule implements ActiveRule {

        @Override
        public RuleKey ruleKey() {
            return RuleKey.of(CheckstyleConstants.REPOSITORY_KEY,
                    "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck");
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
            return new HashMap<>();
        }

        @CheckForNull
        @Override
        public String internalKey() {
            return "Checker/JavadocPackage";
        }

        @CheckForNull
        @Override
        public String templateRuleKey() {
            return "TEMPLATE";
        }
    }
}
