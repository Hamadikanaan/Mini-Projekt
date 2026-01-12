package ast.expr;

import ast.Expression;

public class StringLiteral extends Expression {
  private String value;

  public StringLiteral(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
