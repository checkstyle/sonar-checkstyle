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

import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;

public class CheckstyleProfileExporterTest {

    private Settings settings;

    @SuppressWarnings("unchecked")
    @Before
    public void prepare() {
        settings = new Settings(new PropertyDefinitions(new CheckstylePlugin().getExtensions()));
    }

    @Test
    public void alwaysSetFileContentsHolderAndSuppressionCommentFilter() {
        final RulesProfile profile = RulesProfile.create("sonar way", "java");

        final StringWriter writer = new StringWriter();
        new CheckstyleProfileExporter(settings).exportProfile(profile, writer);

        CheckstyleTestUtils.assertSimilarXmlWithResource(
                "/org/sonar/plugins/checkstyle/CheckstyleProfileExporterTest/"
                        + "alwaysSetFileContentsHolderAndSuppressionCommentFilter.xml",
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
                Rule.create("checkstyle",
                        "com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck",
                        "Line Length").setConfigKey("Checker/TreeWalker/LineLength"),
                RulePriority.CRITICAL);
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
    public void addCustomFilters() {
        settings.setProperty(CheckstyleConstants.FILTERS_KEY,
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

    private static String sanitizeForTests(String xml) {
        // remove the doctype declaration, else the unit test fails when
        // executed offline
        return StringUtils.remove(xml, CheckstyleProfileExporter.DOCTYPE_DECLARATION);
    }
}
