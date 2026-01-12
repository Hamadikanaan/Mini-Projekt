package ast.expr;

import ast.Expression;

public class IdentifierExpr extends Expression {
  private String name;

  public IdentifierExpr(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
