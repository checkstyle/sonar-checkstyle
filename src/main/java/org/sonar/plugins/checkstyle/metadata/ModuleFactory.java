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

package org.sonar.plugins.checkstyle.metadata;

import java.util.Set;

import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public final class ModuleFactory {
    private static PackageObjectFactory packageObjectFactory;

    private ModuleFactory() {
    }

    public static AbstractCheck getCheck(String checkName) {
        if (packageObjectFactory == null) {
            try {
                final ClassLoader classLoader = CheckstyleMetadata.class.getClassLoader();
                final Set<String> packageNames = PackageNamesLoader.getPackageNames(classLoader);
                packageObjectFactory = new PackageObjectFactory(packageNames, classLoader);
            }
            catch (CheckstyleException ex) {
                throw new IllegalStateException("exception happened during initialization of"
                        + " PackageObjectFactory while loading of " + checkName, ex);
            }
        }

        try {
            return (AbstractCheck) packageObjectFactory.createModule(checkName);
        }
        catch (CheckstyleException ex) {
            throw new IllegalStateException("exception occured during load of " + checkName, ex);
        }
    }
}
