package ast.decl;

import ast.ASTNode;
import ast.Type;
import ast.stmt.BlockStmt;
import java.util.List;

public class MethodDecl extends ASTNode {
  private String name;
  private Type returnType;
  private List<Parameter> parameters;
  private BlockStmt body;
  private boolean isVirtual;

  public MethodDecl(
      String name, Type returnType, List<Parameter> parameters, BlockStmt body, boolean isVirtual) {
    this.name = name;
    this.returnType = returnType;
    this.parameters = parameters;
    this.body = body;
    this.isVirtual = isVirtual;
  }

  public String getName() {
    return name;
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

  public boolean isVirtual() {
    return isVirtual;
  }
}
