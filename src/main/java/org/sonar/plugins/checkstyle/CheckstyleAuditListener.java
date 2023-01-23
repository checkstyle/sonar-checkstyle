////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2023 the original author or authors.
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

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import com.google.common.annotations.VisibleForTesting;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

/**
 * Custom Checkstyle Listener which captures the output of Checkstyle and
 * converts it into sonar's format.
 *
 * @since 2.3
 */
@ExtensionPoint
@ScannerSide
public class CheckstyleAuditListener implements AuditListener {

    private static final Logger LOG = LoggerFactory.getLogger(CheckstyleAuditListener.class);

    private final ActiveRules ruleFinder;
    private final FileSystem fileSystem;

    private InputFile currentResource;
    private SensorContext context;

    public CheckstyleAuditListener(ActiveRules ruleFinder, FileSystem fileSystem) {
        this.ruleFinder = ruleFinder;
        this.fileSystem = fileSystem;
    }

    /**
     * Sets the sensor context for the listener.
     *
     * @param context The context.
     */
    public void setContext(SensorContext context) {
        this.context = context;
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
        final String ruleKey = getRuleKey(event);

        if (Objects.nonNull(ruleKey)) {
            final String message = getMessage(event);
            // In Checkstyle 5.5 exceptions are reported as an events from
            // TreeWalker
            if ("com.puppycrawl.tools.checkstyle.TreeWalker".equals(ruleKey)) {
                LOG.warn("{} : {}", event.getFileName(), message);
            }

            initResource(event);

            final NewIssue issue = context.newIssue();
            final ActiveRule rule = ruleFinder.find(
                    RuleKey.of(CheckstyleConstants.REPOSITORY_KEY, ruleKey));
            if (Objects.nonNull(issue) && Objects.nonNull(rule)) {
                final NewIssueLocation location = issue.newLocation()
                        .on(currentResource)
                        .at(currentResource.selectLine(getLineId(event)))
                        .message(message);
                issue.forRule(rule.ruleKey())
                        .at(location)
                        .save();
            }
        }
    }

    private void initResource(AuditEvent event) {
        if (currentResource == null) {
            final String absoluteFilename = event.getFileName();
            currentResource = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(
                    absoluteFilename));
        }
    }

    @VisibleForTesting
    static String getRuleKey(AuditEvent event) {
        String key = null;
        try {
            key = event.getModuleId();
        }
        catch (Exception ex) {
            LOG.warn("AuditEvent is created incorrectly. Exception happen during getModuleId()",
                    ex);
        }
        if (StringUtils.isBlank(key)) {
            try {
                key = event.getSourceName();
            }
            catch (Exception ex) {
                LOG.warn("AuditEvent is created incorrectly."
                        + "Exception happen during getSourceName()", ex);
            }
        }
        return key;
    }

    @VisibleForTesting
    static String getMessage(AuditEvent event) {
        String result;
        try {
            result = event.getMessage();

        }
        catch (Exception ex) {
            LOG.warn("AuditEvent is created incorrectly. Exception happen during getMessage()", ex);
            result = null;
        }
        return result;
    }

    @VisibleForTesting
    static int getLineId(AuditEvent event) {
        int result = 1;
        try {
            final int eventLine = event.getLine();
            if (eventLine > 0) {
                result = eventLine;
            }
        }
        catch (Exception ex) {
            LOG.warn("AuditEvent is created incorrectly. Exception happen during getLine()", ex);
        }
        return result;
    }

    /**
     * Note that this method never invoked from Checkstyle 5.5.
     */
    @Override
    public void addException(AuditEvent event, Throwable throwable) {
        // nop
    }

}
