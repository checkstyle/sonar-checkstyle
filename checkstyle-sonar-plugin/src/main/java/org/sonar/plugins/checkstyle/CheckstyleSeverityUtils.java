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
        switch (priority) {
            case BLOCKER:
            case CRITICAL:
                return SeverityLevel.ERROR.getName();
            case MAJOR:
                return SeverityLevel.WARNING.getName();
            case MINOR:
            case INFO:
                return SeverityLevel.INFO.getName();
            default:
                throw new IllegalArgumentException("Priority not supported: " + priority);
        }
    }

    public static RulePriority fromSeverity(String severity) {
        SeverityLevel severityLevel;
        try {
            severityLevel = SeverityLevel.getInstance(severity);
        }
        catch (Exception exc) {
            LOG.warn("Smth wrong severity", exc);
            return null;
        }
        switch (severityLevel) {
            case ERROR:
                return RulePriority.BLOCKER;
            case WARNING:
                return RulePriority.MAJOR;
            case INFO:
            case IGNORE:
                return RulePriority.INFO;
            default:
                return null;
        }
    }
}
