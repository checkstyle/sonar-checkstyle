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
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Properties.class, CheckstyleVersion.class})
public class CheckstyleVersionTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void getCheckstyleVersion() {
        assertThat(new CheckstyleVersion().getVersion().length()).isGreaterThan(1);
    }

    @Test
    public void getCheckstyleVersionException() throws Exception {
        final Properties mock = PowerMockito.mock(Properties.class);
        PowerMockito.whenNew(Properties.class).withNoArguments().thenReturn(mock);
        PowerMockito.doThrow(new IOException("Unable to process DataSource")).when(mock)
                .load(any(InputStream.class));

        final CheckstyleVersion version = new CheckstyleVersion();
        assertEquals("", version.getVersion());
    }
}
