/*
 * Checkstyle :: IT :: Plugin :: Tests
 * Copyright (C) 2013 SonarSource
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
    List<Issue> issues = issueClient.find(
      IssueQuery.create()
        .componentRoots("com.sonarsource.it.projects:checkstyle-extension")
        .rules("checkstyle:org.sonar.samples.checkstyle.MethodsCountCheck")).list();
    assertThat(issues).hasSize(1);
    //new rule is registered as we found an issue with it.
    assertThat(issues.get(0).message()).isEqualTo("Too many methods (3) in class");
  }
}
