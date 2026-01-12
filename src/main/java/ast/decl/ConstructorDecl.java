package ast.decl;

import ast.ASTNode;
import ast.stmt.BlockStmt;
import java.util.List;

public class ConstructorDecl extends ASTNode {
  private String className;
  private List<Parameter> parameters;
  private BlockStmt body;

  public ConstructorDecl(String className, List<Parameter> parameters, BlockStmt body) {
    this.className = className;
    this.parameters = parameters;
    this.body = body;
  }

  public String getClassName() {
    return className;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  public BlockStmt getBody() {
    return body;
  }
}
