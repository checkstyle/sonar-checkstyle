////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2024 the original author or authors.
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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.DefaultTextRange;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.Violation;

public class CheckstyleAuditListenerTest {

    private final File file = new File("file1");
    private final AuditEvent event = new AuditEvent(this, file.getAbsolutePath(),
            new Violation(42, "", "", null, "", CheckstyleAuditListenerTest.class, "msg"));
    private ActiveRules ruleFinder;
    private InputFile inputFile;
    private SensorContext context;
    private FileSystem fileSystem;

    @Before
    public void before() {
        fileSystem = mock(FileSystem.class);
        context = mock(SensorContext.class);
        inputFile = mock(InputFile.class);
        ruleFinder = mock(ActiveRules.class);

        final FilePredicates predicates = mock(FilePredicates.class);
        final FilePredicate filePredicate = mock(FilePredicate.class);
        when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(inputFile);
        when(fileSystem.predicates()).thenReturn(predicates);
        when(predicates.hasAbsolutePath(anyString())).thenReturn(filePredicate);
    }

    @Test
    public void testUtilityMethods() {
        final AuditEvent event1 = new AuditEvent(this, "", new Violation(0, "", "", null, "",
                CheckstyleAuditListenerTest.class, "msg"));
        assertThat(CheckstyleAuditListener.getLineId(event1)).isEqualTo(1);
        assertThat(CheckstyleAuditListener.getMessage(event1)).isEqualTo("msg");
        assertThat(CheckstyleAuditListener.getRuleKey(event1)).isEqualTo(
                CheckstyleAuditListenerTest.class.getName());

        final AuditEvent event2 = new AuditEvent(this, "", new Violation(1, "", "", null, "",
                CheckstyleAuditListenerTest.class, "msg"));
        assertThat(CheckstyleAuditListener.getLineId(event2)).isEqualTo(1);
        assertThat(CheckstyleAuditListener.getMessage(event2)).isEqualTo("msg");
        assertThat(CheckstyleAuditListener.getRuleKey(event2)).isEqualTo(
                CheckstyleAuditListenerTest.class.getName());

        final AuditEvent event3 = new AuditEvent(this);
        assertThat(CheckstyleAuditListener.getLineId(event3)).isEqualTo(1);
        assertThat(CheckstyleAuditListener.getMessage(event3)).isNull();
        assertThat(CheckstyleAuditListener.getRuleKey(event3)).isNull();

        final AuditEvent event4 = new AuditEvent(this, "", new Violation(0, "", "", null, "module",
                CheckstyleAuditListenerTest.class, "msg"));
        assertThat(CheckstyleAuditListener.getLineId(event4)).isEqualTo(1);
        assertThat(CheckstyleAuditListener.getMessage(event4)).isEqualTo("msg");
        assertThat(CheckstyleAuditListener.getRuleKey(event4)).isEqualTo("module");
    }

    @Test
    public void addErrorTest() {
        addErrorTestForLine(42);
    }

    @Test
    public void addErrorLine0Test() {
        addErrorTestForLine(0);
    }

    @Test
    public void addErrorLine1Test() {
        addErrorTestForLine(1);
    }

    private void addErrorTestForLine(final int pLineNo) {
        final ActiveRule rule = setupRule("repo", "key");

        final NewIssue newIssue = mock(NewIssue.class);
        final NewIssueLocation newLocation = mock(NewIssueLocation.class);
        when(context.newIssue()).thenReturn(newIssue);
        when(newIssue.newLocation()).thenReturn(newLocation);
        when(newIssue.forRule(rule.ruleKey())).thenReturn(newIssue);
        when(newIssue.at(newLocation)).thenReturn(newIssue);
        when(newLocation.on(any(InputComponent.class))).thenReturn(newLocation);
        when(newLocation.at(any(TextRange.class))).thenReturn(newLocation);
        when(newLocation.message(anyString())).thenReturn(newLocation);

        when(inputFile.selectLine(anyInt())).thenReturn(new DefaultTextRange(
            new DefaultTextPointer(1, 1), new DefaultTextPointer(1, 2)));

        final AuditEvent eventAdded = new AuditEvent(this, file.getAbsolutePath(),
            new Violation(pLineNo, "", "", null, "", CheckstyleAuditListenerTest.class,
                "msg"));
        addErrorToListener(eventAdded);

        verify(newIssue, times(1)).save();
        verify(newIssue, times(1)).forRule(rule.ruleKey());
        verify(newIssue, times(1)).at(newLocation);
        verify(newIssue, times(1)).newLocation();
        verify(newLocation, times(1)).on(any());
        verify(newLocation, times(1)).at(any());
        verify(newLocation, times(1)).message(any());
    }

    @Test
    public void addErrorOnUnknownRule() {
        when(context.newIssue()).thenReturn(null);
        addErrorToListener(event);
        verify(context, times(1)).newIssue();
    }

    @Test
    public void addErrorOnTreeWalkerRule() {
        final AuditEvent treeWalkerEvent = new AuditEvent(
                this,
                file.getAbsolutePath(),
                new Violation(42, "", "", null, "",
                        TreeWalker.class, "msg"));

        when(context.newIssue()).thenReturn(null);
        addErrorToListener(treeWalkerEvent);
        verify(context, times(1)).newIssue();
    }

    @Test
    public void addErrorOnUnknownFile() {
        final ActiveRule rule = setupRule("repo", "key");
        addErrorToListener(event);
        verifyZeroInteractions(rule);
    }

    private CheckstyleAuditListener addErrorToListener(AuditEvent auditEvent) {
        final CheckstyleAuditListener listener = new CheckstyleAuditListener(
                ruleFinder,
                fileSystem);
        listener.setContext(context);
        listener.addError(auditEvent);
        return listener;
    }

    private ActiveRule setupRule(String repo, String key) {
        final ActiveRule rule = mock(ActiveRule.class);
        when(rule.ruleKey()).thenReturn(RuleKey.of(repo, key));
        when(
                ruleFinder.find(RuleKey.of(CheckstyleConstants.REPOSITORY_KEY,
                        CheckstyleAuditListenerTest.class.getCanonicalName()))).thenReturn(rule);
        return rule;
    }
}
