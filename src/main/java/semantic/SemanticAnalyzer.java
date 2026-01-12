package semantic;

import ast.*;
import ast.decl.*;
import ast.expr.*;
import ast.stmt.*;

public class SemanticAnalyzer {
  private SymbolTable symbolTable;
  private Type currentFunctionReturnType;
  private ClassDecl currentClass;

  public SemanticAnalyzer(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  public void analyze(Program program) {
    // Pass 1: Alle Klassen registrieren
    for (ClassDecl cls : program.getClasses()) {
      symbolTable.declareClass(cls);
    }

    // Pass 2: Alle Funktionen registrieren
    for (FunctionDecl func : program.getFunctions()) {
      symbolTable.declareFunction(func);
    }

    // Pass 3: Klassen analysieren
    for (ClassDecl cls : program.getClasses()) {
      analyzeClass(cls);
    }

    // Pass 4: Funktionen analysieren
    for (FunctionDecl func : program.getFunctions()) {
      analyzeFunction(func);
    }
  }

  private void analyzeClass(ClassDecl cls) {
    currentClass = cls;

    // Prüfe Basisklasse
    if (cls.hasBaseClass()) {
      ClassDecl baseClass = symbolTable.lookupClass(cls.getBaseClass());
      if (baseClass == null) {
        throw new SemanticException("Basisklasse '" + cls.getBaseClass() + "' nicht gefunden");
      }
    }

    // Analysiere Konstruktoren
    for (ConstructorDecl ctor : cls.getConstructors()) {
      analyzeConstructor(ctor);
    }

    // Analysiere Methoden
    for (MethodDecl method : cls.getMethods()) {
      analyzeMethod(method);
    }

    currentClass = null;
  }

  private void analyzeConstructor(ConstructorDecl ctor) {
    symbolTable.enterScope();

    // Parameter registrieren
    for (Parameter param : ctor.getParameters()) {
      Symbol symbol = new Symbol(param.getName(), param.getType());
      symbolTable.declareVariable(symbol);
    }

    // Body analysieren
    currentFunctionReturnType = new Type("void");
    analyzeBlock(ctor.getBody());

    symbolTable.exitScope();
  }

  private void analyzeMethod(MethodDecl method) {
    symbolTable.enterScope();

    // Parameter registrieren
    for (Parameter param : method.getParameters()) {
      Symbol symbol = new Symbol(param.getName(), param.getType());
      symbolTable.declareVariable(symbol);
    }

    // Body analysieren
    currentFunctionReturnType = method.getReturnType();
    analyzeBlock(method.getBody());

    symbolTable.exitScope();
  }

  private void analyzeFunction(FunctionDecl func) {
    symbolTable.enterScope();

    // Parameter registrieren
    for (Parameter param : func.getParameters()) {
      Symbol symbol = new Symbol(param.getName(), param.getType());
      symbolTable.declareVariable(symbol);
    }

    // Body analysieren
    currentFunctionReturnType = func.getReturnType();
    analyzeBlock(func.getBody());

    symbolTable.exitScope();
  }

  private void analyzeBlock(BlockStmt block) {
    for (Statement stmt : block.getStatements()) {
      analyzeStatement(stmt);
    }
  }

  private void analyzeStatement(Statement stmt) {
    if (stmt instanceof VarDeclStmt) {
      analyzeVarDecl((VarDeclStmt) stmt);
    } else if (stmt instanceof IfStmt) {
      analyzeIf((IfStmt) stmt);
    } else if (stmt instanceof WhileStmt) {
      analyzeWhile((WhileStmt) stmt);
    } else if (stmt instanceof ReturnStmt) {
      analyzeReturn((ReturnStmt) stmt);
    } else if (stmt instanceof ExprStmt) {
      analyzeExprStmt((ExprStmt) stmt);
    } else if (stmt instanceof BlockStmt) {
      symbolTable.enterScope();
      analyzeBlock((BlockStmt) stmt);
      symbolTable.exitScope();
    }
  }

  private void analyzeVarDecl(VarDeclStmt stmt) {
    // Prüfe ob Variable bereits existiert
    if (symbolTable.isVariableDeclaredInCurrentScope(stmt.getName())) {
      throw new SemanticException("Variable '" + stmt.getName() + "' bereits definiert");
    }

    // Referenz braucht Initialisierer
    if (stmt.getType().isReference() && !stmt.hasInitializer()) {
      throw new SemanticException("Referenz '" + stmt.getName() + "' muss initialisiert werden");
    }

    // Referenz braucht LValue
    if (stmt.getType().isReference() && stmt.hasInitializer()) {
      if (!isLValue(stmt.getInitializer())) {
        throw new SemanticException("Referenz kann nur mit LValue initialisiert werden");
      }
    }

    // Initialisierer analysieren
    if (stmt.hasInitializer()) {
      analyzeExpression(stmt.getInitializer());
    }

    // Variable registrieren
    Symbol symbol = new Symbol(stmt.getName(), stmt.getType());
    symbolTable.declareVariable(symbol);
  }

  private void analyzeIf(IfStmt stmt) {
    analyzeExpression(stmt.getCondition());

    symbolTable.enterScope();
    analyzeStatement(stmt.getThenBranch());
    symbolTable.exitScope();

    if (stmt.hasElseBranch()) {
      symbolTable.enterScope();
      analyzeStatement(stmt.getElseBranch());
      symbolTable.exitScope();
    }
  }

  private void analyzeWhile(WhileStmt stmt) {
    analyzeExpression(stmt.getCondition());

    symbolTable.enterScope();
    analyzeStatement(stmt.getBody());
    symbolTable.exitScope();
  }

  private void analyzeReturn(ReturnStmt stmt) {
    if (stmt.hasValue()) {
      if (currentFunctionReturnType.getTypeName().equals("void")) {
        throw new SemanticException("void-Funktion darf keinen Wert zurückgeben");
      }
      analyzeExpression(stmt.getValue());
    }
  }

  private void analyzeExprStmt(ExprStmt stmt) {
    analyzeExpression(stmt.getExpression());
  }

  private Type analyzeExpression(Expression expr) {
    if (expr instanceof IntLiteral) {
      return new Type("int");
    } else if (expr instanceof BoolLiteral) {
      return new Type("bool");
    } else if (expr instanceof CharLiteral) {
      return new Type("char");
    } else if (expr instanceof StringLiteral) {
      return new Type("string");
    } else if (expr instanceof IdentifierExpr) {
      return analyzeIdentifier((IdentifierExpr) expr);
    } else if (expr instanceof BinaryExpr) {
      return analyzeBinary((BinaryExpr) expr);
    } else if (expr instanceof UnaryExpr) {
      return analyzeUnary((UnaryExpr) expr);
    } else if (expr instanceof AssignExpr) {
      return analyzeAssign((AssignExpr) expr);
    } else if (expr instanceof FunctionCallExpr) {
      return analyzeFunctionCall((FunctionCallExpr) expr);
    } else if (expr instanceof MethodCallExpr) {
      return analyzeMethodCall((MethodCallExpr) expr);
    } else if (expr instanceof MemberAccessExpr) {
      return analyzeMemberAccess((MemberAccessExpr) expr);
    }
    return new Type("void");
  }

  private Type analyzeIdentifier(IdentifierExpr expr) {
    Symbol symbol = symbolTable.lookupVariable(expr.getName());
    if (symbol == null) {
      throw new SemanticException("Variable '" + expr.getName() + "' nicht definiert");
    }
    return symbol.getType();
  }

  private Type analyzeBinary(BinaryExpr expr) {
    Type left = analyzeExpression(expr.getLeft());
    Type right = analyzeExpression(expr.getRight());

    // Typ-Kompatibilität prüfen (vereinfacht)
    return left;
  }

  private Type analyzeUnary(UnaryExpr expr) {
    return analyzeExpression(expr.getOperand());
  }

  private Type analyzeAssign(AssignExpr expr) {
    if (!isLValue(expr.getTarget())) {
      throw new SemanticException("Linke Seite der Zuweisung muss ein LValue sein");
    }
    analyzeExpression(expr.getTarget());
    analyzeExpression(expr.getValue());
    return analyzeExpression(expr.getValue());
  }

  private Type analyzeFunctionCall(FunctionCallExpr expr) {
    FunctionDecl func =
        symbolTable.lookupFunction(expr.getFunctionName(), expr.getArguments().size());
    if (func == null) {
      // Prüfe auf Built-in Funktionen
      if (isBuiltinFunction(expr.getFunctionName())) {
        return new Type("void");
      }
      // Prüfe ob es ein Konstruktor ist
      ClassDecl cls = symbolTable.lookupClass(expr.getFunctionName());
      if (cls != null) {
        return new Type(cls.getName());
      }
      throw new SemanticException("Funktion '" + expr.getFunctionName() + "' nicht gefunden");
    }

    // Argumente analysieren
    for (Expression arg : expr.getArguments()) {
      analyzeExpression(arg);
    }

    return func.getReturnType();
  }

  private Type analyzeMethodCall(MethodCallExpr expr) {
    analyzeExpression(expr.getObject());
    for (Expression arg : expr.getArguments()) {
      analyzeExpression(arg);
    }
    return new Type("void"); // Vereinfacht
  }

  private Type analyzeMemberAccess(MemberAccessExpr expr) {
    analyzeExpression(expr.getObject());
    return new Type("int"); // Vereinfacht
  }

  private boolean isLValue(Expression expr) {
    return expr instanceof IdentifierExpr || expr instanceof MemberAccessExpr;
  }

  private boolean isBuiltinFunction(String name) {
    return name.equals("print_int")
        || name.equals("print_bool")
        || name.equals("print_char")
        || name.equals("print_string");
  }
}
