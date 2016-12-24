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

import com.google.common.annotations.VisibleForTesting;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;

/**
 * @since 2.3
 */
public class CheckstyleAuditListener implements AuditListener, BatchExtension {

  private static final Logger LOG = LoggerFactory.getLogger(CheckstyleAuditListener.class);

  private final RuleFinder ruleFinder;
  private final FileSystem fs;
  private final ResourcePerspectives perspectives;
  private InputFile currentResource;

  public CheckstyleAuditListener(RuleFinder ruleFinder, FileSystem fs, ResourcePerspectives perspectives) {
    this.ruleFinder = ruleFinder;
    this.fs = fs;
    this.perspectives = perspectives;
  }

  @Override
  public void auditStarted(AuditEvent event) {
    // nop
  }

  @Override
  public void auditFinished(AuditEvent event) {
    // nop
  }

  @Override
  public void fileStarted(AuditEvent event) {
    // nop
  }

  @Override
  public void fileFinished(AuditEvent event) {
    currentResource = null;
  }

  @Override
  public void addError(AuditEvent event) {
    String ruleKey = getRuleKey(event);
    if (ruleKey != null) {
      String message = getMessage(event);
      // In Checkstyle 5.5 exceptions are reported as an events from TreeWalker
      if ("com.puppycrawl.tools.checkstyle.TreeWalker".equals(ruleKey)) {
        LOG.warn(event.getFileName() + ": " + message);
      }
      initResource(event);
      Issuable issuable = perspectives.as(Issuable.class, currentResource);
      Rule rule = ruleFinder.findByKey(CheckstyleConstants.REPOSITORY_KEY, ruleKey);
      if (rule != null && issuable != null) {
        IssueBuilder issueBuilder = issuable.newIssueBuilder()
          .ruleKey(rule.ruleKey())
          .message(message)
          .line(getLineId(event));
        issuable.addIssue(issueBuilder.build());
      }
    }
  }

  private void initResource(AuditEvent event) {
    if (currentResource == null) {
      String absoluteFilename = event.getFileName();
      currentResource = fs.inputFile(fs.predicates().hasAbsolutePath(absoluteFilename));
    }
  }

  @VisibleForTesting
  static String getRuleKey(AuditEvent event) {
    String key = null;
    try {
      key = event.getModuleId();
    } catch (Exception e) {
      // checkstyle throws a NullPointerException if the message is not set
    }
    if (StringUtils.isBlank(key)) {
      try {
        key = event.getSourceName();
      } catch (Exception e) {
        // checkstyle can throw a NullPointerException if the message is not set
      }
    }
    return key;
  }

  @VisibleForTesting
  static String getMessage(AuditEvent event) {
    try {
      return event.getMessage();

    } catch (Exception e) {
      // checkstyle can throw a NullPointerException if the message is not set
      return null;
    }
  }

  @VisibleForTesting
  static Integer getLineId(AuditEvent event) {
    try {
      int line = event.getLine();
      // checkstyle returns 0 if there is no relation to a file content, but we use null
      return line == 0 ? null : line;

    } catch (Exception e) {
      // checkstyle can throw a NullPointerException if the message is not set
      return null;
    }
  }

  /**
   * Note that this method never invoked from Checkstyle 5.5.
   */
  @Override
  public void addException(AuditEvent event, Throwable throwable) {
    // nop
  }

}
