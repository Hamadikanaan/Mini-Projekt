package semantic;

import ast.Type;

public class Symbol {
  private String name;
  private Type type;
  private Object value;
  private boolean isReference;
  private Symbol referenceTo; // für Referenzen: zeigt auf das Original

  public Symbol(String name, Type type) {
    this.name = name;
    this.type = type;
    this.value = null;
    this.isReference = type != null && type.isReference();
    this.referenceTo = null;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public Object getValue() {
    if (isReference && referenceTo != null) {
      return referenceTo.getValue();
    }
    return value;
  }

  public void setValue(Object value) {
    if (isReference && referenceTo != null) {
      referenceTo.setValue(value);
    } else {
      this.value = value;
    }
  }

  public boolean isReference() {
    return isReference;
  }

  public void setReferenceTo(Symbol target) {
    this.referenceTo = target;
  }

  public Symbol getReferenceTo() {
    return referenceTo;
  }
}
