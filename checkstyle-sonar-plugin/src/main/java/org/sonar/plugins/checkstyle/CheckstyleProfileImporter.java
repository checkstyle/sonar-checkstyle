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

import java.io.Reader;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CheckstyleProfileImporter extends ProfileImporter {

  private static final Logger LOG = LoggerFactory.getLogger(CheckstyleProfileImporter.class);

  private static final String CHECKER_MODULE = "Checker";
  private static final String TREEWALKER_MODULE = "TreeWalker";
  private static final String MODULE_NODE = "module";
  private static final String[] FILTERS = new String[] {
    "SeverityMatchFilter",
    "SuppressionFilter",
    "SuppressWarningsFilter",
    "SuppressionCommentFilter",
    "SuppressWithNearbyCommentFilter"
  };
  private final RuleFinder ruleFinder;

  private static class Module {
    private String name;
    private Map<String, String> properties = Maps.newHashMap();
    private List<Module> modules = Lists.newArrayList();
  }

  public CheckstyleProfileImporter(RuleFinder ruleFinder) {
    super(CheckstyleConstants.REPOSITORY_KEY, CheckstyleConstants.PLUGIN_NAME);
    setSupportedLanguages(CheckstyleConstants.JAVA_KEY);
    this.ruleFinder = ruleFinder;
  }

  private Module loadModule(SMInputCursor parentCursor) throws XMLStreamException {
    Module result = new Module();
    result.name = parentCursor.getAttrValue("name");
    SMInputCursor cursor = parentCursor.childElementCursor();
    while (cursor.getNext() != null) {
      String nodeName = cursor.getLocalName();
      if (MODULE_NODE.equals(nodeName)) {
        result.modules.add(loadModule(cursor));
      } else if ("property".equals(nodeName)) {
        String key = cursor.getAttrValue("name");
        String value = cursor.getAttrValue("value");
        result.properties.put(key, value);
      }
    }
    return result;
  }

  @Override
  public RulesProfile importProfile(Reader reader, ValidationMessages messages) {
    SMInputFactory inputFactory = initStax();
    RulesProfile profile = RulesProfile.create();
    try {
      Module checkerModule = loadModule(inputFactory.rootElementCursor(reader).advance());

      for (Module rootModule : checkerModule.modules) {
        Map<String, String> rootModuleProperties = Maps.newHashMap(checkerModule.properties);
        rootModuleProperties.putAll(rootModule.properties);

        if (StringUtils.equals(TREEWALKER_MODULE, rootModule.name)) {
          for (Module treewalkerModule : rootModule.modules) {
            Map<String, String> treewalkerModuleProperties = Maps.newHashMap(rootModuleProperties);
            treewalkerModuleProperties.putAll(treewalkerModule.properties);

            processModule(profile, CHECKER_MODULE + "/" + TREEWALKER_MODULE + "/", treewalkerModule.name, treewalkerModuleProperties, messages);
          }
        } else {
          processModule(profile, CHECKER_MODULE + "/", rootModule.name, rootModuleProperties, messages);
        }
      }

    } catch (XMLStreamException e) {
      String message = "XML is not valid: " + e.getMessage();
      LOG.error(message, e);
      messages.addErrorText(message);
    }
    return profile;
  }

  private static SMInputFactory initStax() {
    XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    return new SMInputFactory(xmlFactory);
  }

  private void processModule(RulesProfile profile, String path, String moduleName, Map<String, String> properties, ValidationMessages messages) {
    if (isFilter(moduleName)) {
      messages.addWarningText("Checkstyle filters are not imported: " + moduleName);

    } else if (!isIgnored(moduleName)) {
      processRule(profile, path, moduleName, properties, messages);
    }
  }

  @VisibleForTesting
  static boolean isIgnored(String configKey) {
    return StringUtils.equals(configKey, "FileContentsHolder") || StringUtils.equals(configKey, "SuppressWarningsHolder");
  }

  @VisibleForTesting
  static boolean isFilter(String configKey) {
    for (String filter : FILTERS) {
      if (StringUtils.equals(configKey, filter)) {
        return true;
      }
    }
    return false;
  }

  private void processRule(RulesProfile profile, String path, String moduleName, Map<String, String> properties, ValidationMessages messages) {
    Rule rule;
    String id = properties.get("id");
    String warning;
    if (StringUtils.isNotBlank(id)) {
      rule = ruleFinder.find(RuleQuery.create().withRepositoryKey(CheckstyleConstants.REPOSITORY_KEY).withKey(id));
      warning = "Checkstyle rule with key '" + id + "' not found";

    } else {
      String configKey = path + moduleName;
      rule = ruleFinder.find(RuleQuery.create().withRepositoryKey(CheckstyleConstants.REPOSITORY_KEY).withConfigKey(configKey));
      warning = "Checkstyle rule with config key '" + configKey + "' not found";
    }

    if (rule == null) {
      messages.addWarningText(warning);

    } else {
      ActiveRule activeRule = profile.activateRule(rule, null);
      activateProperties(activeRule, properties);
    }
  }

  private static void activateProperties(ActiveRule activeRule, Map<String, String> properties) {
    for (Map.Entry<String, String> property : properties.entrySet()) {
      if (StringUtils.equals("severity", property.getKey())) {
        activeRule.setSeverity(CheckstyleSeverityUtils.fromSeverity(property.getValue()));

      } else if (!StringUtils.equals("id", property.getKey())) {
        activeRule.setParameter(property.getKey(), property.getValue());
      }
    }
  }

}
