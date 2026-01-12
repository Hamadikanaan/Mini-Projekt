package ast.stmt;

import ast.Expression;
import ast.Statement;

public class IfStmt extends Statement {
  private Expression condition;
  private Statement thenBranch;
  private Statement elseBranch; // kann null sein

  public IfStmt(Expression condition, Statement thenBranch, Statement elseBranch) {
    this.condition = condition;
    this.thenBranch = thenBranch;
    this.elseBranch = elseBranch;
  }

  public Expression getCondition() {
    return condition;
  }

  public Statement getThenBranch() {
    return thenBranch;
  }

  public Statement getElseBranch() {
    return elseBranch;
  }

  public boolean hasElseBranch() {
    return elseBranch != null;
  }
}
