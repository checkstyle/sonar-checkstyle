////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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

package org.checkstyle.plugins.sonar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.Build;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.MavenBuild;
import com.sonar.orchestrator.container.Edition;
import com.sonar.orchestrator.container.Server;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;

/**
 * Integration testing of plugin jar inside of sonar.
 */
public class RunPluginTest {
    private static final Logger LOG = LoggerFactory.getLogger(RunPluginTest.class);
    private static final String SONAR_APP_VERSION = "9.0.1.46107";
    private static final int LOGS_NUMBER_LINES = 200;
    private static final String TRUE = "true";
    private static final String PROJECT_KEY = "com.puppycrows.tools:checkstyle";
    private static final String PROJECT_NAME = "integration-test-project";

    private static Orchestrator orchestrator;

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    @BeforeClass
    public static void beforeAll() {
        orchestrator = Orchestrator.builderEnv()
                .setZipFile(new File("target/temp-downloads/sonar-application-"
                        + SONAR_APP_VERSION
                        + ".zip"))
                .setEdition(Edition.COMMUNITY)
                .addPlugin(FileLocation.byWildcardMavenFilename(new File("target"),
                        "checkstyle-sonar-plugin-*.jar"))
                .addPlugin(MavenLocation.of("org.sonarsource.sonar-lits-plugin",
                        "sonar-lits-plugin",
                        "0.10.0.2181"))
                .setServerProperty("sonar.web.javaOpts", "-Xmx1G")
                .build();

        orchestrator.start();
    }

    @AfterClass
    public static void afterAll() {
        orchestrator.stop();
    }

    @Test
    public void testSonarExecution() {
        try {
            final MavenBuild build = testProjectBuild();
            executeBuildWithCommonProperties(build, true);
        }
        catch (IOException exception) {
            LOG.error("Build execution error.", exception);
            fail("Failed to execute build.");
        }
    }

    private static void executeBuildWithCommonProperties(Build<?> build, boolean buildQuietly)
            throws IOException {
        final String dumpOldLocation = FileLocation.of("target/old/" + PROJECT_NAME)
                .getFile()
                .getAbsolutePath();
        final String dumpNewLocation = FileLocation.of("target/actual/" + PROJECT_NAME)
                .getFile()
                .getAbsolutePath();
        build
                .setProperty("sonar.login", "admin")
                .setProperty("sonar.password", "admin")
                .setProperty("sonar.cpd.exclusions", "**/*")
                .setProperty("sonar.import_unknown_files", TRUE)
                .setProperty("sonar.skipPackageDesign", TRUE)
                .setProperty("dump.old", dumpOldLocation)
                .setProperty("dump.new", dumpNewLocation)
                .setProperty("lits.differences", litsDifferencesPath())
                .setProperty("sonar.java.xfile", TRUE)
                .setProperty("sonar.java.failOnException", TRUE);

        final BuildResult buildResult;
        // if build fail, job is not violently interrupted, allowing time to dump SQ
        // logs
        if (buildQuietly) {
            buildResult = orchestrator.executeBuildQuietly(build);
        }
        else {
            buildResult = orchestrator.executeBuild(build);
        }

        if (buildResult.isSuccess()) {
            assertNoDifferences();
        }
        else {
            dumpServerLogs();
            fail("Build failure for project: " + PROJECT_NAME);
        }
    }

    private static void assertNoDifferences() {
        try {
            final String differences = Files
                    .readString(new File(litsDifferencesPath()).toPath(), UTF_8);

            Assertions.assertThat(differences)
                    .isEmpty();
        }
        catch (IOException exception) {
            LOG.error("Failed to read LITS differences.", exception);
            fail("LITS differences not computed.");
        }
    }

    private static String litsDifferencesPath() {
        return FileLocation.of("target/" + PROJECT_NAME + "_differences")
                .getFile()
                .getAbsolutePath();
    }

    private static void dumpServerLogs() throws IOException {
        final Server server = orchestrator.getServer();
        LOG.error(":::::::::::::::: DUMPING SERVER LOGS ::::::::::::::::");
        dumpServerLogLastLines(server.getAppLogs());
        dumpServerLogLastLines(server.getCeLogs());
        dumpServerLogLastLines(server.getEsLogs());
        dumpServerLogLastLines(server.getWebLogs());
    }

    private static void dumpServerLogLastLines(File logFile) throws IOException {
        if (logFile.exists()) {
            List<String> logs = Files.readAllLines(logFile.toPath());
            final int nbLines = logs.size();
            if (nbLines > LOGS_NUMBER_LINES) {
                logs = logs.subList(nbLines - LOGS_NUMBER_LINES, nbLines);
            }
            final String collectedLogs = logs.stream()
                    .collect(Collectors.joining(System.lineSeparator()));

            LOG.error("============= START {} =============", logFile.getName());
            LOG.error("{} {}", System.lineSeparator(), collectedLogs);
            LOG.error("============= END {} =============", logFile.getName());
        }
    }

    private MavenBuild testProjectBuild() throws IOException {
        final File targetDir = prepareProject();

        final String pomLocation = targetDir.getCanonicalPath() + "/pom.xml";
        final File pomFile = FileLocation.of(pomLocation)
                .getFile()
                .getCanonicalFile();
        final MavenBuild mavenBuild = MavenBuild.create()
                .setPom(pomFile)
                .setCleanPackageSonarGoals()
                .addArgument("-Dmaven.test.skip=true")
                .addArgument("-DskipTests")
                .addArgument("-DskipITs");
        mavenBuild.setProperty("sonar.projectKey", PROJECT_KEY);
        return mavenBuild;
    }

    private File prepareProject() throws IOException {
        // restore quality profile
        orchestrator
                .getServer()
                .restoreProfile(FileLocation.of(
                    Paths.get("src/it/resources/integration-test-profile.xml").toFile()));

        // associate profile
        orchestrator.getServer().provisionProject(PROJECT_KEY, PROJECT_NAME);
        orchestrator.getServer()
                .associateProjectToQualityProfile(PROJECT_KEY, "java", "checkstyle");

        // copy project to analysis space
        final Path projectRoot = Paths.get("src/it/resources/" + PROJECT_NAME);
        final File targetDir = temp.newFolder(PROJECT_NAME);
        FileUtils.copyDirectory(projectRoot.toFile(), targetDir);
        return targetDir;
    }
}
