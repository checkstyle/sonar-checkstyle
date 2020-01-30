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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class CheckJarTest {
    private static final String VERSION = "4.29";

    @Test
    public void testJarPresence() {
        final boolean snapshotExists = new File("target/checkstyle-sonar-plugin-"
                                                + VERSION + "-SNAPSHOT.jar").exists();
        final boolean releaseExists = new File("target/checkstyle-sonar-plugin-"
                                               + VERSION + ".jar").exists();
        assertTrue("Jar should exists",
                   snapshotExists || releaseExists);
    }
}
