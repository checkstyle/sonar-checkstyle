////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2020 the original author or authors.
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

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.junit.Test;

public class CheckJarTest {
    private static final String MATCHER = ".*checkstyle-sonar-plugin-\\d+\\.\\d+(-SNAPSHOT)?\\.jar";

    @Test
    public void testJarPresence() throws IOException {
        final BiPredicate<Path, BasicFileAttributes> matcher = (path, basicFileAttributes) -> {
            return path.toString()
                    .matches(MATCHER);
        };
        final List<Path> files = Files.find(Paths.get("target"), 1, matcher)
                .collect(Collectors.toList());
        assertFalse("Jar should exists", files.isEmpty());
    }
}
