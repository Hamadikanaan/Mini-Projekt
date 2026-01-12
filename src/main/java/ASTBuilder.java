import ast.*;
import ast.decl.*;
import ast.expr.*;
import ast.stmt.*;
import java.util.ArrayList;
import java.util.List;

public class ASTBuilder extends MiniCppBaseVisitor<ASTNode> {

  // ========================================================================
  // Program
  // ========================================================================

  @Override
  public ASTNode visitProgram(MiniCppParser.ProgramContext ctx) {
    List<ClassDecl> classes = new ArrayList<>();
    List<FunctionDecl> functions = new ArrayList<>();

    for (MiniCppParser.ClassDeclContext classCtx : ctx.classDecl()) {
      classes.add((ClassDecl) visit(classCtx));
    }

    for (MiniCppParser.FunctionDeclContext funcCtx : ctx.functionDecl()) {
      functions.add((FunctionDecl) visit(funcCtx));
    }

    return new Program(classes, functions);
  }

  // ========================================================================
  // Klassen
  // ========================================================================

  @Override
  public ASTNode visitClassDecl(MiniCppParser.ClassDeclContext ctx) {
    String name = ctx.IDENTIFIER(0).getText();
    String baseClass = null;

    // Vererbung: class D : public B
    if (ctx.IDENTIFIER().size() > 1) {
      baseClass = ctx.IDENTIFIER(1).getText();
    }

    List<FieldDecl> fields = new ArrayList<>();
    List<MethodDecl> methods = new ArrayList<>();
    List<ConstructorDecl> constructors = new ArrayList<>();

    for (MiniCppParser.ClassMemberContext member : ctx.classMember()) {
      if (member.fieldDecl() != null) {
        fields.add((FieldDecl) visit(member.fieldDecl()));
      } else if (member.methodDecl() != null) {
        methods.add((MethodDecl) visit(member.methodDecl()));
      } else if (member.constructorDecl() != null) {
        constructors.add((ConstructorDecl) visit(member.constructorDecl()));
      }
    }

    return new ClassDecl(name, baseClass, fields, methods, constructors);
  }

  @Override
  public ASTNode visitFieldDecl(MiniCppParser.FieldDeclContext ctx) {
    Type type = buildType(ctx.type(), false);
    String name = ctx.IDENTIFIER().getText();
    return new FieldDecl(type, name);
  }

  @Override
  public ASTNode visitMethodDecl(MiniCppParser.MethodDeclContext ctx) {
    boolean isVirtual = ctx.getChild(0).getText().equals("virtual");
    Type returnType = buildType(ctx.type(), false);
    String name = ctx.IDENTIFIER().getText();
    List<Parameter> params = buildParameters(ctx.paramList());
    BlockStmt body = (BlockStmt) visit(ctx.block());

    return new MethodDecl(name, returnType, params, body, isVirtual);
  }

  @Override
  public ASTNode visitConstructorDecl(MiniCppParser.ConstructorDeclContext ctx) {
    String className = ctx.IDENTIFIER().getText();
    List<Parameter> params = buildParameters(ctx.paramList());
    BlockStmt body = (BlockStmt) visit(ctx.block());

    return new ConstructorDecl(className, params, body);
  }

  // ========================================================================
  // Funktionen
  // ========================================================================

  @Override
  public ASTNode visitFunctionDecl(MiniCppParser.FunctionDeclContext ctx) {
    Type returnType = buildType(ctx.type(), false);
    String name = ctx.IDENTIFIER().getText();
    List<Parameter> params = buildParameters(ctx.paramList());
    BlockStmt body = (BlockStmt) visit(ctx.block());

    return new FunctionDecl(name, returnType, params, body);
  }

  // ========================================================================
  // Statements
  // ========================================================================

  @Override
  public ASTNode visitBlock(MiniCppParser.BlockContext ctx) {
    List<Statement> statements = new ArrayList<>();
    for (MiniCppParser.StatementContext stmtCtx : ctx.statement()) {
      ASTNode node = visit(stmtCtx);
      if (node != null) {
        statements.add((Statement) node);
      }
    }
    return new BlockStmt(statements);
  }

  @Override
  public ASTNode visitBlockStmt(MiniCppParser.BlockStmtContext ctx) {
    return visit(ctx.block());
  }

  @Override
  public ASTNode visitVarDeclStmt(MiniCppParser.VarDeclStmtContext ctx) {
    return visit(ctx.varDecl());
  }

  @Override
  public ASTNode visitVarDecl(MiniCppParser.VarDeclContext ctx) {
    boolean isRef = ctx.getChildCount() > 2 && ctx.getChild(1).getText().equals("&");
    Type type = buildType(ctx.type(), isRef);
    String name = ctx.IDENTIFIER().getText();
    Expression init = null;

    if (ctx.expression() != null) {
      init = (Expression) visit(ctx.expression());
    }

    return new VarDeclStmt(type, name, init);
  }

  @Override
  public ASTNode visitIfStmt(MiniCppParser.IfStmtContext ctx) {
    Expression condition = (Expression) visit(ctx.expression());
    Statement thenBranch = (Statement) visit(ctx.statement(0));
    Statement elseBranch = null;

    if (ctx.statement().size() > 1) {
      elseBranch = (Statement) visit(ctx.statement(1));
    }

    return new IfStmt(condition, thenBranch, elseBranch);
  }

  @Override
  public ASTNode visitWhileStmt(MiniCppParser.WhileStmtContext ctx) {
    Expression condition = (Expression) visit(ctx.expression());
    Statement body = (Statement) visit(ctx.statement());
    return new WhileStmt(condition, body);
  }

  @Override
  public ASTNode visitReturnStmt(MiniCppParser.ReturnStmtContext ctx) {
    Expression value = null;
    if (ctx.expression() != null) {
      value = (Expression) visit(ctx.expression());
    }
    return new ReturnStmt(value);
  }

  @Override
  public ASTNode visitExprStmt(MiniCppParser.ExprStmtContext ctx) {
    Expression expr = (Expression) visit(ctx.expression());
    return new ExprStmt(expr);
  }

  @Override
  public ASTNode visitEmptyStmt(MiniCppParser.EmptyStmtContext ctx) {
    return null; // Leere Statements ignorieren
  }

  // ========================================================================
  // Expressions
  // ========================================================================

  @Override
  public ASTNode visitExpression(MiniCppParser.ExpressionContext ctx) {
    return visit(ctx.assignment());
  }

  @Override
  public ASTNode visitAssignment(MiniCppParser.AssignmentContext ctx) {
    if (ctx.assignment() != null) {
      // Zuweisung: a = b
      Expression target = (Expression) visit(ctx.logicalOr());
      Expression value = (Expression) visit(ctx.assignment());
      return new AssignExpr(target, value);
    }
    return visit(ctx.logicalOr());
  }

  @Override
  public ASTNode visitLogicalOr(MiniCppParser.LogicalOrContext ctx) {
    Expression left = (Expression) visit(ctx.logicalAnd(0));

    for (int i = 1; i < ctx.logicalAnd().size(); i++) {
      Expression right = (Expression) visit(ctx.logicalAnd(i));
      left = new BinaryExpr(left, BinaryExpr.Operator.OR, right);
    }

    return left;
  }

  @Override
  public ASTNode visitLogicalAnd(MiniCppParser.LogicalAndContext ctx) {
    Expression left = (Expression) visit(ctx.equality(0));

    for (int i = 1; i < ctx.equality().size(); i++) {
      Expression right = (Expression) visit(ctx.equality(i));
      left = new BinaryExpr(left, BinaryExpr.Operator.AND, right);
    }

    return left;
  }

  @Override
  public ASTNode visitEquality(MiniCppParser.EqualityContext ctx) {
    Expression left = (Expression) visit(ctx.relational(0));

    for (int i = 1; i < ctx.relational().size(); i++) {
      String op = ctx.getChild(i * 2 - 1).getText();
      Expression right = (Expression) visit(ctx.relational(i));
      BinaryExpr.Operator operator =
          op.equals("==") ? BinaryExpr.Operator.EQ : BinaryExpr.Operator.NEQ;
      left = new BinaryExpr(left, operator, right);
    }

    return left;
  }

  @Override
  public ASTNode visitRelational(MiniCppParser.RelationalContext ctx) {
    Expression left = (Expression) visit(ctx.additive(0));

    for (int i = 1; i < ctx.additive().size(); i++) {
      String op = ctx.getChild(i * 2 - 1).getText();
      Expression right = (Expression) visit(ctx.additive(i));
      BinaryExpr.Operator operator;

      switch (op) {
        case "<":
          operator = BinaryExpr.Operator.LT;
          break;
        case "<=":
          operator = BinaryExpr.Operator.LE;
          break;
        case ">":
          operator = BinaryExpr.Operator.GT;
          break;
        case ">=":
          operator = BinaryExpr.Operator.GE;
          break;
        default:
          throw new RuntimeException("Unknown operator: " + op);
      }

      left = new BinaryExpr(left, operator, right);
    }

    return left;
  }

  @Override
  public ASTNode visitAdditive(MiniCppParser.AdditiveContext ctx) {
    Expression left = (Expression) visit(ctx.multiplicative(0));

    for (int i = 1; i < ctx.multiplicative().size(); i++) {
      String op = ctx.getChild(i * 2 - 1).getText();
      Expression right = (Expression) visit(ctx.multiplicative(i));
      BinaryExpr.Operator operator =
          op.equals("+") ? BinaryExpr.Operator.ADD : BinaryExpr.Operator.SUB;
      left = new BinaryExpr(left, operator, right);
    }

    return left;
  }

  @Override
  public ASTNode visitMultiplicative(MiniCppParser.MultiplicativeContext ctx) {
    Expression left = (Expression) visit(ctx.unary(0));

    for (int i = 1; i < ctx.unary().size(); i++) {
      String op = ctx.getChild(i * 2 - 1).getText();
      Expression right = (Expression) visit(ctx.unary(i));
      BinaryExpr.Operator operator;

      switch (op) {
        case "*":
          operator = BinaryExpr.Operator.MUL;
          break;
        case "/":
          operator = BinaryExpr.Operator.DIV;
          break;
        case "%":
          operator = BinaryExpr.Operator.MOD;
          break;
        default:
          throw new RuntimeException("Unknown operator: " + op);
      }

      left = new BinaryExpr(left, operator, right);
    }

    return left;
  }

  @Override
  public ASTNode visitUnary(MiniCppParser.UnaryContext ctx) {
    if (ctx.unary() != null) {
      String op = ctx.getChild(0).getText();
      Expression operand = (Expression) visit(ctx.unary());
      UnaryExpr.Operator operator;

      switch (op) {
        case "!":
          operator = UnaryExpr.Operator.NOT;
          break;
        case "+":
          operator = UnaryExpr.Operator.POS;
          break;
        case "-":
          operator = UnaryExpr.Operator.NEG;
          break;
        default:
          throw new RuntimeException("Unknown unary operator: " + op);
      }

      return new UnaryExpr(operator, operand);
    }
    return visit(ctx.postfix());
  }

  @Override
  public ASTNode visitPostfix(MiniCppParser.PostfixContext ctx) {
    Expression expr = (Expression) visit(ctx.primary());

    for (MiniCppParser.PostfixOpContext opCtx : ctx.postfixOp()) {
      expr = buildPostfixOp(expr, opCtx);
    }

    return expr;
  }

  private Expression buildPostfixOp(Expression expr, MiniCppParser.PostfixOpContext ctx) {
    String memberName = ctx.IDENTIFIER().getText();

    if (ctx.argList() != null || ctx.getText().contains("()")) {
      // Methodenaufruf: obj.method(args)
      List<Expression> args = new ArrayList<>();
      if (ctx.argList() != null) {
        for (MiniCppParser.ExpressionContext argCtx : ctx.argList().expression()) {
          args.add((Expression) visit(argCtx));
        }
      }
      return new MethodCallExpr(expr, memberName, args);
    } else {
      // Feldzugriff: obj.field
      return new MemberAccessExpr(expr, memberName);
    }
  }

  // ========================================================================
  // Primary Expressions
  // ========================================================================

  @Override
  public ASTNode visitParenExpr(MiniCppParser.ParenExprContext ctx) {
    return visit(ctx.expression());
  }

  @Override
  public ASTNode visitFunctionCall(MiniCppParser.FunctionCallContext ctx) {
    String funcName = ctx.IDENTIFIER().getText();
    List<Expression> args = new ArrayList<>();

    if (ctx.argList() != null) {
      for (MiniCppParser.ExpressionContext argCtx : ctx.argList().expression()) {
        args.add((Expression) visit(argCtx));
      }
    }

    return new FunctionCallExpr(funcName, args);
  }

  @Override
  public ASTNode visitIdentifierExpr(MiniCppParser.IdentifierExprContext ctx) {
    return new IdentifierExpr(ctx.IDENTIFIER().getText());
  }

  @Override
  public ASTNode visitLiteralExpr(MiniCppParser.LiteralExprContext ctx) {
    return visit(ctx.literal());
  }

  @Override
  public ASTNode visitLiteral(MiniCppParser.LiteralContext ctx) {
    if (ctx.INT_LITERAL() != null) {
      return new IntLiteral(Integer.parseInt(ctx.INT_LITERAL().getText()));
    } else if (ctx.TRUE() != null) {
      return new BoolLiteral(true);
    } else if (ctx.FALSE() != null) {
      return new BoolLiteral(false);
    } else if (ctx.CHAR_LITERAL() != null) {
      String text = ctx.CHAR_LITERAL().getText();
      char value = parseCharLiteral(text);
      return new CharLiteral(value);
    } else if (ctx.STRING_LITERAL() != null) {
      String text = ctx.STRING_LITERAL().getText();
      String value = parseStringLiteral(text);
      return new StringLiteral(value);
    }
    throw new RuntimeException("Unknown literal type: " + ctx.getText());
  }

  // ========================================================================
  // Helper Methods
  // ========================================================================

  private Type buildType(MiniCppParser.TypeContext ctx, boolean isReference) {
    String typeName = ctx.getText();
    return new Type(typeName, isReference);
  }

  private List<Parameter> buildParameters(MiniCppParser.ParamListContext ctx) {
    List<Parameter> params = new ArrayList<>();
    if (ctx != null) {
      for (MiniCppParser.ParamContext paramCtx : ctx.param()) {
        boolean isRef = paramCtx.getChildCount() > 2;
        Type type = buildType(paramCtx.type(), isRef);
        String name = paramCtx.IDENTIFIER().getText();
        params.add(new Parameter(type, name));
      }
    }
    return params;
  }

  private char parseCharLiteral(String text) {
    // Entferne Anführungszeichen: 'x' -> x
    String inner = text.substring(1, text.length() - 1);
    if (inner.startsWith("\\")) {
      // Escape-Sequenz
      switch (inner.charAt(1)) {
        case 'n':
          return '\n';
        case 't':
          return '\t';
        case 'r':
          return '\r';
        case '0':
          return '\0';
        case '\\':
          return '\\';
        case '\'':
          return '\'';
        default:
          return inner.charAt(1);
      }
    }
    return inner.charAt(0);
  }

  private String parseStringLiteral(String text) {
    // Entferne Anführungszeichen: "abc" -> abc
    String inner = text.substring(1, text.length() - 1);
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < inner.length(); i++) {
      if (inner.charAt(i) == '\\' && i + 1 < inner.length()) {
        switch (inner.charAt(i + 1)) {
          case 'n':
            result.append('\n');
            i++;
            break;
          case 't':
            result.append('\t');
            i++;
            break;
          case 'r':
            result.append('\r');
            i++;
            break;
          case '0':
            result.append('\0');
            i++;
            break;
          case '\\':
            result.append('\\');
            i++;
            break;
          case '"':
            result.append('"');
            i++;
            break;
          default:
            result.append(inner.charAt(i));
        }
      } else {
        result.append(inner.charAt(i));
      }
    }

    return result.toString();
  }
}
