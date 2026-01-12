package ast.decl;

import ast.Declaration;
import java.util.List;

public class ClassDecl extends Declaration {
  private String baseClass; // kann null sein
  private List<FieldDecl> fields;
  private List<MethodDecl> methods;
  private List<ConstructorDecl> constructors;

  public ClassDecl(
      String name,
      String baseClass,
      List<FieldDecl> fields,
      List<MethodDecl> methods,
      List<ConstructorDecl> constructors) {
    super(name);
    this.baseClass = baseClass;
    this.fields = fields;
    this.methods = methods;
    this.constructors = constructors;
  }

  public String getBaseClass() {
    return baseClass;
  }

  public boolean hasBaseClass() {
    return baseClass != null;
  }

  public List<FieldDecl> getFields() {
    return fields;
  }

  public List<MethodDecl> getMethods() {
    return methods;
  }

  public List<ConstructorDecl> getConstructors() {
    return constructors;
  }
}
