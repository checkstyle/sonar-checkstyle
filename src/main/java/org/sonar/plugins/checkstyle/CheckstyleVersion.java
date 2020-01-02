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

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

public final class CheckstyleVersion {

    public String getVersion(String path) {
        return loadVersion(path);
    }

    private String loadVersion(String path) {
        String version;
        final InputStream input = getClass().getResourceAsStream(path);
        try {
            final Properties properties = new Properties();
            properties.load(input);
            version = properties.getProperty("checkstyle.version");

        }
        catch (Exception ex) {
            LoggerFactory.getLogger(getClass())
                    .warn("Can not load the Checkstyle version from the file {}", path, ex);
            version = "";
        }
        finally {
            IOUtils.closeQuietly(input);
        }
        return version;
    }
}
