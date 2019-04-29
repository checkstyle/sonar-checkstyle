////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;

public class CheckstyleProfileImporterTest {

    private ValidationMessages messages;
    private CheckstyleProfileImporter importer;

    @Before
    public void before() {
        messages = ValidationMessages.create();

        /*
         * The mocked rule finder defines 3 rules :
         *
         * - JavadocCheck with 2 paramters format and ignore, default priority
         * is MAJOR
         * - EqualsHashCodeCheck without parameters, default priority
         * is BLOCKER
         * - MissingOverride with 1 parameter javaFiveCompatibility,
         * default priority is MINOR
         */
        importer = new CheckstyleProfileImporter(newRuleFinder());
    }

    @Test
    public void importSimpleProfile() {
        final Reader reader = new StringReader(CheckstyleTestUtils.getResourceContent(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileImporterTest/simple.xml"));
        final RulesProfile profile = importer.importProfile(reader, messages);

        assertThat(profile.getActiveRules().size()).isEqualTo(2);
        assertNotNull(profile.getActiveRuleByConfigKey("checkstyle",
                "Checker/TreeWalker/EqualsHashCode"));
        assertNotNull(profile.getActiveRuleByConfigKey("checkstyle", "Checker/JavadocPackage"));
        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void importParameters() {
        final Reader reader = new StringReader(CheckstyleTestUtils.getResourceContent(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileImporterTest/simple.xml"));
        final RulesProfile profile = importer.importProfile(reader, messages);

        final ActiveRule javadocCheck = profile.getActiveRuleByConfigKey("checkstyle",
                "Checker/JavadocPackage");
        assertThat(javadocCheck.getActiveRuleParams()).hasSize(2);
        assertThat(javadocCheck.getParameter("format")).isEqualTo("abcde");
        assertThat(javadocCheck.getParameter("ignore")).isEqualTo("true");
        // checkstyle internal parameter
        assertThat(javadocCheck.getParameter("severity")).isNull();
    }

    @Test
    public void propertiesShouldBeInherited() {
        final Reader reader = new StringReader(CheckstyleTestUtils.getResourceContent(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileImporterTest/"
                + "inheritance_of_properties.xml"));
        final RulesProfile profile = importer.importProfile(reader, messages);

        final ActiveRule activeRule = profile.getActiveRuleByConfigKey("checkstyle",
                "Checker/TreeWalker/MissingOverride");
        assertThat(activeRule.getSeverity()).isEqualTo(RulePriority.BLOCKER);
        assertThat(activeRule.getParameter("javaFiveCompatibility")).isEqualTo("true");
    }

    @Test
    public void importPriorities() {
        final Reader reader = new StringReader(CheckstyleTestUtils.getResourceContent(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileImporterTest/simple.xml"));
        final RulesProfile profile = importer.importProfile(reader, messages);

        final ActiveRule javadocCheck = profile.getActiveRuleByConfigKey("checkstyle",
                "Checker/JavadocPackage");
        assertThat(javadocCheck.getSeverity()).isEqualTo(RulePriority.BLOCKER);
    }

    @Test
    public void priorityIsOptional() {
        final Reader reader = new StringReader(CheckstyleTestUtils.getResourceContent(
                "/org/sonar/plugins/checkstyle/"
                        + "CheckstyleProfileImporterTest/simple.xml"));
        final RulesProfile profile = importer.importProfile(reader, messages);

        final ActiveRule activeRule = profile.getActiveRuleByConfigKey("checkstyle",
                "Checker/TreeWalker/EqualsHashCode");
        // reuse the rule default priority
        assertThat(activeRule.getSeverity()).isEqualTo(RulePriority.BLOCKER);
    }

    @Test
    public void idPropertyShouldBeTheRuleKey() {
        final Reader reader = new StringReader(CheckstyleTestUtils.getResourceContent(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileImporterTest/"
                        + "idPropertyShouldBeTheRuleKey.xml"));
        final RulesProfile profile = importer.importProfile(reader, messages);

        assertNull(profile.getActiveRuleByConfigKey("checkstyle", "Checker/JavadocPackage"));
        assertThat(messages.getWarnings().size()).isEqualTo(1);
    }

    @Test
    public void shouldUseTheIdPropertyToFindRule() {
        final Reader reader = new StringReader(
                CheckstyleTestUtils.getResourceContent("/org/sonar/plugins/checkstyle/"
                        + "CheckstyleProfileImporterTest/shouldUseTheIdPropertyToFindRule.xml"));
        final RulesProfile profile = importer.importProfile(reader, messages);

        assertNotNull(profile.getActiveRuleByConfigKey("checkstyle", "Checker/JavadocPackage"));
        assertThat(
                profile.getActiveRuleByConfigKey("checkstyle", "Checker/JavadocPackage").getRule()
                        .getKey()).isEqualTo(
                "com.puppycrawl.tools.checkstyle.checks.javadoc." + "JavadocPackageCheck_12345");
        assertThat(messages.getWarnings().size()).isEqualTo(0);
    }

    @Test
    public void testUnvalidXml() {
        final Reader reader = new StringReader("not xml");
        importer.importProfile(reader, messages);
        assertThat(messages.getErrors().size()).isEqualTo(1);
    }

    @Test
    public void importingFiltersIsNotSupported() {
        final Reader reader = new StringReader(
                CheckstyleTestUtils.getResourceContent("/org/sonar/plugins/checkstyle/"
                        + "CheckstyleProfileImporterTest/importingFiltersIsNotSupported.xml"));
        final RulesProfile profile = importer.importProfile(reader, messages);

        assertNull(profile.getActiveRuleByConfigKey("checkstyle",
                "Checker/SuppressionCommentFilter"));
        assertThat(profile.getActiveRules().size()).isEqualTo(2);
        assertThat(messages.getWarnings().size()).isEqualTo(5);
    }

    private static RuleFinder newRuleFinder() {
        final RuleFinder ruleFinder = mock(RuleFinder.class);
        when(ruleFinder.find(any(RuleQuery.class))).thenAnswer(new RuleAnswer());
        return ruleFinder;
    }

    private static class RuleAnswer implements Answer<Rule> {
        @Override
        public Rule answer(InvocationOnMock iom) {
            final RuleQuery query = (RuleQuery) iom.getArguments()[0];
            Rule rule = null;
            if (StringUtils.equals(query.getConfigKey(), "Checker/JavadocPackage")) {
                rule = Rule
                        .create(query.getRepositoryKey(),
                                "com.puppycrawl.tools.checkstyle.checks.javadoc."
                                        + "JavadocPackageCheck", "Javadoc Package")
                        .setConfigKey("Checker/JavadocPackage")
                        .setSeverity(RulePriority.MAJOR);
                rule.createParameter("format");
                rule.createParameter("ignore");

            }
            else if (StringUtils.equals(query.getConfigKey(),
                    "Checker/TreeWalker/EqualsHashCode")) {
                rule = Rule
                        .create(query.getRepositoryKey(),
                                "com.puppycrawl.tools.checkstyle.checks.coding."
                                        + "EqualsHashCodeCheck",
                                "Equals HashCode")
                        .setConfigKey("Checker/TreeWalker/EqualsHashCode")
                        .setSeverity(RulePriority.BLOCKER);

            }
            else if (StringUtils.equals(query.getKey(),
                    "com.puppycrawl.tools.checkstyle.checks.javadoc."
                            + "JavadocPackageCheck_12345")) {
                rule = Rule
                        .create(query.getRepositoryKey(),
                                "com.puppycrawl.tools.checkstyle.checks.javadoc."
                                        + "JavadocPackageCheck_12345",
                                "Javadoc Package").setConfigKey("Checker/JavadocPackage")
                        .setSeverity(RulePriority.MAJOR);
                rule.createParameter("format");
                rule.createParameter("ignore");
            }
            else if (StringUtils.equals(query.getConfigKey(),
                    "Checker/TreeWalker/MissingOverride")) {
                rule = Rule
                        .create(query.getRepositoryKey(),
                                "com.puppycrawl.tools.checkstyle.checks.annotation."
                                        + "MissingOverrideCheck",
                                "Missing Override")
                        .setConfigKey("Checker/TreeWalker/MissingOverride")
                        .setSeverity(RulePriority.MINOR);
                rule.createParameter("javaFiveCompatibility");
            }
            return rule;
        }
    }
}
