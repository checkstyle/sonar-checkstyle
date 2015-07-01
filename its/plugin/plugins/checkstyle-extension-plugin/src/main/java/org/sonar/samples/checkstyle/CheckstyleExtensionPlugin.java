/*
 * Copyright (C) 2009-2012 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.samples.checkstyle;

import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

public final class CheckstyleExtensionPlugin extends SonarPlugin {
  public List getExtensions() {
    return Arrays.asList(CheckstyleExtensionRepository.class);
  }
}
