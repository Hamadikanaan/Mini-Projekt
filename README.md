# MiniCpp Interpreter

Ein Interpreter für eine C++-ähnliche Sprache, entwickelt als Projekt für den Compilerbau-Kurs.

---

## Features

| Feature | Beispiel |
|---------|----------|
| Datentypen | `int`, `bool`, `char`, `string`, `void` |
| Variablen | `int x = 5;` |
| Referenzen | `int& ref = x;` |
| Operatoren | `+`, `-`, `*`, `/`, `%`, `==`, `!=`, `<`, `>`, `&&`, `||` |
| Kontrollfluss | `if`, `else`, `while`, `return` |
| Funktionen | `int add(int a, int b) { return a + b; }` |
| Klassen | Felder, Methoden, Konstruktoren |
| Vererbung | `class D : public B { }` |
| Virtual | Dynamischer Dispatch mit `virtual` |
| Ausgabe | `print_int()`, `print_bool()`, `print_char()`, `print_string()` |

---

## Architektur

```
C++ Code → Lexer → Parser → AST → Semantische Analyse → Interpreter → Ausgabe
```

| Komponente | Datei | Aufgabe |
|------------|-------|---------|
| Grammatik | `MiniCpp.g4` | Definiert die Sprachregeln |
| Lexer/Parser | (ANTLR generiert) | Zerlegt und prüft den Code |
| AST | `ast/*.java` | Baumstruktur des Codes |
| ASTBuilder | `ASTBuilder.java` | Parse-Tree → AST |
| SymbolTable | `semantic/SymbolTable.java` | Verwaltet Variablen und Scopes |
| SemanticAnalyzer | `semantic/SemanticAnalyzer.java` | Typprüfung |
| Interpreter | `interpreter/Interpreter.java` | Führt den Code aus |
| REPL | `Main.java` | Interaktive Konsole |

---

## Projektstruktur

```
src/main/
├── antlr/
│   └── MiniCpp.g4              # Grammatik
│
├── java/
│   ├── Main.java               # REPL
│   ├── ASTBuilder.java         # Parse-Tree → AST
│   │
│   ├── ast/                    # AST-Klassen
│   │   ├── ASTNode.java
│   │   ├── Program.java
│   │   ├── Type.java
│   │   ├── decl/               # Deklarationen
│   │   ├── expr/               # Ausdrücke
│   │   └── stmt/               # Statements
│   │
│   ├── semantic/               # Semantische Analyse
│   │   ├── Symbol.java
│   │   ├── SymbolTable.java
│   │   ├── SemanticAnalyzer.java
│   │   └── SemanticException.java
│   │
│   └── interpreter/            # Interpreter
│       ├── Interpreter.java
│       ├── ReturnException.java
│       └── RuntimeValue.java
│
└── resources/cpp/tests/        # Testdateien
    ├── pos/                    # Positive Tests
    └── neg/                    # Negative Tests
```

---

## Installation

### Voraussetzungen

- Java SE Development Kit 21+ ([Download](https://jdk.java.net/21/))
- IntelliJ IDEA (empfohlen) oder andere IDE

### Setup

1. Projekt in IntelliJ öffnen: `File → Open → Projektordner auswählen`
2. Als Gradle-Projekt importieren
3. Gradle JVM auf Java 21 setzen

---

## Verwendung

### Bauen

```bash
./gradlew build
```

### Starten

```bash
./gradlew run
```

### REPL verwenden

```
MiniCpp Interpreter - REPL
Eingabe 'exit' zum Beenden

>>> int main() { print_int(42); return 0; }
42
main() returned: 0

>>> exit
Auf Wiedersehen!
```

### Datei ausführen

```bash
./gradlew run --args="pfad/zur/datei.cpp"
```

---

## Beispiele

### Variablen und Arithmetik
```cpp
int main() {
    int x = 10;
    int y = 3;
    print_int(x + y);    // 13
    print_int(x * y);    // 30
    return 0;
}
```

### While-Schleife
```cpp
int main() {
    int i = 0;
    while (i < 5) {
        print_int(i);
        i = i + 1;
    }
    return 0;
}
// Ausgabe: 01234
```

### Funktionen
```cpp
int factorial(int n) {
    if (n <= 1) { return 1; }
    return n * factorial(n - 1);
}

int main() {
    print_int(factorial(5));  // 120
    return 0;
}
```

### Klassen
```cpp
class Counter {
public:
    int val;
    Counter() { val = 0; }
    void inc() { val = val + 1; }
    int get() { return val; }
}

int main() {
    Counter c;
    c.inc();
    c.inc();
    print_int(c.get());  // 2
    return 0;
}
```

---

## Gradle-Tasks

| Task | Beschreibung |
|------|--------------|
| `./gradlew build` | Projekt bauen |
| `./gradlew run` | Interpreter starten |
| `./gradlew clean` | Build-Dateien löschen |
| `./gradlew spotlessApply` | Code formatieren |
| `./gradlew check` | Tests ausführen |
| `./gradlew generateGrammarSource` | ANTLR-Grammatik neu generieren |

---

## Team

3er-Team Projekt für Compilerbau (IFM5)

---

## Lizenz

Basiert auf dem [Student Support Code Template](https://github.com/Compiler-CampusMinden/student-support-code-template) von Carsten Gips.
