////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2023 the original author or authors.
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

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.ValidationMessages;

import com.google.common.annotations.VisibleForTesting;

@ExtensionPoint
@ScannerSide
public class CheckstyleProfileImporter extends ProfileImporter {

    private static final Logger LOG = LoggerFactory.getLogger(CheckstyleProfileImporter.class);

    private static final String CHECKER_MODULE = "Checker";
    private static final String TREEWALKER_MODULE = "TreeWalker";
    private static final String MODULE_NODE = "module";
    private static final String[] FILTERS = {
        "SeverityMatchFilter",
        "SuppressionFilter",
        "SuppressWarningsFilter",
        "SuppressionCommentFilter",
        "SuppressWithNearbyCommentFilter",
        "SuppressionXpathSingleFilter",
        "SuppressionSingleFilter",
    };
    private final RuleFinder ruleFinder;

    public CheckstyleProfileImporter(RuleFinder ruleFinder) {
        super(CheckstyleConstants.REPOSITORY_KEY, CheckstyleConstants.PLUGIN_NAME);
        setSupportedLanguages(CheckstyleConstants.JAVA_KEY);
        this.ruleFinder = ruleFinder;
    }

    private Module loadModule(SMInputCursor parentCursor) throws XMLStreamException {
        final Module result = new Module();
        result.name = parentCursor.getAttrValue("name");
        final SMInputCursor cursor = parentCursor.childElementCursor();
        while (cursor.getNext() != null) {
            final String nodeName = cursor.getLocalName();
            if (MODULE_NODE.equals(nodeName)) {
                result.modules.add(loadModule(cursor));
            }
            else if ("property".equals(nodeName)) {
                final String key = cursor.getAttrValue("name");
                final String value = cursor.getAttrValue("value");
                result.properties.put(key, value);
            }
        }
        return result;
    }

    @Override
    public RulesProfile importProfile(Reader reader, ValidationMessages messages) {
        final SMInputFactory inputFactory = initStax();
        final RulesProfile profile = RulesProfile.create();
        try {
            final Module checkerModule = loadModule(inputFactory.rootElementCursor(reader)
                    .advance());

            for (Module rootModule : checkerModule.modules) {
                final Map<String, String> rootModuleProperties = new HashMap<>(
                        checkerModule.properties);
                rootModuleProperties.putAll(rootModule.properties);

                if (StringUtils.equals(TREEWALKER_MODULE, rootModule.name)) {
                    processTreewalker(profile, rootModule, rootModuleProperties, messages);
                }
                else {
                    processModule(profile, CHECKER_MODULE + "/", rootModule.name,
                            rootModuleProperties, messages);
                }
            }

        }
        catch (XMLStreamException ex) {
            final String message = "XML is not valid: " + ex.getMessage();
            LOG.error(message, ex);
            messages.addErrorText(message);
        }
        return profile;
    }

    private void processTreewalker(RulesProfile profile, Module rootModule,
            Map<String, String> rootModuleProperties, ValidationMessages messages) {
        for (Module treewalkerModule : rootModule.modules) {
            final Map<String, String> treewalkerModuleProperties = new HashMap<>(
                    rootModuleProperties);
            treewalkerModuleProperties.putAll(treewalkerModule.properties);

            processModule(profile, CHECKER_MODULE + "/" + TREEWALKER_MODULE + "/",
                    treewalkerModule.name, treewalkerModuleProperties, messages);
        }
    }

    private static SMInputFactory initStax() {
        final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
        xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        xmlFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        return new SMInputFactory(xmlFactory);
    }

    private void processModule(RulesProfile profile, String path, String moduleName,
            Map<String, String> properties, ValidationMessages messages) {
        if (isFilter(moduleName)) {
            messages.addWarningText("Checkstyle filters are not imported: " + moduleName);

        }
        else if (!isIgnored(moduleName)) {
            processRule(profile, path, moduleName, properties, messages);
        }
    }

    @VisibleForTesting
    static boolean isIgnored(String configKey) {
        return StringUtils.equals(configKey, "SuppressWarningsHolder");
    }

    @VisibleForTesting
    static boolean isFilter(String configKey) {
        boolean result = false;
        for (String filter : FILTERS) {
            if (StringUtils.equals(configKey, filter)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void processRule(RulesProfile profile, String path, String moduleName,
            Map<String, String> properties, ValidationMessages messages) {
        final Rule rule;
        final String id = properties.get("id");
        final String warning;
        if (StringUtils.isNotBlank(id)) {
            rule = ruleFinder.find(RuleQuery.create()
                    .withRepositoryKey(CheckstyleConstants.REPOSITORY_KEY).withKey(id));
            warning = "Checkstyle rule with key '" + id + "' not found";

        }
        else {
            final String configKey = path + moduleName;
            rule = ruleFinder
                    .find(RuleQuery.create().withRepositoryKey(CheckstyleConstants.REPOSITORY_KEY)
                            .withConfigKey(configKey));
            warning = "Checkstyle rule with config key '" + configKey + "' not found";
        }

        if (rule == null) {
            messages.addWarningText(warning);

        }
        else {
            final ActiveRule activeRule = profile.activateRule(rule, null);
            activateProperties(activeRule, properties);
        }
    }

    private static void activateProperties(ActiveRule activeRule, Map<String, String> properties) {
        for (Map.Entry<String, String> property : properties.entrySet()) {
            if (StringUtils.equals("severity", property.getKey())) {
                activeRule.setSeverity(CheckstyleSeverityUtils.fromSeverity(property.getValue()));

            }
            else if (!StringUtils.equals("id", property.getKey())) {
                activeRule.setParameter(property.getKey(), property.getValue());
            }
        }
    }

    private static final class Module {
        private final Map<String, String> properties = new HashMap<>();
        private final List<Module> modules = new ArrayList<>();
        private String name;
    }
}
