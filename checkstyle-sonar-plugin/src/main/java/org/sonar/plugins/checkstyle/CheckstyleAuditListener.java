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

import com.google.common.annotations.VisibleForTesting;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

/**
 * @since 2.3
 */
public class CheckstyleAuditListener implements AuditListener, BatchExtension {

    private static final Logger LOG = LoggerFactory.getLogger(CheckstyleAuditListener.class);

    private final RuleFinder ruleFinder;
    private final FileSystem fileSystem;
    private final ResourcePerspectives perspectives;
    private InputFile currentResource;

    public CheckstyleAuditListener(RuleFinder ruleFinder, FileSystem fileSystem,
            ResourcePerspectives perspectives) {
        this.ruleFinder = ruleFinder;
        this.fileSystem = fileSystem;
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
        final String ruleKey = getRuleKey(event);
        if (ruleKey != null) {
            final String message = getMessage(event);
            // In Checkstyle 5.5 exceptions are reported as an events from
            // TreeWalker
            if ("com.puppycrawl.tools.checkstyle.TreeWalker".equals(ruleKey)) {
                LOG.warn("{} : {}", event.getFileName(), message);
            }
            initResource(event);
            final Issuable issuable = perspectives.as(Issuable.class, currentResource);
            final Rule rule = ruleFinder.findByKey(CheckstyleConstants.REPOSITORY_KEY, ruleKey);
            if (rule != null && issuable != null) {
                final IssueBuilder issueBuilder = issuable.newIssueBuilder().ruleKey(rule.ruleKey())
                        .message(message).line(getLineId(event));
                issuable.addIssue(issueBuilder.build());
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
        try {
            return event.getMessage();

        }
        catch (Exception ex) {
            LOG.warn("AuditEvent is created incorrectly. Exception happen during getMessage()", ex);
            return null;
        }
    }

    @VisibleForTesting
    static Integer getLineId(AuditEvent event) {
        Integer result = null;
        try {
            final int line = event.getLine();
            // checkstyle returns 0 if there is no relation to a file content,
            // but we use null
            if (line != 0) {
                result = line;
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
