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

package org.sonar.plugins.checkstyle.metadata;

import java.util.Set;

import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

public final class CheckUtil {
    private CheckUtil() {
    }

    public static Object getCheck(String checkName) {
        final ClassLoader classLoader = CheckstyleMetadata.class.getClassLoader();
        try {
            final Set<String> packageNames = PackageNamesLoader.getPackageNames(classLoader);
            return new PackageObjectFactory(packageNames, classLoader)
                    .createModule(checkName.substring(0, checkName.length() - "Check".length()));
        }
        catch (CheckstyleException ex) {
            throw new IllegalStateException("exception occured during load of " + checkName, ex);
        }
    }

    public static String getAcceptableTokens(String checkName) {
        final Object checkResult = getCheck(checkName);
        String result = null;
        if (AbstractJavadocCheck.class.isAssignableFrom(checkResult.getClass())) {
            final AbstractJavadocCheck javadocCheck = (AbstractJavadocCheck) checkResult;
            result = getTokenText(true, javadocCheck.getAcceptableJavadocTokens(),
                    javadocCheck.getRequiredJavadocTokens());
        }
        else if (AbstractCheck.class.isAssignableFrom(checkResult.getClass())) {
            final AbstractCheck check = (AbstractCheck) checkResult;
            result = getTokenText(false, check.getAcceptableTokens(),
                    check.getRequiredTokens());
        }
        return result;
    }

    public static String getTokenText(boolean isJavadocCheck, int[] tokens, int... requiredTokens) {
        final StringBuilder result = new StringBuilder();
        boolean first = true;

        for (int token : tokens) {
            boolean found = false;

            for (int subtraction : requiredTokens) {
                if (subtraction == token) {
                    found = true;
                    break;
                }
            }

            if (found) {
                continue;
            }

            if (first) {
                first = false;
            }
            else {
                result.append(',');
            }

            if (isJavadocCheck) {
                result.append(JavadocUtil.getTokenName(token));
            }
            else {
                result.append(TokenUtil.getTokenName(token));
            }
        }

        return result.toString();
    }
}
