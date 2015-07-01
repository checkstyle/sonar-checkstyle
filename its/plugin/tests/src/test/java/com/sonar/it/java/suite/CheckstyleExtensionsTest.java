/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.java.suite;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.MavenBuild;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueClient;
import org.sonar.wsclient.issue.IssueQuery;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class CheckstyleExtensionsTest {

  @ClassRule
  public static Orchestrator orchestrator = CheckstyleTestSuite.ORCHESTRATOR;

  @Test
  public void test_checkstyle_extensions() {

    MavenBuild build = MavenBuild.create(CheckstyleTestSuite.projectPom("checkstyle-extension"))
      .setCleanSonarGoals()
      .withoutDynamicAnalysis()
      .setProperty("sonar.profile", "checkstyle-extension");
    orchestrator.executeBuild(build);

    IssueClient issueClient = orchestrator.getServer().wsClient().issueClient();
    List<Issue> issues = issueClient.find(IssueQuery.create()
        .componentRoots("com.sonarsource.it.projects:checkstyle-extension")
        .rules("checkstyle:org.sonar.samples.checkstyle.MethodsCountCheck")).list();
    assertThat(issues).hasSize(1);
    //new rule is registered as we found an issue with it.
    assertThat(issues.get(0).message()).isEqualTo("Too many methods (3) in class");
  }
}
