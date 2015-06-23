/*
 * Copyright (C) 2009-2012 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
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
