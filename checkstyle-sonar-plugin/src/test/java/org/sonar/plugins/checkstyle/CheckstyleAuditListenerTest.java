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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;

public class CheckstyleAuditListenerTest {

    private final File file = new File("file1");
    private final AuditEvent event = new AuditEvent(this, file.getAbsolutePath(),
            new LocalizedMessage(42, "", "", null, "", CheckstyleAuditListenerTest.class, "msg"));
    private final DefaultFileSystem fileSystem = new DefaultFileSystem(new File(""));
    private final RuleFinder ruleFinder = mock(RuleFinder.class);
    private final DefaultInputFile inputFile = new DefaultInputFile("", file.getPath());
    private final ResourcePerspectives perspectives = mock(ResourcePerspectives.class);

    @Before
    public void before() {
        // inputFile.setAbsolutePath(file.getAbsolutePath());
        fileSystem.add(inputFile);
    }

    @Test
    public void testUtilityMethods() {
        AuditEvent eventTest;

        eventTest = new AuditEvent(this, "", new LocalizedMessage(0, "", "", null, "",
                CheckstyleAuditListenerTest.class, "msg"));
        assertThat(CheckstyleAuditListener.getLineId(eventTest)).isNull();
        assertThat(CheckstyleAuditListener.getMessage(eventTest)).isEqualTo("msg");
        assertThat(CheckstyleAuditListener.getRuleKey(eventTest)).isEqualTo(
                CheckstyleAuditListenerTest.class.getName());

        eventTest = new AuditEvent(this, "", new LocalizedMessage(1, "", "", null, "",
                CheckstyleAuditListenerTest.class, "msg"));
        assertThat(CheckstyleAuditListener.getLineId(eventTest)).isEqualTo(1);
        assertThat(CheckstyleAuditListener.getMessage(eventTest)).isEqualTo("msg");
        assertThat(CheckstyleAuditListener.getRuleKey(eventTest)).isEqualTo(
                CheckstyleAuditListenerTest.class.getName());

        eventTest = new AuditEvent(this);
        assertThat(CheckstyleAuditListener.getLineId(eventTest)).isNull();
        assertThat(CheckstyleAuditListener.getMessage(eventTest)).isNull();
        assertThat(CheckstyleAuditListener.getRuleKey(eventTest)).isNull();
    }

    @Test
    public void addErrorTest() {
        final Rule rule = setupRule("repo", "key");

        final Issuable issuable = setupIssuable();
        final IssueBuilder issueBuilder = mock(IssueBuilder.class);
        final Issue issue = mock(Issue.class);
        when(issuable.newIssueBuilder()).thenReturn(issueBuilder);
        when(issueBuilder.ruleKey(RuleKey.of("repo", "key"))).thenReturn(issueBuilder);
        when(issueBuilder.message(event.getMessage())).thenReturn(issueBuilder);
        when(issueBuilder.line(event.getLine())).thenReturn(issueBuilder);
        when(issueBuilder.build()).thenReturn(issue);

        addErrorToListener();

        verify(issuable).addIssue(issue);
        verify(issueBuilder).ruleKey(RuleKey.of("repo", "key"));
        verify(issueBuilder).message(event.getMessage());
        verify(issueBuilder).line(event.getLine());
        verify(rule).ruleKey();
    }

    @Test
    public void addErrorOnUnknownRule() {
        final Issuable issuable = setupIssuable();
        addErrorToListener();
        verifyZeroInteractions(issuable);
    }

    @Test
    public void addErrorOnUnknownFile() {
        final Rule rule = setupRule("repo", "key");
        addErrorToListener();
        verifyZeroInteractions(rule);
    }

    private CheckstyleAuditListener addErrorToListener() {
        final CheckstyleAuditListener listener = new CheckstyleAuditListener(ruleFinder, fileSystem,
                perspectives);
        listener.addError(event);
        return listener;
    }

    private Rule setupRule(String repo, String key) {
        final Rule rule = mock(Rule.class);
        when(rule.ruleKey()).thenReturn(RuleKey.of(repo, key));
        when(
                ruleFinder.findByKey(CheckstyleConstants.REPOSITORY_KEY,
                        CheckstyleAuditListenerTest.class.getCanonicalName())).thenReturn(rule);
        return rule;
    }

    private Issuable setupIssuable() {
        final Issuable issuable = mock(Issuable.class);
        when(perspectives.as(Issuable.class, inputFile)).thenReturn(issuable);
        return issuable;
    }
}
