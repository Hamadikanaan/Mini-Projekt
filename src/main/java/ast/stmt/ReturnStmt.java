package ast.stmt;

import ast.Expression;
import ast.Statement;

public class ReturnStmt extends Statement {
  private Expression value; // kann null sein (für void return)

  public ReturnStmt(Expression value) {
    this.value = value;
  }

  public Expression getValue() {
    return value;
  }

  public boolean hasValue() {
    return value != null;
  }
}
