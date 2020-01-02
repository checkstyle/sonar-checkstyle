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

package org.sonar.plugins.checkstyle;

import static org.fest.assertions.Assertions.assertThat;

import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.rules.RulePriority;

public class CheckstyleSeverityUtilsTest {

    @Test
    public void testToSeverity() {
        assertThat(CheckstyleSeverityUtils.toSeverity(RulePriority.BLOCKER)).isEqualTo("error");
        assertThat(CheckstyleSeverityUtils.toSeverity(RulePriority.CRITICAL)).isEqualTo("error");
        assertThat(CheckstyleSeverityUtils.toSeverity(RulePriority.MAJOR)).isEqualTo("warning");
        assertThat(CheckstyleSeverityUtils.toSeverity(RulePriority.MINOR)).isEqualTo("info");
        assertThat(CheckstyleSeverityUtils.toSeverity(RulePriority.INFO)).isEqualTo("info");
    }

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
    public void testFromSeverity() {
        assertThat(CheckstyleSeverityUtils.fromSeverity("error")).isEqualTo(RulePriority.BLOCKER);
        assertThat(CheckstyleSeverityUtils.fromSeverity("warning")).isEqualTo(RulePriority.MAJOR);
        assertThat(CheckstyleSeverityUtils.fromSeverity("info")).isEqualTo(RulePriority.INFO);
        assertThat(CheckstyleSeverityUtils.fromSeverity("ignore")).isEqualTo(RulePriority.INFO);
        assertThat(CheckstyleSeverityUtils.fromSeverity("")).isNull();
    }

    @Test
    public void privateConstructor() throws ReflectiveOperationException {
        final Constructor<CheckstyleSeverityUtils> constructor = CheckstyleSeverityUtils.class
                .getDeclaredConstructor();
        assertThat(constructor.isAccessible()).isFalse();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
