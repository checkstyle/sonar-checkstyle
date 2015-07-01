/*
 * Checkstyle :: IT :: Plugin :: Tests
 * Copyright (C) 2013 SonarSource
 * dev@sonar.codehaus.org
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
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.MavenBuild;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueClient;
import org.sonar.wsclient.issue.IssueQuery;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class CheckstyleTest {

  @ClassRule
  public static Orchestrator orchestrator = CheckstyleTestSuite.ORCHESTRATOR;

  @Before
  public void clear() {
    orchestrator.resetData();
  }

  @Test
  public void testCheckstyleSuppressionCommentFilter() {
    MavenBuild build = MavenBuild.create(CheckstyleTestSuite.projectPom("checkstyle-suppression-comment-filter"))
      .setCleanSonarGoals()
      .setProperty("sonar.dynamicAnalysis", "false")
      .setProperty("sonar.profile", "checkstyle-suppression-comment-filter");
    orchestrator.executeBuild(build);

    Resource project = orchestrator.getServer().getWsClient()
      .find(ResourceQuery.createForMetrics("com.sonarsource.it.projects:checkstyle-suppression-comment-filter", "violations"));

    // there should be 2 violations, but one is disabled
    assertThat(project.getMeasureIntValue("violations")).isEqualTo(1);
  }

  @Test
  public void testCheckstyleSuppressWarningsFilter() {
    MavenBuild build = MavenBuild.create(CheckstyleTestSuite.projectPom("checkstyle-suppress-warnings-filter"))
      .setCleanSonarGoals()
      .setProperty("sonar.dynamicAnalysis", "false")
      .setProperty("sonar.profile", "checkstyle-suppress-warnings-filter");
    orchestrator.executeBuild(build);

    Resource project = orchestrator.getServer().getWsClient()
      .find(ResourceQuery.createForMetrics("com.sonarsource.it.projects:checkstyle-suppress-warnings-filter", "violations"));

    // there should be 4 violations, but 3 are disabled using @SuppressWarnings
    int nbViolations = 4;
    if (CheckstyleTestSuite.isCheckstyleAtLeast_2_3()) {
      nbViolations = 1;
    }
    assertThat(project.getMeasureIntValue("violations")).isEqualTo(nbViolations);
  }

  /**
   * SONAR-3031
   */
  @Test
  public void testCheckstyleError() {
    MavenBuild build = MavenBuild.create(CheckstyleTestSuite.projectPom("checkstyle-error"))
      .setCleanSonarGoals()
      .setProperty("sonar.dynamicAnalysis", "false")
      .setProperty("sonar.profile", "sonar-way-2.7");
    BuildResult result = orchestrator.executeBuild(build);

    assertThat(result.getLogs()).contains("expecting IDENT, found '.'");
  }

  @Test
  public void ruleIllegalThrows() {
    MavenBuild build = MavenBuild.create(CheckstyleTestSuite.projectPom("checkstyle-illegal-throws"))
      .setCleanSonarGoals()
      .setProperty("sonar.dynamicAnalysis", "false")
      .setProperty("sonar.profile", "illegal-throws");
    orchestrator.executeBuild(build);

    Resource project = orchestrator.getServer().getWsClient().find(ResourceQuery.createForMetrics("com.sonarsource.it.projects:checkstyle-illegal-throws", "violations"));
    assertThat(project.getMeasureIntValue("violations")).isEqualTo(1);
  }

  @Test
  public void checkstyle_should_use_selected_locale() {
    MavenBuild build = MavenBuild.create(CheckstyleTestSuite.projectPom("checkstyle-illegal-throws"))
      .setCleanSonarGoals()
      .setProperty("sonar.dynamicAnalysis", "false")
      .setProperty("sonar.profile", "illegal-throws")
      .setEnvironmentVariable("MAVEN_OPTS", "-Duser.language=fr");
    orchestrator.executeBuild(build);

    IssueClient issueClient = orchestrator.getServer().wsClient().issueClient();
    List<Issue> issues = issueClient.find(IssueQuery.create()
      .componentRoots("com.sonarsource.it.projects:checkstyle-illegal-throws")
      .rules("checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.IllegalThrowsCheck")).list();

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).message()).contains("Throwing 'RuntimeException' is not allowed.");
  }

}
