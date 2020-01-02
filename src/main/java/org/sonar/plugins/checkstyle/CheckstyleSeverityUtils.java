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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rules.RulePriority;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

public final class CheckstyleSeverityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CheckstyleSeverityUtils.class);

    private CheckstyleSeverityUtils() {
        // only static methods
    }

    public static String toSeverity(RulePriority priority) {
        return toSeverity(priority.name());
    }

    public static String toSeverity(String priority) {
        final String result;

        switch (priority) {
            case "BLOCKER":
            case "CRITICAL":
                result = SeverityLevel.ERROR.getName();
                break;
            case "MAJOR":
                result = SeverityLevel.WARNING.getName();
                break;
            case "MINOR":
            case "INFO":
                result = SeverityLevel.INFO.getName();
                break;
            default:
                throw new IllegalArgumentException("Priority not supported: " + priority);
        }

        return result;
    }

    public static RulePriority fromSeverity(String severity) {
        RulePriority result = null;

        try {
            final SeverityLevel severityLevel = SeverityLevel.getInstance(severity);

            switch (severityLevel) {
                case ERROR:
                    result = RulePriority.BLOCKER;
                    break;
                case WARNING:
                    result = RulePriority.MAJOR;
                    break;
                case INFO:
                case IGNORE:
                    result = RulePriority.INFO;
                    break;
                default:
            }
        }
        catch (Exception exc) {
            LOG.warn("Smth wrong severity", exc);
        }

        return result;
    }
}
