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

import org.sonar.api.rules.RulePriority;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

public final class CheckstyleSeverityUtils {

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
    } catch (Exception exc) {
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
