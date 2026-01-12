package ast.stmt;

import ast.Statement;
import java.util.List;

public class BlockStmt extends Statement {
  private List<Statement> statements;

  public BlockStmt(List<Statement> statements) {
    this.statements = statements;
  }

  public List<Statement> getStatements() {
    return statements;
  }
}
