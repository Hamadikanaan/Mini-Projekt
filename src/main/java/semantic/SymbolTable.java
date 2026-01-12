package semantic;

import ast.decl.ClassDecl;
import ast.decl.FunctionDecl;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
  private Stack<Map<String, Symbol>> scopes;
  private Map<String, FunctionDecl> functions;
  private Map<String, ClassDecl> classes;

  public SymbolTable() {
    scopes = new Stack<>();
    functions = new HashMap<>();
    classes = new HashMap<>();
    enterScope(); // Globaler Scope
  }

  // === Scope Management ===

  public void enterScope() {
    scopes.push(new HashMap<>());
  }

  public void exitScope() {
    if (scopes.size() > 1) {
      scopes.pop();
    }
  }

  // === Variablen ===

  public void declareVariable(Symbol symbol) {
    Map<String, Symbol> currentScope = scopes.peek();
    if (currentScope.containsKey(symbol.getName())) {
      throw new SemanticException("Variable '" + symbol.getName() + "' bereits definiert");
    }
    currentScope.put(symbol.getName(), symbol);
  }

  public Symbol lookupVariable(String name) {
    // Suche von innen nach außen
    for (int i = scopes.size() - 1; i >= 0; i--) {
      Map<String, Symbol> scope = scopes.get(i);
      if (scope.containsKey(name)) {
        return scope.get(name);
      }
    }
    return null;
  }

  public boolean isVariableDeclaredInCurrentScope(String name) {
    return scopes.peek().containsKey(name);
  }

  // === Funktionen ===

  public void declareFunction(FunctionDecl func) {
    String signature = getFunctionSignature(func);
    if (functions.containsKey(signature)) {
      throw new SemanticException("Funktion '" + func.getName() + "' bereits definiert");
    }
    functions.put(signature, func);
  }

  public FunctionDecl lookupFunction(String name, int argCount) {
    // Suche nach passender Funktion (einfache Überladung nach Arität)
    for (Map.Entry<String, FunctionDecl> entry : functions.entrySet()) {
      FunctionDecl func = entry.getValue();
      if (func.getName().equals(name) && func.getParameters().size() == argCount) {
        return func;
      }
    }
    return null;
  }

  private String getFunctionSignature(FunctionDecl func) {
    StringBuilder sb = new StringBuilder(func.getName());
    sb.append("(");
    for (int i = 0; i < func.getParameters().size(); i++) {
      if (i > 0) sb.append(",");
      sb.append(func.getParameters().get(i).getType().toString());
    }
    sb.append(")");
    return sb.toString();
  }

  // === Klassen ===

  public void declareClass(ClassDecl cls) {
    if (classes.containsKey(cls.getName())) {
      throw new SemanticException("Klasse '" + cls.getName() + "' bereits definiert");
    }
    classes.put(cls.getName(), cls);
  }

  public ClassDecl lookupClass(String name) {
    return classes.get(name);
  }

  public boolean isClassType(String typeName) {
    return classes.containsKey(typeName);
  }

  // === Hilfsmethoden ===

  public Map<String, FunctionDecl> getAllFunctions() {
    return functions;
  }

  public Map<String, ClassDecl> getAllClasses() {
    return classes;
  }
}
