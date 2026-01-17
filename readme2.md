# MiniCpp Interpreter

Ein Interpreter für eine C++-ähnliche Sprache, entwickelt als Projekt für den Compilerbau-Kurs (IFM5).

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

## Team

- [Name 1]
- [Name 2]
- [Name 3]

## Kurs

Compilerbau (IFM5) – Wintersemester 2025/26
```

---

## Präsentation (5 Minuten)

### Folie 1: Einleitung (30 Sekunden)

> "Unser Projekt ist ein MiniCpp Interpreter. Er kann eine vereinfachte Version von C++ lesen und direkt ausführen – ohne zu kompilieren."

### Folie 2: Architektur (1 Minute)

> "Der Code durchläuft 5 Schritte:
> 1. **Lexer** – zerlegt den Code in Tokens, also einzelne Wörter
> 2. **Parser** – prüft die Grammatik und baut einen Baum
> 3. **ASTBuilder** – vereinfacht den Baum zu einem AST
> 4. **SemanticAnalyzer** – prüft Typen und Variablen
> 5. **Interpreter** – führt den Code aus
>
> Die Grammatik haben wir mit ANTLR definiert, der Rest ist in Java."

### Folie 3: Features (1 Minute)

> "Was kann unser Interpreter?
> - Alle Grundtypen: int, bool, char, string
> - Variablen und Referenzen
> - if-else und while-Schleifen
> - Funktionen mit Überladung und Rekursion
> - Klassen mit Feldern und Methoden
> - Vererbung und virtual für Polymorphie
> - Eine interaktive REPL"

### Folie 4: Live-Demo (2 Minuten)

**Demo 1:** Einfache Rechnung
```
>>> int x = 5;
>>> print_int(x * 2);
10
```

**Demo 2:** Funktion
```
>>> int add(int a, int b) { return a + b; }
>>> print_int(add(3, 4));
7
```

**Demo 3:** Datei laden
```
.\gradlew run --args="src\main\resources\cpp\tests\pos\GOLD01_basics.cpp"