////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2026 the original author or authors.
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

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

public final class CheckstyleSeverityUtils {

    private CheckstyleSeverityUtils() {
        // only static methods
    }

    public static String toSeverity(String priority) {
        return switch (priority) {
            case "BLOCKER", "CRITICAL" -> SeverityLevel.ERROR.getName();
            case "MAJOR" -> SeverityLevel.WARNING.getName();
            case "MINOR", "INFO" -> SeverityLevel.INFO.getName();
            case null, default ->
                throw new IllegalArgumentException("Priority not supported: " + priority);
        };
    }
}
