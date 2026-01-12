package interpreter;

import ast.*;
import ast.decl.*;
import ast.expr.*;
import ast.stmt.*;
import java.util.List;
import semantic.Symbol;
import semantic.SymbolTable;

public class Interpreter {
  private SymbolTable symbolTable;
  private Program program;

  public Interpreter(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  public Object execute(Program program) {
    this.program = program;

    // Registriere alle Klassen und Funktionen
    for (ClassDecl cls : program.getClasses()) {
      symbolTable.declareClass(cls);
    }
    for (FunctionDecl func : program.getFunctions()) {
      symbolTable.declareFunction(func);
    }

    // Suche und führe main() aus
    FunctionDecl mainFunc = program.getMainFunction();
    if (mainFunc != null) {
      return executeFunction(mainFunc, List.of());
    }

    return null;
  }

  private Object executeFunction(FunctionDecl func, List<Object> args) {
    symbolTable.enterScope();

    // Parameter binden
    for (int i = 0; i < func.getParameters().size(); i++) {
      Parameter param = func.getParameters().get(i);
      Symbol symbol = new Symbol(param.getName(), param.getType());
      symbol.setValue(args.get(i));
      symbolTable.declareVariable(symbol);
    }

    // Body ausführen
    try {
      executeBlock(func.getBody());
    } catch (ReturnException e) {
      symbolTable.exitScope();
      return e.getValue();
    }

    symbolTable.exitScope();
    return null;
  }

  private Object executeMethod(RuntimeValue object, MethodDecl method, List<Object> args) {
    symbolTable.enterScope();

    // Felder als Variablen verfügbar machen
    for (String fieldName : object.getFields().keySet()) {
      Symbol symbol = new Symbol(fieldName, new Type("int")); // Vereinfacht
      symbol.setValue(object.getField(fieldName));
      symbolTable.declareVariable(symbol);
    }

    // Parameter binden
    for (int i = 0; i < method.getParameters().size(); i++) {
      Parameter param = method.getParameters().get(i);
      Symbol symbol = new Symbol(param.getName(), param.getType());
      symbol.setValue(args.get(i));
      symbolTable.declareVariable(symbol);
    }

    // Body ausführen
    try {
      executeBlock(method.getBody());
    } catch (ReturnException e) {
      // Felder zurückschreiben
      for (String fieldName : object.getFields().keySet()) {
        Symbol symbol = symbolTable.lookupVariable(fieldName);
        if (symbol != null) {
          object.setField(fieldName, symbol.getValue());
        }
      }
      symbolTable.exitScope();
      return e.getValue();
    }

    // Felder zurückschreiben
    for (String fieldName : object.getFields().keySet()) {
      Symbol symbol = symbolTable.lookupVariable(fieldName);
      if (symbol != null) {
        object.setField(fieldName, symbol.getValue());
      }
    }

    symbolTable.exitScope();
    return null;
  }

  private void executeBlock(BlockStmt block) {
    for (Statement stmt : block.getStatements()) {
      executeStatement(stmt);
    }
  }

  private void executeStatement(Statement stmt) {
    if (stmt instanceof VarDeclStmt) {
      executeVarDecl((VarDeclStmt) stmt);
    } else if (stmt instanceof IfStmt) {
      executeIf((IfStmt) stmt);
    } else if (stmt instanceof WhileStmt) {
      executeWhile((WhileStmt) stmt);
    } else if (stmt instanceof ReturnStmt) {
      executeReturn((ReturnStmt) stmt);
    } else if (stmt instanceof ExprStmt) {
      executeExprStmt((ExprStmt) stmt);
    } else if (stmt instanceof BlockStmt) {
      symbolTable.enterScope();
      executeBlock((BlockStmt) stmt);
      symbolTable.exitScope();
    }
  }

  private void executeVarDecl(VarDeclStmt stmt) {
    Object value = getDefaultValue(stmt.getType().getTypeName());

    if (stmt.hasInitializer()) {
      value = evaluate(stmt.getInitializer());
    }

    Symbol symbol = new Symbol(stmt.getName(), stmt.getType());

    // Referenz-Handling
    if (stmt.getType().isReference() && stmt.hasInitializer()) {
      if (stmt.getInitializer() instanceof IdentifierExpr) {
        String targetName = ((IdentifierExpr) stmt.getInitializer()).getName();
        Symbol target = symbolTable.lookupVariable(targetName);
        if (target != null) {
          symbol.setReferenceTo(target);
        }
      }
    } else {
      symbol.setValue(value);
    }

    symbolTable.declareVariable(symbol);
  }

  private void executeIf(IfStmt stmt) {
    Object condition = evaluate(stmt.getCondition());
    boolean result = toBoolean(condition);

    if (result) {
      symbolTable.enterScope();
      executeStatement(stmt.getThenBranch());
      symbolTable.exitScope();
    } else if (stmt.hasElseBranch()) {
      symbolTable.enterScope();
      executeStatement(stmt.getElseBranch());
      symbolTable.exitScope();
    }
  }

  private void executeWhile(WhileStmt stmt) {
    while (toBoolean(evaluate(stmt.getCondition()))) {
      symbolTable.enterScope();
      executeStatement(stmt.getBody());
      symbolTable.exitScope();
    }
  }

  private void executeReturn(ReturnStmt stmt) {
    Object value = null;
    if (stmt.hasValue()) {
      value = evaluate(stmt.getValue());
    }
    throw new ReturnException(value);
  }

  private void executeExprStmt(ExprStmt stmt) {
    evaluate(stmt.getExpression());
  }

  // === Expression Evaluation ===

  private Object evaluate(Expression expr) {
    if (expr instanceof IntLiteral) {
      return ((IntLiteral) expr).getValue();
    } else if (expr instanceof BoolLiteral) {
      return ((BoolLiteral) expr).getValue();
    } else if (expr instanceof CharLiteral) {
      return ((CharLiteral) expr).getValue();
    } else if (expr instanceof StringLiteral) {
      return ((StringLiteral) expr).getValue();
    } else if (expr instanceof IdentifierExpr) {
      return evaluateIdentifier((IdentifierExpr) expr);
    } else if (expr instanceof BinaryExpr) {
      return evaluateBinary((BinaryExpr) expr);
    } else if (expr instanceof UnaryExpr) {
      return evaluateUnary((UnaryExpr) expr);
    } else if (expr instanceof AssignExpr) {
      return evaluateAssign((AssignExpr) expr);
    } else if (expr instanceof FunctionCallExpr) {
      return evaluateFunctionCall((FunctionCallExpr) expr);
    } else if (expr instanceof MethodCallExpr) {
      return evaluateMethodCall((MethodCallExpr) expr);
    } else if (expr instanceof MemberAccessExpr) {
      return evaluateMemberAccess((MemberAccessExpr) expr);
    }
    return null;
  }

  private Object evaluateIdentifier(IdentifierExpr expr) {
    Symbol symbol = symbolTable.lookupVariable(expr.getName());
    if (symbol == null) {
      throw new RuntimeException("Variable '" + expr.getName() + "' nicht definiert");
    }
    return symbol.getValue();
  }

  private Object evaluateBinary(BinaryExpr expr) {
    // Short-circuit für && und ||
    if (expr.getOperator() == BinaryExpr.Operator.AND) {
      Object left = evaluate(expr.getLeft());
      if (!toBoolean(left)) return false;
      return toBoolean(evaluate(expr.getRight()));
    }
    if (expr.getOperator() == BinaryExpr.Operator.OR) {
      Object left = evaluate(expr.getLeft());
      if (toBoolean(left)) return true;
      return toBoolean(evaluate(expr.getRight()));
    }

    Object left = evaluate(expr.getLeft());
    Object right = evaluate(expr.getRight());

    switch (expr.getOperator()) {
      case ADD:
        if (left instanceof String || right instanceof String) {
          return String.valueOf(left) + String.valueOf(right);
        }
        return toInt(left) + toInt(right);
      case SUB:
        return toInt(left) - toInt(right);
      case MUL:
        return toInt(left) * toInt(right);
      case DIV:
        int divisor = toInt(right);
        if (divisor == 0) {
          throw new RuntimeException("Division durch 0");
        }
        return toInt(left) / divisor;
      case MOD:
        int mod = toInt(right);
        if (mod == 0) {
          throw new RuntimeException("Modulo durch 0");
        }
        return toInt(left) % mod;
      case EQ:
        return equals(left, right);
      case NEQ:
        return !equals(left, right);
      case LT:
        return compare(left, right) < 0;
      case LE:
        return compare(left, right) <= 0;
      case GT:
        return compare(left, right) > 0;
      case GE:
        return compare(left, right) >= 0;
      default:
        throw new RuntimeException("Unbekannter Operator: " + expr.getOperator());
    }
  }

  private Object evaluateUnary(UnaryExpr expr) {
    Object operand = evaluate(expr.getOperand());

    switch (expr.getOperator()) {
      case NEG:
        return -toInt(operand);
      case POS:
        return toInt(operand);
      case NOT:
        return !toBoolean(operand);
      default:
        throw new RuntimeException("Unbekannter Operator: " + expr.getOperator());
    }
  }

  private Object evaluateAssign(AssignExpr expr) {
    Object value = evaluate(expr.getValue());

    if (expr.getTarget() instanceof IdentifierExpr) {
      String name = ((IdentifierExpr) expr.getTarget()).getName();
      Symbol symbol = symbolTable.lookupVariable(name);
      if (symbol == null) {
        throw new RuntimeException("Variable '" + name + "' nicht definiert");
      }
      symbol.setValue(value);
    } else if (expr.getTarget() instanceof MemberAccessExpr) {
      MemberAccessExpr access = (MemberAccessExpr) expr.getTarget();
      Object obj = evaluate(access.getObject());
      if (obj instanceof RuntimeValue) {
        ((RuntimeValue) obj).setField(access.getMemberName(), value);
      }
    }

    return value;
  }

  private Object evaluateFunctionCall(FunctionCallExpr expr) {
    String funcName = expr.getFunctionName();

    // Built-in Funktionen
    if (funcName.equals("print_int")) {
      Object arg = evaluate(expr.getArguments().get(0));
      System.out.print(toInt(arg));
      return null;
    }
    if (funcName.equals("print_bool")) {
      Object arg = evaluate(expr.getArguments().get(0));
      System.out.print(toBoolean(arg) ? "true" : "false");
      return null;
    }
    if (funcName.equals("print_char")) {
      Object arg = evaluate(expr.getArguments().get(0));
      System.out.print((char) arg);
      return null;
    }
    if (funcName.equals("print_string")) {
      Object arg = evaluate(expr.getArguments().get(0));
      System.out.print(String.valueOf(arg));
      return null;
    }

    // Konstruktor-Aufruf prüfen
    ClassDecl cls = symbolTable.lookupClass(funcName);
    if (cls != null) {
      return createObject(cls, expr.getArguments());
    }

    // Normale Funktion
    FunctionDecl func = symbolTable.lookupFunction(funcName, expr.getArguments().size());
    if (func == null) {
      throw new RuntimeException("Funktion '" + funcName + "' nicht gefunden");
    }

    // Argumente auswerten
    List<Object> args = new java.util.ArrayList<>();
    for (Expression arg : expr.getArguments()) {
      args.add(evaluate(arg));
    }

    return executeFunction(func, args);
  }

  private Object evaluateMethodCall(MethodCallExpr expr) {
    Object obj = evaluate(expr.getObject());

    if (!(obj instanceof RuntimeValue)) {
      throw new RuntimeException("Methodenaufruf auf Nicht-Objekt");
    }

    RuntimeValue runtimeObj = (RuntimeValue) obj;
    ClassDecl cls = runtimeObj.getClassDecl();

    // Methode suchen (inkl. Vererbung)
    MethodDecl method = findMethod(cls, expr.getMethodName(), expr.getArguments().size());
    if (method == null) {
      throw new RuntimeException(
          "Methode '"
              + expr.getMethodName()
              + "' nicht gefunden in Klasse '"
              + cls.getName()
              + "'");
    }

    // Argumente auswerten
    List<Object> args = new java.util.ArrayList<>();
    for (Expression arg : expr.getArguments()) {
      args.add(evaluate(arg));
    }

    return executeMethod(runtimeObj, method, args);
  }

  private MethodDecl findMethod(ClassDecl cls, String name, int argCount) {
    // In aktueller Klasse suchen
    for (MethodDecl method : cls.getMethods()) {
      if (method.getName().equals(name) && method.getParameters().size() == argCount) {
        return method;
      }
    }

    // In Basisklasse suchen
    if (cls.hasBaseClass()) {
      ClassDecl baseClass = symbolTable.lookupClass(cls.getBaseClass());
      if (baseClass != null) {
        return findMethod(baseClass, name, argCount);
      }
    }

    return null;
  }

  private Object evaluateMemberAccess(MemberAccessExpr expr) {
    Object obj = evaluate(expr.getObject());

    if (obj instanceof RuntimeValue) {
      return ((RuntimeValue) obj).getField(expr.getMemberName());
    }

    throw new RuntimeException("Feldzugriff auf Nicht-Objekt");
  }

  private RuntimeValue createObject(ClassDecl cls, List<Expression> args) {
    RuntimeValue obj = new RuntimeValue(cls);

    // Felder mit Standardwerten initialisieren
    initializeFields(obj, cls);

    // Konstruktor suchen und ausführen
    ConstructorDecl ctor = findConstructor(cls, args.size());
    if (ctor != null) {
      List<Object> evalArgs = new java.util.ArrayList<>();
      for (Expression arg : args) {
        evalArgs.add(evaluate(arg));
      }
      executeConstructor(obj, ctor, evalArgs);
    }

    return obj;
  }

  private void initializeFields(RuntimeValue obj, ClassDecl cls) {
    // Basisklassen-Felder zuerst
    if (cls.hasBaseClass()) {
      ClassDecl baseClass = symbolTable.lookupClass(cls.getBaseClass());
      if (baseClass != null) {
        initializeFields(obj, baseClass);
      }
    }

    // Eigene Felder
    for (FieldDecl field : cls.getFields()) {
      Object defaultValue = getDefaultValue(field.getType().getTypeName());
      obj.setField(field.getName(), defaultValue);
    }
  }

  private ConstructorDecl findConstructor(ClassDecl cls, int argCount) {
    for (ConstructorDecl ctor : cls.getConstructors()) {
      if (ctor.getParameters().size() == argCount) {
        return ctor;
      }
    }
    // Default-Konstruktor (kein expliziter Konstruktor)
    if (argCount == 0) {
      return null;
    }
    return null;
  }

  private void executeConstructor(RuntimeValue obj, ConstructorDecl ctor, List<Object> args) {
    symbolTable.enterScope();

    // Felder verfügbar machen
    for (String fieldName : obj.getFields().keySet()) {
      Symbol symbol = new Symbol(fieldName, new Type("int")); // Vereinfacht
      symbol.setValue(obj.getField(fieldName));
      symbolTable.declareVariable(symbol);
    }

    // Parameter binden
    for (int i = 0; i < ctor.getParameters().size(); i++) {
      Parameter param = ctor.getParameters().get(i);
      Symbol symbol = new Symbol(param.getName(), param.getType());
      symbol.setValue(args.get(i));
      symbolTable.declareVariable(symbol);
    }

    // Body ausführen
    try {
      executeBlock(ctor.getBody());
    } catch (ReturnException e) {
      // Konstruktor ignoriert return-Wert
    }

    // Felder zurückschreiben
    for (String fieldName : obj.getFields().keySet()) {
      Symbol symbol = symbolTable.lookupVariable(fieldName);
      if (symbol != null) {
        obj.setField(fieldName, symbol.getValue());
      }
    }

    symbolTable.exitScope();
  }

  // === Hilfsmethoden ===

  private Object getDefaultValue(String typeName) {
    switch (typeName) {
      case "int":
        return 0;
      case "bool":
        return false;
      case "char":
        return '\0';
      case "string":
        return "";
      default:
        return null;
    }
  }

  private int toInt(Object value) {
    if (value instanceof Integer) return (Integer) value;
    if (value instanceof Character) return (int) (Character) value;
    if (value instanceof Boolean) return (Boolean) value ? 1 : 0;
    throw new RuntimeException("Kann nicht zu int konvertieren: " + value);
  }

  private boolean toBoolean(Object value) {
    if (value instanceof Boolean) return (Boolean) value;
    if (value instanceof Integer) return (Integer) value != 0;
    if (value instanceof Character) return (Character) value != '\0';
    if (value instanceof String) return !((String) value).isEmpty();
    return value != null;
  }

  private boolean equals(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null || b == null) return false;
    return a.equals(b);
  }

  private int compare(Object a, Object b) {
    if (a instanceof Integer && b instanceof Integer) {
      return Integer.compare((Integer) a, (Integer) b);
    }
    if (a instanceof Character && b instanceof Character) {
      return Character.compare((Character) a, (Character) b);
    }
    throw new RuntimeException("Kann Werte nicht vergleichen: " + a + ", " + b);
  }
}
