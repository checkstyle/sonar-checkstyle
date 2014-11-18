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

import com.google.common.collect.Iterables;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;

import java.io.File;

public class CheckstyleSensor implements Sensor {

  private final RulesProfile profile;
  private final CheckstyleExecutor executor;
  private final FileSystem fs;

  public CheckstyleSensor(RulesProfile profile, CheckstyleExecutor executor, FileSystem fs) {
    this.profile = profile;
    this.executor = executor;
    this.fs = fs;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    FilePredicates predicates = fs.predicates();
    Iterable<File> mainFiles = fs.files(predicates.and(
      predicates.hasLanguage(CheckstyleConstants.JAVA_KEY),
      predicates.hasType(Type.MAIN)));
    return !Iterables.isEmpty(mainFiles) &&
        !profile.getActiveRulesByRepository(CheckstyleConstants.REPOSITORY_KEY).isEmpty();
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    executor.execute();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
