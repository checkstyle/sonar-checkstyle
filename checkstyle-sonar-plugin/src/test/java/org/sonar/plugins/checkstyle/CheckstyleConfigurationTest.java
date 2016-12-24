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

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static org.fest.assertions.Assertions.assertThat;

public class CheckstyleConfigurationTest {

  private DefaultFileSystem fileSystem;

  @Before
  public void beforeClass() {
    fileSystem = new DefaultFileSystem(new File(""));
    DefaultInputFile inputFile = new DefaultInputFile("mainFile");
    inputFile.setAbsolutePath("mainFile");
    inputFile.setLanguage("java");
    inputFile.setType(InputFile.Type.MAIN);
    fileSystem.add(inputFile);
    DefaultInputFile testFile = new DefaultInputFile("testFile");
    testFile.setAbsolutePath("testFile");
    testFile.setLanguage("java");
    testFile.setType(InputFile.Type.TEST);
    fileSystem.add(testFile);
  }

  @Test
  public void getSourceFiles() {
    CheckstyleProfileExporter exporter = new FakeExporter();
    CheckstyleConfiguration configuration = new CheckstyleConfiguration(null, exporter, null, fileSystem);
    assertThat(configuration.getSourceFiles()).hasSize(1);
    assertThat(configuration.getSourceFiles().iterator().next().toString()).contains("mainFile");
  }

  @Test
  public void getTargetXMLReport() {
    Settings conf = new Settings();
    CheckstyleConfiguration configuration = new CheckstyleConfiguration(conf, null, null, fileSystem);
    assertThat(configuration.getTargetXMLReport()).isNull();

    conf.setProperty(CheckstyleConfiguration.PROPERTY_GENERATE_XML, "true");
    configuration = new CheckstyleConfiguration(conf, null, null, fileSystem);
    assertThat(configuration.getTargetXMLReport()).isEqualTo(new File(fileSystem.workDir(), "checkstyle-result.xml"));
  }

  @Test
  public void writeConfigurationToWorkingDir() throws IOException {
    CheckstyleProfileExporter exporter = new FakeExporter();
    CheckstyleConfiguration configuration = new CheckstyleConfiguration(null, exporter, null, fileSystem);
    File xmlFile = configuration.getXMLDefinitionFile();

    assertThat(xmlFile.exists()).isTrue();
    assertThat(FileUtils.readFileToString(xmlFile)).isEqualTo("<conf/>");
    FileUtils.forceDelete(xmlFile);
  }

  @Test
  public void getCheckstyleConfiguration() throws IOException, CheckstyleException {
    fileSystem.setEncoding(StandardCharsets.UTF_8);
    Settings settings = new Settings(new PropertyDefinitions(new CheckstylePlugin().getExtensions()));
    settings.setProperty(CheckstyleConstants.FILTERS_KEY, CheckstyleConstants.FILTERS_DEFAULT_VALUE);

    RulesProfile profile = RulesProfile.create("sonar way", "java");

    Rule rule = Rule.create("checkstyle", "CheckStyleRule1", "checkstyle rule one");
    rule.setConfigKey("checkstyle/rule1");
    profile.activateRule(rule, null);

    CheckstyleConfiguration configuration = new CheckstyleConfiguration(settings, new CheckstyleProfileExporter(settings), profile, fileSystem);
    Configuration checkstyleConfiguration = configuration.getCheckstyleConfiguration();
    assertThat(checkstyleConfiguration).isNotNull();
    assertThat(checkstyleConfiguration.getAttribute("charset")).isEqualTo("UTF-8");
    File xmlFile = new File("checkstyle.xml");
    assertThat(xmlFile.exists()).isTrue();

    FileUtils.forceDelete(xmlFile);
  }

  public class FakeExporter extends CheckstyleProfileExporter {

    public FakeExporter() {
      super(new Settings());
    }

    @Override
    public void exportProfile(RulesProfile profile, Writer writer) {
      try {
        writer.write("<conf/>");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
