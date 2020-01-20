package org.checkstyle.plugins.sonar;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class CheckJarTest {

    @Test
    public void testJarPresence() {
        assertTrue("Jar should exists",
                new File("target/checkstyle-sonar-plugin-4.28-SNAPSHOT.jar").exists());
    }
}
