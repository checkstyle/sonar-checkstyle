////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2025 the original author or authors.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

public class CheckstyleSeverityUtilsTest {

    @Test
    public void testToSeverityString() {
        assertThat(CheckstyleSeverityUtils.toSeverity("BLOCKER")).isEqualTo("error");
        assertThat(CheckstyleSeverityUtils.toSeverity("CRITICAL")).isEqualTo("error");
        assertThat(CheckstyleSeverityUtils.toSeverity("MAJOR")).isEqualTo("warning");
        assertThat(CheckstyleSeverityUtils.toSeverity("MINOR")).isEqualTo("info");
        assertThat(CheckstyleSeverityUtils.toSeverity("INFO")).isEqualTo("info");
    }

    @Test
    public void testToSeverityWrongString() {
        try {
            CheckstyleSeverityUtils.toSeverity("nothing");
            Assert.fail("IOException while writing should not be ignored");
        }
        catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Priority not supported: nothing");
        }
    }

    @Test
    public void privateConstructor() throws ReflectiveOperationException {
        final Constructor<CheckstyleSeverityUtils> constructor = CheckstyleSeverityUtils.class
                .getDeclaredConstructor();

        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
