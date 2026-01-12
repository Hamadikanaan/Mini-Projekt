package ast.stmt;

import ast.Expression;
import ast.Statement;
import ast.Type;

public class VarDeclStmt extends Statement {
  private Type type;
  private String name;
  private Expression initializer; // kann null sein

  public VarDeclStmt(Type type, String name, Expression initializer) {
    this.type = type;
    this.name = name;
    this.initializer = initializer;
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Expression getInitializer() {
    return initializer;
  }

  public boolean hasInitializer() {
    return initializer != null;
  }
}
