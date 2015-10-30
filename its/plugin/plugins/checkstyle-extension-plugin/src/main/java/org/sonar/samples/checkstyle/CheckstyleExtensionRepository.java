/*
 * Java :: IT :: Plugins :: Checkstyle Extension
 * Copyright (C) 2013 SonarSource
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
package org.sonar.samples.checkstyle;

import org.apache.commons.io.IOUtils;
import org.sonar.api.resources.Java;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;

import java.io.InputStream;
import java.util.List;

public final class CheckstyleExtensionRepository extends RuleRepository {

  private XMLRuleParser xmlRuleParser;

  public CheckstyleExtensionRepository(XMLRuleParser xmlRuleParser) {
    super("checkstyle", Java.KEY);
    setName("Checkstyle");
    this.xmlRuleParser = xmlRuleParser;
  }

  @Override
  public List<Rule> createRules() {
    InputStream input = getClass().getResourceAsStream("/org/sonar/samples/checkstyle/checkstyle-extensions.xml");
    try {
      return xmlRuleParser.parse(input);

    } finally {
      IOUtils.closeQuietly(input);
    }
  }
}
