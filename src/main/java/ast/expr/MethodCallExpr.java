package ast.expr;

import ast.Expression;
import java.util.List;

public class MethodCallExpr extends Expression {
  private Expression object;
  private String methodName;
  private List<Expression> arguments;

  public MethodCallExpr(Expression object, String methodName, List<Expression> arguments) {
    this.object = object;
    this.methodName = methodName;
    this.arguments = arguments;
  }

  public Expression getObject() {
    return object;
  }

  public String getMethodName() {
    return methodName;
  }

  public List<Expression> getArguments() {
    return arguments;
  }
}
