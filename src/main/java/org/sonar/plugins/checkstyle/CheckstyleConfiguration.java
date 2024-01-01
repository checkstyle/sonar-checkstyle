////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2024 the original author or authors.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;

import com.google.common.annotations.VisibleForTesting;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

@ExtensionPoint
@ScannerSide
public class CheckstyleConfiguration {
    public static final String PROPERTY_GENERATE_XML = "sonar.checkstyle.generateXml";

    private static final Logger LOG = LoggerFactory.getLogger(CheckstyleConfiguration.class);

    private final CheckstyleProfileExporter confExporter;
    private final ActiveRules activeRules;
    private final org.sonar.api.config.Configuration conf;
    private final FileSystem fileSystem;

    public CheckstyleConfiguration(
            org.sonar.api.config.Configuration conf,
            CheckstyleProfileExporter confExporter,
            ActiveRules activeRules,
            FileSystem fileSystem) {
        this.conf = conf;
        this.confExporter = confExporter;
        this.activeRules = activeRules;
        this.fileSystem = fileSystem;
    }

    /**
     * Creates and retrieves the Checkstyle configuration file with the rules from sonar.
     *
     * @return The location of the created Checkstyle configuration file.
     * @throws IllegalStateException if the Checkstyle configuration file failed to save.
     */
    public File getXmlDefinitionFile() {
        final File xmlFile = new File(fileSystem.workDir(), "checkstyle.xml");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(xmlFile, false),
                StandardCharsets.UTF_8)) {
            confExporter.exportProfile(activeRules, writer);
            writer.flush();
            return xmlFile;
        }
        catch (IOException ex) {
            throw new IllegalStateException("Fail to save the Checkstyle configuration to "
                    + xmlFile.getPath(), ex);

        }
    }

    /**
     * Obtains the list of input source files for Checkstyle to run against.
     *
     * @return The list of source files.
     */
    public List<InputFile> getSourceFiles() {
        final FilePredicates predicates = fileSystem.predicates();
        final Iterable<InputFile> files = fileSystem.inputFiles(predicates.and(
                predicates.hasLanguage(CheckstyleConstants.JAVA_KEY),
                predicates.hasType(InputFile.Type.MAIN)));
        final List<InputFile> fileList = new ArrayList<>();
        for (InputFile file : files) {
            fileList.add(file);
        }
        return fileList;
    }

    /**
     * Obtains the file location of the xml report from Checkstyle.
     * This location is only valid if {@link #PROPERTY_GENERATE_XML}
     * is turned on.
     *
     * @return The file location or {@code null}.
     */
    public File getTargetXmlReport() {
        return conf.getBoolean(PROPERTY_GENERATE_XML)
                .map(aBoolean -> new File(fileSystem.workDir(), "checkstyle-result.xml"))
                .orElse(null);
    }

    /**
     * Generates the checkstyle configuration with the rules from sonar.
     *
     * @return The Checkstyle configuration.
     * @throws CheckstyleException if there is an error  generating the Checkstyle configuration.
     */
    public Configuration getCheckstyleConfiguration() throws CheckstyleException {
        final File xmlConfig = getXmlDefinitionFile();

        LOG.info("Checkstyle configuration: {}", xmlConfig.getAbsolutePath());
        final Configuration configuration = toCheckstyleConfiguration(xmlConfig);
        defineCharset(configuration);
        return configuration;
    }

    @VisibleForTesting
    static Configuration toCheckstyleConfiguration(File xmlConfig) throws CheckstyleException {
        return ConfigurationLoader.loadConfiguration(xmlConfig.getAbsolutePath(),
                new PropertiesExpander(new Properties()));
    }

    private void defineCharset(Configuration configuration) {
        defineModuleCharset(configuration);
        for (Configuration module : configuration.getChildren()) {
            defineModuleCharset(module);
        }
    }

    private void defineModuleCharset(Configuration module) {
        if (module instanceof DefaultConfiguration
                && ("Checker".equals(module.getName())
                        || "com.puppycrawl.tools.checkstyle.Checker".equals(module.getName()))) {
            final Charset charset = getCharset();
            final String charsetName = charset.name();
            LOG.info("Checkstyle charset: {}", charsetName);
            ((DefaultConfiguration) module).addProperty("charset", charsetName);
        }
    }

    /**
     * Retrieves the charset of the underlying file system.
     *
     * @return The charset.
     */
    public Charset getCharset() {
        return fileSystem.encoding();
    }

}
