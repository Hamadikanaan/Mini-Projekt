import ast.*;
import ast.decl.*;
import ast.stmt.*;
import interpreter.*;
import java.io.*;
import java.nio.file.*;
import java.util.Scanner;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import semantic.*;

public class Main {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    // Optional: Datei beim Start laden
    if (args.length > 0) {
      try {
        String code = Files.readString(Path.of(args[0]));
        System.out.println("Lade Datei: " + args[0]);
        parseAndRun(code, false);
      } catch (IOException e) {
        System.err.println("Fehler beim Laden: " + e.getMessage());
      }
    }

    // REPL starten
    System.out.println("MiniCpp Interpreter - REPL");
    System.out.println("Eingabe 'exit' zum Beenden");
    System.out.println();

    StringBuilder inputBuffer = new StringBuilder();
    boolean multiLine = false;

    while (true) {
      // Prompt anzeigen
      if (multiLine) {
        System.out.print("...> ");
      } else {
        System.out.print(">>> ");
      }

      String line = scanner.nextLine();

      // Exit-Befehl
      if (!multiLine && line.trim().equals("exit")) {
        System.out.println("Auf Wiedersehen!");
        break;
      }

      inputBuffer.append(line).append("\n");
      String input = inputBuffer.toString();

      // Prüfen ob Eingabe vollständig ist (einfache Heuristik)
      int braces = countChar(input, '{') - countChar(input, '}');
      int parens = countChar(input, '(') - countChar(input, ')');

      if (braces > 0 || parens > 0) {
        multiLine = true;
        continue;
      }

      // Eingabe verarbeiten
      if (!input.trim().isEmpty()) {
        parseAndRun(input, true);
      }

      inputBuffer.setLength(0);
      multiLine = false;
    }

    scanner.close();
  }

  private static void parseAndRun(String input, boolean isRepl) {
    try {
      // Lexer erstellen
      MiniCppLexer lexer = new MiniCppLexer(CharStreams.fromString(input));
      lexer.removeErrorListeners();
      lexer.addErrorListener(
          new BaseErrorListener() {
            @Override
            public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {
              System.err.println(
                  "Lexer-Fehler Zeile " + line + ":" + charPositionInLine + " - " + msg);
            }
          });

      // Parser erstellen
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      MiniCppParser parser = new MiniCppParser(tokens);
      parser.removeErrorListeners();
      parser.addErrorListener(
          new BaseErrorListener() {
            @Override
            public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {
              System.err.println(
                  "Parser-Fehler Zeile " + line + ":" + charPositionInLine + " - " + msg);
            }
          });

      // Parsen
      ParseTree tree = parser.program();

      // Bei Fehlern abbrechen
      if (parser.getNumberOfSyntaxErrors() > 0) {
        return;
      }

      // AST bauen
      ASTBuilder builder = new ASTBuilder();
      Program program = (Program) builder.visit(tree);

      // AST ausgeben (zum Testen)
      printAST(program);

      // TODO: Später SemanticAnalyzer und Interpreter hinzufügen
      SymbolTable symbolTable = new SymbolTable();
      SemanticAnalyzer analyzer = new SemanticAnalyzer(symbolTable);
      analyzer.analyze(program);

      Interpreter interpreter = new Interpreter(symbolTable);
      interpreter.execute(program);

    } catch (Exception e) {
      System.err.println("Fehler: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void printAST(Program program) {
    System.out.println("\n=== AST ===");

    // Klassen ausgeben
    for (ClassDecl cls : program.getClasses()) {
      System.out.println("Klasse: " + cls.getName());
      if (cls.hasBaseClass()) {
        System.out.println("  extends: " + cls.getBaseClass());
      }
      for (FieldDecl field : cls.getFields()) {
        System.out.println("  Feld: " + field.getType() + " " + field.getName());
      }
      for (ConstructorDecl ctor : cls.getConstructors()) {
        System.out.println(
            "  Konstruktor: "
                + ctor.getClassName()
                + "("
                + ctor.getParameters().size()
                + " params)");
      }
      for (MethodDecl method : cls.getMethods()) {
        String virtual = method.isVirtual() ? "virtual " : "";
        System.out.println(
            "  Methode: "
                + virtual
                + method.getReturnType()
                + " "
                + method.getName()
                + "("
                + method.getParameters().size()
                + " params)");
      }
    }

    // Funktionen ausgeben
    for (FunctionDecl func : program.getFunctions()) {
      System.out.println(
          "Funktion: "
              + func.getReturnType()
              + " "
              + func.getName()
              + "("
              + func.getParameters().size()
              + " params)");
      printStatements(func.getBody().getStatements(), "  ");
    }

    System.out.println("=== Ende AST ===\n");
  }

  private static void printStatements(java.util.List<Statement> stmts, String indent) {
    for (Statement stmt : stmts) {
      if (stmt instanceof VarDeclStmt) {
        VarDeclStmt v = (VarDeclStmt) stmt;
        System.out.println(
            indent
                + "VarDecl: "
                + v.getType()
                + " "
                + v.getName()
                + (v.hasInitializer() ? " = ..." : ""));
      } else if (stmt instanceof ReturnStmt) {
        ReturnStmt r = (ReturnStmt) stmt;
        System.out.println(indent + "Return" + (r.hasValue() ? " ..." : ""));
      } else if (stmt instanceof IfStmt) {
        IfStmt i = (IfStmt) stmt;
        System.out.println(indent + "If (...)");
        System.out.println(indent + "  Then:");
        if (i.getThenBranch() instanceof BlockStmt) {
          printStatements(((BlockStmt) i.getThenBranch()).getStatements(), indent + "    ");
        }
        if (i.hasElseBranch()) {
          System.out.println(indent + "  Else:");
          if (i.getElseBranch() instanceof BlockStmt) {
            printStatements(((BlockStmt) i.getElseBranch()).getStatements(), indent + "    ");
          }
        }
      } else if (stmt instanceof WhileStmt) {
        WhileStmt w = (WhileStmt) stmt;
        System.out.println(indent + "While (...)");
        if (w.getBody() instanceof BlockStmt) {
          printStatements(((BlockStmt) w.getBody()).getStatements(), indent + "  ");
        }
      } else if (stmt instanceof ExprStmt) {
        System.out.println(indent + "ExprStmt: ...");
      } else if (stmt instanceof BlockStmt) {
        System.out.println(indent + "Block:");
        printStatements(((BlockStmt) stmt).getStatements(), indent + "  ");
      }
    }
  }

  private static int countChar(String s, char c) {
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == c) count++;
    }
    return count;
  }
}
