package ast;

public abstract class Declaration extends ASTNode {
  protected String name;

  public Declaration(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
