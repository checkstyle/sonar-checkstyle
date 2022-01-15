////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static String getModifiableTokens(String checkName) {
        final AbstractCheck checkResult = getCheck(checkName);
        final String result;
        if (AbstractJavadocCheck.class.isAssignableFrom(checkResult.getClass())) {
            final AbstractJavadocCheck javadocCheck = (AbstractJavadocCheck) checkResult;
            final List<Integer> modifiableJavadocTokens =
                    subtractTokens(javadocCheck.getAcceptableJavadocTokens(),
                            javadocCheck.getRequiredJavadocTokens());
            result = getTokens(JavadocUtil::getTokenName, modifiableJavadocTokens);
        }
        else if (AbstractCheck.class.isAssignableFrom(checkResult.getClass())) {
            final List<Integer> modifiableTokens = subtractTokens(checkResult.getAcceptableTokens(),
                  checkResult.getRequiredTokens());
            result = getTokens(TokenUtil::getTokenName, modifiableTokens);
        }
        else {
            throw new IllegalStateException("Exception caused in CheckUtil.getCheck, "
                    + "method executed in wrong context, heirarchy of check class missing");
        }
        return result;
    }

    private static AbstractCheck getCheck(String checkName) {
        final ClassLoader classLoader = CheckstyleMetadata.class.getClassLoader();
        try {
            final Set<String> packageNames = PackageNamesLoader.getPackageNames(classLoader);
            return (AbstractCheck) new PackageObjectFactory(packageNames, classLoader)
                    .createModule(checkName);
        }
        catch (CheckstyleException ex) {
            throw new IllegalStateException("exception occured during load of " + checkName, ex);
        }
    }

    private static List<Integer> subtractTokens(int[] tokens, int... requiredTokens) {
        final Set<Integer> requiredTokensSet = Arrays.stream(requiredTokens)
              .boxed().collect(Collectors.toSet());
        return Arrays.stream(tokens)
              .boxed()
              .filter(token -> !requiredTokensSet.contains(token))
              .collect(Collectors.toList());
    }

    private static String getTokens(Function<Integer, String> function,
                                    List<Integer> modifiableTokens) {
        return modifiableTokens.stream()
            .map(function)
            .collect(Collectors.joining(","));
    }
}
