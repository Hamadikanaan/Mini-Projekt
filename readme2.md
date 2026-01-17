# MiniCpp Interpreter

Ein Interpreter für eine C++-ähnliche Sprache, entwickelt als Projekt für den Compilerbau

## Was ist das?

Dieses Projekt ist ein einfacher Interpreter, der eine Teilmenge von C++ versteht und ausführt. Man kann Code eingeben und der Interpreter führt ihn direkt aus – ohne vorher zu kompilieren.

## Features

- **Datentypen:** int, bool, char, string, void
- **Variablen:** Deklaration und Zuweisung
- **Referenzen:** `int& r = x;`
- **Operatoren:** +, -, *, /, %, ==, !=, <, >, <=, >=, &&, ||, !
- **Kontrollfluss:** if-else, while, return
- **Funktionen:** Definition, Aufruf, Überladung, Rekursion
- **Klassen:** Felder, Methoden, Konstruktoren
- **Vererbung:** Einfachvererbung mit `class B : public A`
- **Virtual:** Dynamischer Dispatch mit `virtual`
- **Ausgabe:** print_int(), print_bool(), print_char(), print_string()

## Architektur
```
C++ Code → Lexer → Parser → AST → Semantische Analyse → Interpreter → Ausgabe
```

| Komponente | Aufgabe |
|------------|---------|
| Lexer | Zerlegt Code in Tokens (Wörter) |
| Parser | Prüft Grammatik, baut Parse-Tree |
| ASTBuilder | Wandelt Parse-Tree in AST um |
| SemanticAnalyzer | Typprüfung, Scope-Check |
| Interpreter | Führt den Code aus |
| REPL | Interaktive Konsole |

## Installation

**Voraussetzungen:**
- Java 21 oder neuer
- Gradle

**Bauen:**
```
./gradlew build
```

**Starten:**
```
./gradlew run
```

## Verwendung

### REPL (Interaktiv)
```
>>> int x = 5;
>>> print_int(x * 2);
10
>>> exit
Auf Wiedersehen!
```

### Datei laden
```
./gradlew run --args="pfad/zur/datei.cpp"
```

### REPL-Befehle

- `exit` – Beenden
- `reset` – Sitzung zurücksetzen

## Beispiele

**Variablen und Arithmetik:**
```cpp
int main() {
    int x = 10;
    int y = 3;
    print_int(x + y);    // 13
    return 0;
}
```

**Funktion mit Rekursion:**
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

**Klasse:**
```cpp
class Point {
public:
    int x;
    int y;
};
int main() {
    Point p;
    p.x = 10;
    p.y = 20;
    print_int(p.x + p.y);  // 30
    return 0;
}
```

## Projektstruktur
```
src/main/
├── antlr/MiniCpp.g4           # Grammatik
├── java/
│   ├── Main.java              # REPL
│   ├── ASTBuilder.java        # Parse-Tree → AST
│   ├── ast/                   # AST-Klassen
│   ├── semantic/              # Semantische Analyse
│   └── interpreter/           # Interpreter
└── resources/cpp/tests/       # Testdateien
```

```

---

