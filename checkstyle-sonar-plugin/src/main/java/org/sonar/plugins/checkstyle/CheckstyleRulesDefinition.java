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

import java.io.IOException;
import java.io.InputStream;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.squidbridge.rules.ExternalDescriptionLoader;
import org.sonar.squidbridge.rules.PropertyFileLoader;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

import com.google.common.annotations.VisibleForTesting;

public final class CheckstyleRulesDefinition implements RulesDefinition {

    @Override
    public void define(Context context) {
        NewRepository repository = context.createRepository(CheckstyleConstants.REPOSITORY_KEY,
                "java").setName(CheckstyleConstants.REPOSITORY_NAME);

        try {
            extractRulesData(repository, "/org/sonar/plugins/checkstyle/rules.xml",
                    "/org/sonar/l10n/checkstyle/rules/checkstyle");
        }
        catch (IOException e) {
            throw new IllegalStateException("Exception during extractRulesData", e);
        }

        repository.done();
    }

    @VisibleForTesting
    static void extractRulesData(NewRepository repository, String xmlRulesFilePath,
            String htmlDescriptionFolder) throws IOException {
        RulesDefinitionXmlLoader ruleLoader = new RulesDefinitionXmlLoader();
        try (InputStream resource = CheckstyleRulesDefinition.class
                .getResourceAsStream(xmlRulesFilePath)) {
            ruleLoader.load(repository, resource, "UTF-8");
        }
        ExternalDescriptionLoader.loadHtmlDescriptions(repository, htmlDescriptionFolder);
        try (InputStream resource = CheckstyleRulesDefinition.class
                .getResourceAsStream("/org/sonar/l10n/checkstyle.properties")) {
            PropertyFileLoader.loadNames(repository, resource);
        }
        SqaleXmlLoader.load(repository, "/com/sonar/sqale/checkstyle-model.xml");
    }
}
