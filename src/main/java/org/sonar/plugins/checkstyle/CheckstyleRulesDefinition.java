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

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.squidbridge.rules.ExternalDescriptionLoader;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

import com.google.common.annotations.VisibleForTesting;

@ExtensionPoint
@ScannerSide
public final class CheckstyleRulesDefinition implements RulesDefinition {

    @Override
    public void define(Context context) {
        final NewRepository repository = context.createRepository(
                CheckstyleConstants.REPOSITORY_KEY, "java").setName(
                CheckstyleConstants.REPOSITORY_NAME);
        try {
            extractRulesData(repository, "/org/sonar/plugins/checkstyle/rules.xml",
                    "/org/sonar/l10n/checkstyle/rules/checkstyle");
        }
        catch (IOException ex) {
            throw new IllegalStateException("Exception during extractRulesData", ex);
        }

        repository.done();
    }

    @VisibleForTesting
    static void extractRulesData(NewRepository repository, String xmlRulesFilePath,
            String htmlDescriptionFolder) throws IOException {
        final RulesDefinitionXmlLoader ruleLoader = new RulesDefinitionXmlLoader();
        try (InputStream resource = CheckstyleRulesDefinition.class
                .getResourceAsStream(xmlRulesFilePath)) {
            ruleLoader.load(repository, resource, "UTF-8");
        }
        ExternalDescriptionLoader.loadHtmlDescriptions(repository, htmlDescriptionFolder);
        try (InputStream resource = CheckstyleRulesDefinition.class
                .getResourceAsStream("/org/sonar/l10n/checkstyle.properties")) {
            loadNames(repository, resource);
        }
        SqaleXmlLoader.load(repository, "/com/sonar/sqale/checkstyle-model.xml");
    }

    private static void loadNames(NewRepository repository, InputStream stream) {
        // code taken from:
        // https://github.com/SonarSource/sslr-squid-bridge/blob/2.5.2/
        // src/main/java/org/sonar/squidbridge/rules/PropertyFileLoader.java#L46
        final Properties properties = new Properties();
        try {
            properties.load(stream);
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("Could not read names from properties", ex);
        }

        if (Objects.nonNull(repository.rules())) {
            repository.rules().forEach(rule -> {
                final String baseKey = "rule." + repository.key() + "." + rule.key();
                final String nameKey = baseKey + ".name";
                final String ruleName = properties.getProperty(nameKey);
                if (Objects.nonNull(ruleName)) {
                    rule.setName(ruleName);
                }
                rule.params().forEach(param -> {
                    final String paramDescriptionKey = baseKey + ".param." + param.key();
                    final String paramDescription = properties.getProperty(paramDescriptionKey);
                    if (Objects.nonNull(paramDescription)) {
                        param.setDescription(paramDescription);
                    }
                });
            });
        }
    }
}
