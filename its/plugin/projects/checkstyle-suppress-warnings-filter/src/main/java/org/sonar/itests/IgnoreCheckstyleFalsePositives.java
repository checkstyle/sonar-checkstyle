package org.sonar.itests;

public class IgnoreCheckstyleFalsePositives {

  @SuppressWarnings("checkstyle:emptyblock")
  public void ignoreIssueOnEmptyBlock_1() {
    try {
      int i = 1;
    } catch (Exception e) {
    }
  }

  @SuppressWarnings({"emptyblock"})
  public void ignoreIssueOnEmptyBlock_2() {
    try {
      int i = 2;
    } catch (Exception e) {
    }
  }

  @SuppressWarnings("emptyblock")
  public void ignoreIssueOnEmptyBlock_3() {
    try {
      int i = 3;
    } catch (Exception e) {
    }
  }

  public void ruleEmptyBlockViolated() {
    try {
      int i = 4;
    } catch (Exception e) {
    }
  }
}
