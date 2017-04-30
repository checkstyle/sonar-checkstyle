////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package org.sonar.plugins.checkstyle;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

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

import com.puppycrawl.tools.checkstyle.api.Configuration;

public class CheckstyleConfigurationTest {

    private DefaultFileSystem fileSystem;

    @Before
    public void beforeClass() {
        fileSystem = new DefaultFileSystem(new File(""));
        fileSystem.setWorkDir(new File(""));
        final DefaultInputFile inputFile = new DefaultInputFile("", "mainFile");
        inputFile.setLanguage("java");
        inputFile.setType(InputFile.Type.MAIN);
        fileSystem.add(inputFile);
        final DefaultInputFile testFile = new DefaultInputFile("", "testFile");
        testFile.setLanguage("java");
        testFile.setType(InputFile.Type.TEST);
        fileSystem.add(testFile);
    }

    @Test
    public void getSourceFiles() {
        final CheckstyleProfileExporter exporter = new FakeExporter();
        final CheckstyleConfiguration configuration = new CheckstyleConfiguration(null, exporter,
                null, fileSystem);
        assertThat(configuration.getSourceFiles()).hasSize(1);
        assertThat(configuration.getSourceFiles().iterator().next().toString())
                .contains("mainFile");
    }

    @Test
    public void getTargetXmlReport() {
        final Settings conf = new Settings();
        CheckstyleConfiguration configuration = new CheckstyleConfiguration(conf, null, null,
                fileSystem);
        assertThat(configuration.getTargetXmlReport()).isNull();

        conf.setProperty(CheckstyleConfiguration.PROPERTY_GENERATE_XML, "true");
        configuration = new CheckstyleConfiguration(conf, null, null, fileSystem);
        assertThat(configuration.getTargetXmlReport()).isEqualTo(
                new File(fileSystem.workDir(), "checkstyle-result.xml"));
    }

    @Test
    public void writeConfigurationToWorkingDir() throws IOException {
        final CheckstyleProfileExporter exporter = new FakeExporter();
        final CheckstyleConfiguration configuration = new CheckstyleConfiguration(null, exporter,
                null, fileSystem);
        final File xmlFile = configuration.getXmlDefinitionFile();

        assertThat(xmlFile.exists()).isTrue();
        assertThat(FileUtils.readFileToString(xmlFile)).isEqualTo("<conf/>");
        FileUtils.forceDelete(xmlFile);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getCheckstyleConfiguration() throws Exception {
        fileSystem.setEncoding(StandardCharsets.UTF_8);
        final Settings settings = new Settings(new PropertyDefinitions(
                new CheckstylePlugin().getExtensions()));
        settings.setProperty(CheckstyleConstants.FILTERS_KEY,
                CheckstyleConstants.FILTERS_DEFAULT_VALUE);

        final RulesProfile profile = RulesProfile.create("sonar way", "java");

        final Rule rule = Rule.create("checkstyle", "CheckStyleRule1", "checkstyle rule one");
        rule.setConfigKey("checkstyle/rule1");
        profile.activateRule(rule, null);

        final CheckstyleConfiguration configuration = new CheckstyleConfiguration(settings,
                new CheckstyleProfileExporter(settings), profile, fileSystem);
        final Configuration checkstyleConfiguration = configuration.getCheckstyleConfiguration();
        assertThat(checkstyleConfiguration).isNotNull();
        assertThat(checkstyleConfiguration.getAttribute("charset")).isEqualTo("UTF-8");
        final File xmlFile = new File("checkstyle.xml");
        assertThat(xmlFile.exists()).isTrue();

        FileUtils.forceDelete(xmlFile);
    }

    /* default */static class FakeExporter extends CheckstyleProfileExporter {

        FakeExporter() {
            super(new Settings());
        }

        @Override
        public void exportProfile(RulesProfile profile, Writer writer) {
            try {
                writer.write("<conf/>");
            }
            catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

}
