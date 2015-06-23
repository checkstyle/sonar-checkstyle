/*
 * Copyright (C) 2009-2012 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
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
