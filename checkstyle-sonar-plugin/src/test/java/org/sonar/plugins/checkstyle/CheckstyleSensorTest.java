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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;

import com.google.common.collect.ImmutableList;

public class CheckstyleSensorTest {

    private final RulesProfile profile = mock(RulesProfile.class);
    private final DefaultFileSystem fileSystem = new DefaultFileSystem(new File(""));
    private final CheckstyleSensor sensor = new CheckstyleSensor(profile, null, fileSystem);

    private final Project project = new Project("projectKey");

    @Test
    public void shouldExecuteOnProjectWithoutJavaFileAndWithRule() {
        addOneActiveRule();
        assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
    }

    @Test
    public void shouldExecuteOnProjectWithJavaFileAndWithoutRule() {
        addOneJavaFile();
        assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
    }

    @Test
    public void shouldExecuteOnProjectWithJavaFilesAndRules() {
        addOneJavaFile();
        addOneActiveRule();
        assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
    }

    @Test
    public void testToString() {
        assertThat(new CheckstyleSensor(null, null, null).toString()).isEqualTo("CheckstyleSensor");
    }

    private void addOneJavaFile() {
        final File file = new File("MyClass.java");
        fileSystem.add(new DefaultInputFile("", file.getName()).setLanguage("java").setType(
                Type.MAIN));
    }

    private void addOneActiveRule() {
        when(profile.getActiveRulesByRepository("checkstyle")).thenReturn(
                ImmutableList.of(mock(ActiveRule.class)));
    }

}
