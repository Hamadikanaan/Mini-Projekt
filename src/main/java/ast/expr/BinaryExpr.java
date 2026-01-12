package ast.expr;

import ast.Expression;

public class BinaryExpr extends Expression {
  public enum Operator {
    // Arithmetik
    ADD,
    SUB,
    MUL,
    DIV,
    MOD,
    // Vergleich
    EQ,
    NEQ,
    LT,
    LE,
    GT,
    GE,
    // Logik
    AND,
    OR
  }

  private Expression left;
  private Operator operator;
  private Expression right;

  public BinaryExpr(Expression left, Operator operator, Expression right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public Expression getLeft() {
    return left;
  }

  public Operator getOperator() {
    return operator;
  }

  public Expression getRight() {
    return right;
  }
}
