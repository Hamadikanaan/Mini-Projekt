package ast.decl;

import ast.Declaration;
import ast.Type;
import ast.stmt.BlockStmt;
import java.util.List;

public class FunctionDecl extends Declaration {
  private Type returnType;
  private List<Parameter> parameters;
  private BlockStmt body;

  public FunctionDecl(String name, Type returnType, List<Parameter> parameters, BlockStmt body) {
    super(name);
    this.returnType = returnType;
    this.parameters = parameters;
    this.body = body;
  }

  public Type getReturnType() {
    return returnType;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  public BlockStmt getBody() {
    return body;
  }
}
