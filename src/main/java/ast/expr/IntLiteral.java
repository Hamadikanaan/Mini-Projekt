package ast.expr;

import ast.Expression;

// ============================================================================
// Literale
// ============================================================================

public class IntLiteral extends Expression {
  private int value;

  public IntLiteral(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
