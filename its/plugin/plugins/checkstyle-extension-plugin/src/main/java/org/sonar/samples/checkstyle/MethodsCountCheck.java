/*
 * Java :: IT :: Plugins :: Checkstyle Extension
 * Copyright (C) 2013 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.samples.checkstyle;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class MethodsCountCheck extends Check {

  private int maxMethodsCount = 1;

  private int methodsCount = 0;
  private DetailAST classAST = null;

  public void setMaxMethodsCount(int maxMethodsCount) {
    this.maxMethodsCount = maxMethodsCount;
  }

  public int[] getDefaultTokens() {
    return new int[]{TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF};
  }

  public void beginTree(DetailAST rootAST) {
    methodsCount = 0;
    classAST = null;
  }

  public void visitToken(DetailAST ast) {
    //ensure this is an unit test.
    if (ast.getType() == TokenTypes.CLASS_DEF) {
      classAST = ast;

    } else if (ast.getType() == TokenTypes.METHOD_DEF) {
      methodsCount++;
    }
  }

  public void finishTree(DetailAST rootAST) {
    super.finishTree(rootAST);
    if (classAST != null && methodsCount > maxMethodsCount) {
      log(classAST.getLineNo(), classAST.getColumnNo(), "Too many methods (" + methodsCount + ") in class");
    }
  }
}
