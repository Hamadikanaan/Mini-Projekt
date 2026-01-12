package ast.stmt;

import ast.Expression;
import ast.Statement;

public class ExprStmt extends Statement {
  private Expression expression;

  public ExprStmt(Expression expression) {
    this.expression = expression;
  }

  public Expression getExpression() {
    return expression;
  }
}
