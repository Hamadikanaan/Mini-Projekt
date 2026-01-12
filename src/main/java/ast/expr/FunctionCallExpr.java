package ast.expr;

import ast.Expression;
import java.util.List;

public class FunctionCallExpr extends Expression {
  private String functionName;
  private List<Expression> arguments;

  public FunctionCallExpr(String functionName, List<Expression> arguments) {
    this.functionName = functionName;
    this.arguments = arguments;
  }

  public String getFunctionName() {
    return functionName;
  }

  public List<Expression> getArguments() {
    return arguments;
  }
}
