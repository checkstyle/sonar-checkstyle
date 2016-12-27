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

  private File file = new File("file1");
  private AuditEvent event =
    new AuditEvent(this, file.getAbsolutePath(), new LocalizedMessage(42, "", "", null, "", CheckstyleAuditListenerTest.class, "msg"));
  private DefaultFileSystem fs = new DefaultFileSystem(new File(""));
  private RuleFinder ruleFinder = mock(RuleFinder.class);
  private DefaultInputFile inputFile = new DefaultInputFile("", file.getPath());
  private ResourcePerspectives perspectives = mock(ResourcePerspectives.class);

  @Before
  public void before() {
    //inputFile.setAbsolutePath(file.getAbsolutePath());
    fs.add(inputFile);
  }

  @Test
  public void testUtilityMethods() {
    AuditEvent event;

    event = new AuditEvent(this, "", new LocalizedMessage(0, "", "", null, "", CheckstyleAuditListenerTest.class, "msg"));
    assertThat(CheckstyleAuditListener.getLineId(event)).isNull();
    assertThat(CheckstyleAuditListener.getMessage(event)).isEqualTo("msg");
    assertThat(CheckstyleAuditListener.getRuleKey(event)).isEqualTo(CheckstyleAuditListenerTest.class.getName());

    event = new AuditEvent(this, "", new LocalizedMessage(1, "", "", null, "", CheckstyleAuditListenerTest.class, "msg"));
    assertThat(CheckstyleAuditListener.getLineId(event)).isEqualTo(1);
    assertThat(CheckstyleAuditListener.getMessage(event)).isEqualTo("msg");
    assertThat(CheckstyleAuditListener.getRuleKey(event)).isEqualTo(CheckstyleAuditListenerTest.class.getName());

    event = new AuditEvent(this);
    assertThat(CheckstyleAuditListener.getLineId(event)).isNull();
    assertThat(CheckstyleAuditListener.getMessage(event)).isNull();
    assertThat(CheckstyleAuditListener.getRuleKey(event)).isNull();
  }


  @Test
  public void addErrorTest() {
    Rule rule = setupRule("repo", "key");

    Issuable issuable = setupIssuable();
    IssueBuilder issueBuilder = mock(IssueBuilder.class);
    Issue issue = mock(Issue.class);
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
    Issuable issuable = setupIssuable();
    addErrorToListener();
    verifyZeroInteractions(issuable);
  }

  @Test
  public void addErrorOnUnknownFile() {
    Rule rule = setupRule("repo", "key");
    addErrorToListener();
    verifyZeroInteractions(rule);
  }

  private CheckstyleAuditListener addErrorToListener() {
    CheckstyleAuditListener listener = new CheckstyleAuditListener(ruleFinder, fs, perspectives);
    listener.addError(event);
    return listener;
  }

  private Rule setupRule(String repo, String key) {
    Rule rule = mock(Rule.class);
    when(rule.ruleKey()).thenReturn(RuleKey.of(repo, key));
    when(ruleFinder.findByKey(CheckstyleConstants.REPOSITORY_KEY, CheckstyleAuditListenerTest.class.getCanonicalName()))
      .thenReturn(rule);
    return rule;
  }

  private Issuable setupIssuable() {
    Issuable issuable = mock(Issuable.class);
    when(perspectives.as(Issuable.class, inputFile)).thenReturn(issuable);
    return issuable;
  }
}
