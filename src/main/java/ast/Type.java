package ast;

public class Type extends ASTNode {
  private String typeName;
  private boolean isReference;

  public Type(String typeName, boolean isReference) {
    this.typeName = typeName;
    this.isReference = isReference;
  }

  public Type(String typeName) {
    this(typeName, false);
  }

  public String getTypeName() {
    return typeName;
  }

  public boolean isReference() {
    return isReference;
  }

  public boolean isPrimitive() {
    return typeName.equals("int")
        || typeName.equals("bool")
        || typeName.equals("char")
        || typeName.equals("string")
        || typeName.equals("void");
  }

  @Override
  public String toString() {
    return typeName + (isReference ? "&" : "");
  }
}
