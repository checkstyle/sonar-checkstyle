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

package org.sonar.plugins.checkstyle.rule;

import java.util.Map;

/**
 * Due to our usage of {@link org.sonar.plugins.checkstyle.CheckstyleProfileExporter}
 * on the {@link org.sonar.api.batch.ScannerSide} for generating CheckStyle configurations,
 * we must make it compatible for the new {@link org.sonar.api.batch.rule.ActiveRules}.
 *
 * Hence, to gain compatibility with server and scanner side in the exporter,
 * we have to wrap either {@link org.sonar.api.batch.rule.ActiveRule} or
 * {@link org.sonar.api.rules.ActiveRule} for usage in the exporter.
 */
public interface ActiveRuleWrapper {
    String getInternalKey();

    String getRuleKey();

    String getTemplateRuleKey();

    String getSeverity();

    Map<String, String> getParams();
}
