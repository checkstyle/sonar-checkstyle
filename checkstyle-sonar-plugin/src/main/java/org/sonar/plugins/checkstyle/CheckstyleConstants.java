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

public final class CheckstyleConstants {

    public static final String REPOSITORY_NAME = "Checkstyle";
    public static final String PLUGIN_KEY = "checkstyle";
    public static final String PLUGIN_NAME = REPOSITORY_NAME;
    public static final String REPOSITORY_KEY = PLUGIN_KEY;

    public static final String CHECKER_FILTERS_KEY = "sonar.checkstyle.filters";
    public static final String TREEWALKER_FILTERS_KEY = "sonar.checkstyle.treewalkerfilters";

    public static final String CHECKER_FILTERS_DEFAULT_VALUE =
            "<module name=\"SuppressWarningsFilter\" />";

    public static final String TREEWALKER_FILTERS_DEFAULT_VALUE =
            "<module name=\"SuppressionCommentFilter\" />";

    public static final String JAVA_KEY = "java";

    private CheckstyleConstants() {
    }
}
