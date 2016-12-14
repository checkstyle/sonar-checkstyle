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
package org.sonar.plugins.checkstyle;

import com.google.common.collect.ImmutableList;

import org.junit.Ignore;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class CheckstyleRulesDefinitionTest {

  List<String> NO_SQALE = ImmutableList.of(
    "com.puppycrawl.tools.checkstyle.checks.TranslationCheck",
    "com.puppycrawl.tools.checkstyle.checks.TodoCommentCheck",
    "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck",
    "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineJavaCheck",
    "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck",
    "com.puppycrawl.tools.checkstyle.checks.regexp.RegexpOnFilenameCheck",
    "com.puppycrawl.tools.checkstyle.checks.RegexpCheck",
    "com.puppycrawl.tools.checkstyle.checks.header.RegexpHeaderCheck",
    "com.puppycrawl.tools.checkstyle.checks.imports.ImportControlCheck",
    "com.puppycrawl.tools.checkstyle.checks.annotation.AnnotationLocationCheck"
  );

  @Ignore
  @Test
  public void test() {
    CheckstyleRulesDefinition definition = new CheckstyleRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    definition.define(context);
    RulesDefinition.Repository repository = context.repository(CheckstyleConstants.REPOSITORY_KEY);

    assertThat(repository.name()).isEqualTo(CheckstyleConstants.REPOSITORY_NAME);
    assertThat(repository.language()).isEqualTo("java");

    List<RulesDefinition.Rule> rules = repository.rules();
    assertThat(rules).hasSize(150);

    for (RulesDefinition.Rule rule : rules) {
      assertThat(rule.key()).isNotNull();
      assertThat(rule.internalKey()).isNotNull();
      assertThat(rule.name()).isNotNull();
      assertThat(rule.htmlDescription()).isNotNull();
      assertThat(rule.severity()).isNotNull();

      for (RulesDefinition.Param param : rule.params()) {
        assertThat(param.name()).isNotNull();
        assertThat(param.description())
          .overridingErrorMessage("Description is not set for parameter '" + param.name() + "' of rule '" + rule.key())
          .isNotNull();
      }

      if (!NO_SQALE.contains(rule.key())) {
        assertThat(rule.debtRemediationFunction())
          .overridingErrorMessage("Sqale remediation function is not set for rule '" + rule.key())
          .isNotNull();
        assertThat(rule.debtSubCharacteristic())
          .overridingErrorMessage("Sqale characteristic is not set for rule '" + rule.key())
          .isNotNull();
      } else {
        assertThat(rule.debtRemediationFunction())
          .overridingErrorMessage("Sqale remediation function is set for rule '" + rule.key())
          .isNull();
        assertThat(rule.debtSubCharacteristic())
          .overridingErrorMessage("Sqale characteristic is set for rule '" + rule.key())
          .isNull();
      }
    }
  }

}
