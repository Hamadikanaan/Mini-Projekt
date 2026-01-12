package ast.stmt;

import ast.Expression;
import ast.Statement;

public class WhileStmt extends Statement {
  private Expression condition;
  private Statement body;

  public WhileStmt(Expression condition, Statement body) {
    this.condition = condition;
    this.body = body;
  }

  public Expression getCondition() {
    return condition;
  }

  public Statement getBody() {
    return body;
  }
}
