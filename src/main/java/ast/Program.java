package ast;

import ast.decl.ClassDecl;
import ast.decl.FunctionDecl;
import java.util.List;

public class Program extends ASTNode {
  private List<ClassDecl> classes;
  private List<FunctionDecl> functions;

  public Program(List<ClassDecl> classes, List<FunctionDecl> functions) {
    this.classes = classes;
    this.functions = functions;
  }

  public List<ClassDecl> getClasses() {
    return classes;
  }

  public List<FunctionDecl> getFunctions() {
    return functions;
  }

  public FunctionDecl getMainFunction() {
    for (FunctionDecl func : functions) {
      if (func.getName().equals("main")) {
        return func;
      }
    }
    return null;
  }
}
