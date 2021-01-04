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

package org.sonar.plugins.checkstyle;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.server.rule.RulesDefinition;

import com.google.common.collect.ImmutableList;

public class CheckstyleRulesDefinitionTest {

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

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        final CheckstyleRulesDefinition definition = new CheckstyleRulesDefinition();
        final RulesDefinition.Context context = new RulesDefinition.Context();
        definition.define(context);
        final RulesDefinition.Repository repository = context
                .repository(CheckstyleConstants.REPOSITORY_KEY);

        assertThat(repository.name()).isEqualTo(CheckstyleConstants.REPOSITORY_NAME);
        assertThat(repository.language()).isEqualTo("java");

        final List<RulesDefinition.Rule> rules = repository.rules();
        final Map<String, Integer> ruleCounts = new HashMap<>();
        rules.forEach(rule -> {
            final String name = rule.key().replace("template", "");
            ruleCounts.merge(name, 1, Integer::sum);
        });
        final List<String> duplicatedRuleWithTemplate = ruleCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
        final List<String> rulesWithDuplicateTemplate = ruleCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
        // such number should not change during checkstyle version upgrade
        assertThat(duplicatedRuleWithTemplate).hasSize(174);
        // all new Rules should fall in this group
        assertThat(rulesWithDuplicateTemplate).hasSize(8);

        for (RulesDefinition.Rule rule : rules) {
            assertThat(rule.key()).isNotNull();
            assertThat(rule.internalKey()).isNotNull();
            assertThat(rule.name()).isNotNull();
            assertThat(rule.htmlDescription()).isNotNull();
            assertThat(rule.severity()).isNotNull();

            for (RulesDefinition.Param param : rule.params()) {
                assertThat(param.name()).isNotNull();
                assertThat(param.description()).overridingErrorMessage(
                        "Description is not set for parameter '" + param.name() + "' of rule '"
                                + rule.key()).isNotNull();
            }

            if (NO_SQALE.contains(rule.key())) {
                assertThat(rule.debtRemediationFunction()).overridingErrorMessage(
                        "Sqale remediation function is set for rule '" + rule.key()).isNull();
            }
            else {
                assertThat(rule.debtRemediationFunction()).overridingErrorMessage(
                        "Sqale remediation function is not set for rule '" + rule.key())
                        .isNotNull();
            }
        }
    }
}
