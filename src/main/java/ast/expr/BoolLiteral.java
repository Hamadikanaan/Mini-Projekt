package ast.expr;

import ast.Expression;

public class BoolLiteral extends Expression {
  private boolean value;

  public BoolLiteral(boolean value) {
    this.value = value;
  }

  public boolean getValue() {
    return value;
  }
}
