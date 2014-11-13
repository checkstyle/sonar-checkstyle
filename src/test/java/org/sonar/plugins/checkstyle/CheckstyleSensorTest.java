/*
 * SonarQube Checkstyle Plugin
 * Copyright (C) 2012 SonarSource
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
package org.sonar.plugins.checkstyle;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.ActiveRule;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckstyleSensorTest {

  private RulesProfile profile = mock(RulesProfile.class);
  private CheckstyleSensor sensor = new CheckstyleSensor(profile, null);

  private Project project = new Project("projectKey");
  private ProjectFileSystem fs = mock(ProjectFileSystem.class);

  @Before
  public void before() {
    project.setFileSystem(fs);
  }

  @Test
  public void shouldExecuteOnProject_without_java_file_and_with_rule() throws Exception {
    addOneActiveRule();
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void shouldExecuteOnProject_with_java_file_and_without_rule() throws Exception {
    addOneJavaFile();
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void shouldExecuteOnProject_with_java_files_and_rules() throws Exception {
    addOneJavaFile();
    addOneActiveRule();
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  private void addOneJavaFile() {
    when(fs.mainFiles("java")).thenReturn(ImmutableList.of(mock(InputFile.class)));
  }

  private void addOneActiveRule() {
    when(profile.getActiveRulesByRepository("checkstyle")).thenReturn(ImmutableList.of(mock(ActiveRule.class)));
  }

}
