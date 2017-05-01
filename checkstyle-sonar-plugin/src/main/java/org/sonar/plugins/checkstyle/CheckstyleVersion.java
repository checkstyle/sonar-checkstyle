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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

public enum CheckstyleVersion {
    INSTANCE;

    private static final String PROPERTIES_PATH =
            "/org/sonar/plugins/checkstyle/checkstyle-plugin.properties";
    private String version;

    CheckstyleVersion() {
        final InputStream input = getClass().getResourceAsStream(PROPERTIES_PATH);
        try {
            final Properties properties = new Properties();
            properties.load(input);
            version = properties.getProperty("checkstyle.version");

        }
        catch (IOException ex) {
            LoggerFactory.getLogger(getClass()).warn(
                    "Can not load the Checkstyle version from the file " + PROPERTIES_PATH, ex);
            version = "";
        }
        finally {
            IOUtils.closeQuietly(input);
        }
    }

    public static String getVersion() {
        return INSTANCE.version;
    }
}
