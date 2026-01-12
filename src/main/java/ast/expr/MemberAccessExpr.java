package ast.expr;

import ast.Expression;

public class MemberAccessExpr extends Expression {
  private Expression object;
  private String memberName;

  public MemberAccessExpr(Expression object, String memberName) {
    this.object = object;
    this.memberName = memberName;
  }

  public Expression getObject() {
    return object;
  }

  public String getMemberName() {
    return memberName;
  }
}
