package interpreter;

import ast.decl.ClassDecl;
import java.util.HashMap;
import java.util.Map;

public class RuntimeValue {
  private ClassDecl classDecl;
  private Map<String, Object> fields;

  public RuntimeValue(ClassDecl classDecl) {
    this.classDecl = classDecl;
    this.fields = new HashMap<>();
  }

  public ClassDecl getClassDecl() {
    return classDecl;
  }

  public Object getField(String name) {
    return fields.get(name);
  }

  public void setField(String name, Object value) {
    fields.put(name, value);
  }

  public Map<String, Object> getFields() {
    return fields;
  }

  public RuntimeValue copy() {
    RuntimeValue copy = new RuntimeValue(classDecl);
    copy.fields.putAll(this.fields);
    return copy;
  }

  @Override
  public String toString() {
    return classDecl.getName() + fields.toString();
  }
}
