package ast.expr;

import ast.Expression;

public class AssignExpr extends Expression {
  private Expression target;
  private Expression value;

  public AssignExpr(Expression target, Expression value) {
    this.target = target;
    this.value = value;
  }

  public Expression getTarget() {
    return target;
  }

  public Expression getValue() {
    return value;
  }
}
