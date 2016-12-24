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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.squidbridge.rules.ExternalDescriptionLoader;
import org.sonar.squidbridge.rules.PropertyFileLoader;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

import com.google.common.annotations.VisibleForTesting;

public final class CheckstyleRulesDefinition implements RulesDefinition {

  @Override
  public void define(Context context) {
    NewRepository repository = context
      .createRepository(CheckstyleConstants.REPOSITORY_KEY, "java")
      .setName(CheckstyleConstants.REPOSITORY_NAME);

    extractRulesData(repository, "/org/sonar/plugins/checkstyle/rules.xml", "/org/sonar/l10n/checkstyle/rules/checkstyle");

    repository.done();
  }

  @VisibleForTesting
  static void extractRulesData(NewRepository repository, String xmlRulesFilePath, String htmlDescriptionFolder) {
    RulesDefinitionXmlLoader ruleLoader = new RulesDefinitionXmlLoader();
    ruleLoader.load(repository, CheckstyleRulesDefinition.class.getResourceAsStream(xmlRulesFilePath), "UTF-8");
    ExternalDescriptionLoader.loadHtmlDescriptions(repository, htmlDescriptionFolder);
    PropertyFileLoader.loadNames(repository, CheckstyleRulesDefinition.class.getResourceAsStream("/org/sonar/l10n/checkstyle.properties"));
    SqaleXmlLoader.load(repository, "/com/sonar/sqale/checkstyle-model.xml");
  }
}
