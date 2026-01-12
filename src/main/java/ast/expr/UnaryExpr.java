package ast.expr;

import ast.Expression;

public class UnaryExpr extends Expression {
  public enum Operator {
    NEG, // -
    POS, // +
    NOT // !
  }

  private Operator operator;
  private Expression operand;

  public UnaryExpr(Operator operator, Expression operand) {
    this.operator = operator;
    this.operand = operand;
  }

  public Operator getOperator() {
    return operator;
  }

  public Expression getOperand() {
    return operand;
  }
}
