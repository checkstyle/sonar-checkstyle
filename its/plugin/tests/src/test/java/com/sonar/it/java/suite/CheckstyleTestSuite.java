/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.java.suite;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.locator.FileLocation;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  CheckstyleTest.class,
  CheckstyleExtensionsTest.class
})
public class CheckstyleTestSuite {

  @ClassRule
  public static final Orchestrator ORCHESTRATOR;

  static {
    OrchestratorBuilder orchestratorBuilder = Orchestrator.builderEnv()
      .addPlugin("java")
      .addPlugin("checkstyle")
      .setMainPluginKey("checkstyle")
      .addPlugin(FileLocation.of(pluginJar("checkstyle-extension-plugin")))
      .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/CheckstyleExtensionsTest/extension-backup.xml"))
      .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/CheckstyleTest/checkstyle-backup.xml"))
      .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/CheckstyleTest/suppression-comment-filter.xml"))
      .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/CheckstyleTest/suppress-warnings-filter.xml"))
      .restoreProfileAtStartup(FileLocation.ofClasspath("/sonar-way-2.7.xml"));
    ORCHESTRATOR = orchestratorBuilder.build();
  }

  private static File pluginJar(String artifactId) {
    return new File("../plugins/" + artifactId + "/target/" + artifactId + "-1.0-SNAPSHOT.jar");
  }

  public static File projectPom(String projectName) {
    return new File("../projects/" + projectName + "/pom.xml");
  }

  public static boolean isCheckstyleAtLeast_2_3() {
    return ORCHESTRATOR.getConfiguration().getPluginVersion("checkstyle").isGreaterThanOrEquals("2.3");
  }

}
