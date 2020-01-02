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

import java.util.Arrays;
import java.util.List;

import org.sonar.api.CoreProperties;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public final class CheckstylePlugin extends SonarPlugin {

    private static final String CHECKSTYLE_SUB_CATEGORY_NAME = "Checkstyle";

    private static final String DESCRIPTION_HEADER = "Checkstyle supports";
    private static final String FILTERS_DESCRIPTION_FOOTER = "This property"
            + " allows the configuration of those filters with a "
            + "native XML format. See the "
            + "<a href='http://checkstyle.sourceforge.net/config.html'>"
            + "Checkstyle</a> "
            + "configuration for more information.";

    private static final String CHECKER_FILTERS_DESCRIPTION = DESCRIPTION_HEADER
            + " <a href=\"http://checkstyle.sourceforge.net/config_filefilters.html\">file filter"
            + "</a> and several "
            + "<a href=\"http://checkstyle.sourceforge.net/config_filters.html\">"
            + "violation filtering mechanisms</a>: "
            + FILTERS_DESCRIPTION_FOOTER;

    private static final String TREEWALKER_FILTERS_DESCRIPTION = DESCRIPTION_HEADER
            + " <a href=\"http://checkstyle.sourceforge.net/config_filters.html"
            + "#SuppressWithNearbyCommentFilter\">"
            + "SuppressWithNearbyCommentFilter"
            + "</a> and "
            + " <a href=\"http://checkstyle.sourceforge.net/config_filters.html"
            + "#SuppressionCommentFilter\">"
            + "SuppressionCommentFilter</a>: "
            + FILTERS_DESCRIPTION_FOOTER;

    private static final String CHECKER_TAB_WIDTH_DESCRIPTION = DESCRIPTION_HEADER
            + " the <a href=\"https://checkstyle.org/config.html#tabWidth\">"
            + "tabWidth</a> property, representing the number of expanded spaces"
            + "for a tab character ('\\t')."
            + "The Checkstyle default's value is used"
            + " if this property is not set. See the "
            + "<a href='https://checkstyle.org/config.html'>"
            + "Checkstyle</a> "
            + "configuration for more information.";

    @SuppressWarnings("rawtypes")
    @Override
    public List getExtensions() {
        return Arrays
                .asList(PropertyDefinition.builder(CheckstyleConstants.CHECKER_FILTERS_KEY)
                                .defaultValue(CheckstyleConstants.CHECKER_FILTERS_DEFAULT_VALUE)
                                .category(CoreProperties.CATEGORY_JAVA)
                                .subCategory(CHECKSTYLE_SUB_CATEGORY_NAME)
                                .name("Checker Filters")
                                .description(CHECKER_FILTERS_DESCRIPTION)
                                .type(PropertyType.TEXT)
                                .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE).build(),
                        PropertyDefinition.builder(CheckstyleConstants.TREEWALKER_FILTERS_KEY)
                                .defaultValue(CheckstyleConstants.TREEWALKER_FILTERS_DEFAULT_VALUE)
                                .category(CoreProperties.CATEGORY_JAVA)
                                .subCategory(CHECKSTYLE_SUB_CATEGORY_NAME)
                                .name("Treewalker Filters")
                                .description(TREEWALKER_FILTERS_DESCRIPTION)
                                .type(PropertyType.TEXT)
                                .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE).build(),
                        PropertyDefinition.builder(CheckstyleConstants.CHECKER_TAB_WIDTH)
                                .category(CoreProperties.CATEGORY_JAVA)
                                .subCategory(CHECKSTYLE_SUB_CATEGORY_NAME)
                                .name("Tab Width")
                                .description(CHECKER_TAB_WIDTH_DESCRIPTION)
                                .type(PropertyType.INTEGER)
                                .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
                                .build(),
                        PropertyDefinition.builder(CheckstyleConfiguration.PROPERTY_GENERATE_XML)
                                .defaultValue("false").category(CoreProperties.CATEGORY_JAVA)
                                .subCategory(CHECKSTYLE_SUB_CATEGORY_NAME)
                                .name("Generate XML Report").type(PropertyType.BOOLEAN).hidden()
                                .build(),

                        CheckstyleSensor.class, CheckstyleConfiguration.class,
                        CheckstyleExecutor.class, CheckstyleAuditListener.class,
                        CheckstyleProfileExporter.class, CheckstyleProfileImporter.class,
                        CheckstyleRulesDefinition.class);
    }

}
