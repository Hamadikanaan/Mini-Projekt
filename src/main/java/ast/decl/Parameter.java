package ast.decl;

import ast.ASTNode;
import ast.Type;

public class Parameter extends ASTNode {
  private Type type;
  private String name;

  public Parameter(Type type, String name) {
    this.type = type;
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }
}
