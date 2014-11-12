/*
 * SonarQube Checkstyle Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
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

import com.google.common.io.Closeables;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.api.utils.SonarException;
import org.sonar.test.TestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CheckstyleRuleRepositoryTest {
  CheckstyleRuleRepository repository;

  @Before
  public void setUpRuleRepository() {
    repository = new CheckstyleRuleRepository(mock(ServerFileSystem.class), new XMLRuleParser());
  }

  @Test
  public void loadRepositoryFromXml() {
    List<Rule> rules = repository.createRules();

    assertThat(repository.getKey()).isEqualTo("checkstyle");
    assertThat(rules.size()).isEqualTo(144);
  }

  @Test
  public void should_provide_a_name_and_description_for_each_rule() throws IOException {
    List<Rule> rules = createRulesWithNameAndDescription("checkstyle", repository);

    assertThat(rules).onProperty("name").excludes(null, "");
    assertThat(rules).onProperty("description").excludes(null, "");
  }

  public static List<Rule> createRulesWithNameAndDescription(String pluginKey, RuleRepository repository) throws IOException {
    Properties props = loadProperties(String.format("/org/sonar/l10n/%s.properties", pluginKey));

    List<Rule> rules = repository.createRules();
    for (Rule rule : rules) {
      String name = props.getProperty(String.format("rule.%s.%s.name", repository.getKey(), rule.getKey()));
      String description = CheckstyleTestUtils.getResourceContent(String.format("/org/sonar/l10n/%s/rules/%s/%s.html", pluginKey, repository.getKey(), rule.getKey()));

      rule.setName(name);
      rule.setDescription(description);
    }

    return rules;
  }

  private static Properties loadProperties(String resourcePath) {
    Properties properties = new Properties();

    InputStream input = null;
    try {
      input = TestUtils.class.getResourceAsStream(resourcePath);
      properties.load(input);
      return properties;
    } catch (IOException e) {
      throw new SonarException("Unable to read properties " + resourcePath, e);
    } finally {
      Closeables.closeQuietly(input);
    }
  }
}
