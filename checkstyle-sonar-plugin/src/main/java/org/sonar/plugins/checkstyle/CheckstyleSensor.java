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

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;

public class CheckstyleSensor implements Sensor {

    private final RulesProfile profile;
    private final CheckstyleExecutor executor;
    private final FileSystem fileSystem;

    public CheckstyleSensor(RulesProfile profile, CheckstyleExecutor executor,
            FileSystem fileSystem) {
        this.profile = profile;
        this.executor = executor;
        this.fileSystem = fileSystem;
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        final FilePredicates predicates = fileSystem.predicates();
        final Iterable<File> mainFiles = fileSystem
                .files(predicates.and(predicates.hasLanguage(CheckstyleConstants.JAVA_KEY),
                        predicates.hasType(Type.MAIN)));
        final boolean mainFilesIsEmpty = !mainFiles.iterator().hasNext();
        return !mainFilesIsEmpty
                && !profile.getActiveRulesByRepository(CheckstyleConstants.REPOSITORY_KEY)
                        .isEmpty();
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
