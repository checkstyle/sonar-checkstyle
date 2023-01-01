////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2023 the original author or authors.
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;

public class CheckstyleSensorTest {

    @Test
    public void shouldDescribePluginCorrectly() {
        final DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
        final CheckstyleSensor sensor = new CheckstyleSensor(null);

        sensor.describe(descriptor);
        assertThat(descriptor.languages()).containsOnly("java");
        assertThat(descriptor.name()).isNotEmpty();
    }

    @Test
    public void shouldExecuteExecutorWithContext() {
        final SensorContext context = mock(SensorContext.class);
        final CheckstyleExecutor executor = mock(CheckstyleExecutor.class);

        final CheckstyleSensor sensor = new CheckstyleSensor(executor);
        sensor.execute(context);

        verify(executor, times(1)).execute(context);
    }

    @Test
    public void testToString() {
        assertThat(new CheckstyleSensor(null).toString()).isEqualTo("CheckstyleSensor");
    }
}
