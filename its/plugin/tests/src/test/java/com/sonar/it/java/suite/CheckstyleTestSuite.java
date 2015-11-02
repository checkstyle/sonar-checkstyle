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
  public static final Orchestrator ORCHESTRATOR = Orchestrator.builderEnv()
    .addPlugin("java")
    .addPlugin(FileLocation.of("../../../target/sonar-checkstyle-plugin.jar"))
    .addPlugin(FileLocation.of(pluginJar("checkstyle-extension-plugin")))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/CheckstyleExtensionsTest/extension-backup.xml"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/CheckstyleTest/checkstyle-backup.xml"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/CheckstyleTest/suppression-comment-filter.xml"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/com/sonar/it/java/CheckstyleTest/suppress-warnings-filter.xml"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/sonar-way-2.7.xml"))
    .build();

  private static File pluginJar(String artifactId) {
    return new File("../plugins/" + artifactId + "/target/" + artifactId + "-1.0-SNAPSHOT.jar");
  }

  public static File projectPom(String projectName) {
    return new File("../projects/" + projectName + "/pom.xml");
  }

}
