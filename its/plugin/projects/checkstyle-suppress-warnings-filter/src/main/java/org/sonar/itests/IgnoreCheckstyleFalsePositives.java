package org.sonar.itests;

public class IgnoreCheckstyleFalsePositives {

  @SuppressWarnings("checkstyle:emptystatement")
  public void ignoreIssueOnEmptyStmt_1() {
    ;
  }

  @SuppressWarnings({"emptystatement"})
  public void ignoreIssueOnEmptyStmt_2() {
    ;
  }

  @SuppressWarnings("emptystatement")
  public void ignoreIssueOnEmptyStmt_3() {
    ;
  }

  public void ruleEmptyStmtViolated() {
    ;
  }
}
