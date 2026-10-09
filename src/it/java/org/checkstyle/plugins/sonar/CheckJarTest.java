////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2026 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;

import org.junit.jupiter.api.Test;

class CheckJarTest {

    private static final String MATCHER = ".*checkstyle-sonar-plugin-"
                                          + "\\d+\\.\\d+(.\\d+)?(-SNAPSHOT)?\\.jar";

    @Test
    void testJarPresence() throws IOException {
        final BiPredicate<Path, BasicFileAttributes> matcher = (path, basicFileAttributes) -> {
            return path.toString()
                    .matches(MATCHER);
        };
        final List<Path> files = Files.find(Paths.get("target"), 1, matcher)
                .toList();
        assertFalse(files.isEmpty(), "Jar should exists");
    }

}
