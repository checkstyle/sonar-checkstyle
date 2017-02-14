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
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;

import com.google.common.annotations.VisibleForTesting;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

public class CheckstyleConfiguration implements BatchExtension {

    private static final Logger LOG = LoggerFactory.getLogger(CheckstyleConfiguration.class);
    public static final String PROPERTY_GENERATE_XML = "sonar.checkstyle.generateXml";

    private final CheckstyleProfileExporter confExporter;
    private final RulesProfile profile;
    private final Settings conf;
    private final FileSystem fileSystem;

    public CheckstyleConfiguration(Settings conf, CheckstyleProfileExporter confExporter,
            RulesProfile profile, FileSystem fileSystem) {
        this.conf = conf;
        this.confExporter = confExporter;
        this.profile = profile;
        this.fileSystem = fileSystem;
    }

    public File getXmlDefinitionFile() {
        File xmlFile = new File(fileSystem.workDir(), "checkstyle.xml");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(xmlFile, false),
                StandardCharsets.UTF_8)) {

            confExporter.exportProfile(profile, writer);
            writer.flush();
            return xmlFile;

        }
        catch (IOException e) {
            throw new IllegalStateException("Fail to save the Checkstyle configuration to "
                    + xmlFile.getPath(), e);

        }
    }

    public List<File> getSourceFiles() {
        FilePredicates predicates = fileSystem.predicates();
        Iterable<File> files = fileSystem.files(predicates.and(
                predicates.hasLanguage(CheckstyleConstants.JAVA_KEY),
                predicates.hasType(InputFile.Type.MAIN)));
        List<File> fileList = new ArrayList<>();
        for (File file : files) {
            fileList.add(file);
        }
        return fileList;
    }

    public File getTargetXmlReport() {
        if (conf.getBoolean(PROPERTY_GENERATE_XML)) {
            return new File(fileSystem.workDir(), "checkstyle-result.xml");
        }
        return null;
    }

    public Configuration getCheckstyleConfiguration() throws CheckstyleException {
        File xmlConfig = getXmlDefinitionFile();

        LOG.info("Checkstyle configuration: {}", xmlConfig.getAbsolutePath());
        Configuration configuration = toCheckstyleConfiguration(xmlConfig);
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
        if (("Checker".equals(module.getName()) || "com.puppycrawl.tools.checkstyle.Checker"
                .equals(module.getName())) && module instanceof DefaultConfiguration) {
            Charset charset = getCharset();
            String charsetName = charset.name();
            LOG.info("Checkstyle charset: {}", charsetName);
            ((DefaultConfiguration) module).addAttribute("charset", charsetName);
        }
    }

    public Charset getCharset() {
        return fileSystem.encoding();
    }

}
