grammar MiniCpp;

// ============================================================================
// PARSER RULES (Regeln für die Struktur vom Code)
// ============================================================================

// Ein Programm besteht aus Klassen und Funktionen
program
    : (classDecl | functionDecl)* EOF   // * bedeutet: null oder mehr
    ;                                    // EOF = Ende der Datei

// --- Klassen ---

// Eine Klasse kann von einer anderen Klasse erben (optional)
// Beispiel: class Dog : public Animal { public: ... };
classDecl
    : 'class' IDENTIFIER (':' 'public' IDENTIFIER)? '{' 'public' ':' classMember* '}' ';'?
    ;
    // IDENTIFIER = Name der Klasse
    // (':' 'public' IDENTIFIER)? = optionale Vererbung
    // ';'? = Semikolon am Ende ist optional

// Eine Klasse hat Felder, Methoden oder Konstruktoren
classMember
    : fieldDecl         // Feld (Variable in der Klasse)
    | methodDecl        // Methode (Funktion in der Klasse)
    | constructorDecl   // Konstruktor (erstellt Objekte)
    ;

// Ein Feld ist eine Variable in der Klasse
// Beispiel: int x;
fieldDecl
    : type IDENTIFIER ';'
    ;

// Eine Methode ist eine Funktion in der Klasse
// Beispiel: virtual int getValue() { return x; }
methodDecl
    : 'virtual'? type IDENTIFIER '(' paramList? ')' block
    ;
    // 'virtual'? = virtual ist optional (für Polymorphie)
    // paramList? = Parameter sind optional

// Ein Konstruktor hat den gleichen Namen wie die Klasse
// Beispiel: Point(int a, int b) { x = a; y = b; }
constructorDecl
    : IDENTIFIER '(' paramList? ')' block
    ;

// --- Funktionen ---

// Eine Funktion hat einen Rückgabetyp, Namen, Parameter und Body
// Beispiel: int add(int a, int b) { return a + b; }
functionDecl
    : type IDENTIFIER '(' paramList? ')' block
    ;

// Liste von Parametern, getrennt durch Komma
// Beispiel: int a, int b, int c
paramList
    : param (',' param)*    // mindestens 1 Parameter, dann mehr mit Komma
    ;

// Ein Parameter hat einen Typ und einen Namen
// Das & macht ihn zur Referenz
// Beispiel: int x  oder  int& x
param
    : type '&'? IDENTIFIER
    ;
    // '&'? = Referenz-Zeichen ist optional

// --- Typen ---

// Alle erlaubten Datentypen
type
    : 'bool'        // true oder false
    | 'int'         // ganze Zahlen
    | 'char'        // einzelne Zeichen
    | 'string'      // Text
    | 'void'        // kein Rückgabewert
    | IDENTIFIER    // Klassenname (z.B. Point, Animal)
    ;

// --- Statements (Anweisungen) ---

// Ein Block ist Code in geschweiften Klammern
// Beispiel: { int x = 5; print_int(x); }
block
    : '{' statement* '}'
    ;

// Verschiedene Arten von Anweisungen
statement
    : block                                                     # blockStmt      // verschachtelter Block
    | varDecl                                                   # varDeclStmt    // Variable deklarieren
    | 'if' '(' expression ')' statement ('else' statement)?     # ifStmt         // Bedingung
    | 'while' '(' expression ')' statement                      # whileStmt      // Schleife
    | 'return' expression? ';'                                  # returnStmt     // Rückgabe
    | expression ';'                                            # exprStmt       // Ausdruck als Statement
    | ';'                                                       # emptyStmt      // leeres Statement
    ;
    // # Name = Label für den ASTBuilder

// Variable deklarieren, optional mit Wert
// Beispiel: int x;  oder  int x = 5;  oder  int& r = x;
varDecl
    : type '&'? IDENTIFIER ('=' expression)? ';'
    ;

// --- Expressions (Ausdrücke) ---
// Sortiert nach Priorität: niedrig oben, hoch unten

// Ein Ausdruck ist eine Zuweisung (oder weniger)
expression
    : assignment
    ;

// Zuweisung ist rechtsassoziativ: a = b = c wird zu a = (b = c)
// Beispiel: x = 5
assignment
    : logicalOr ('=' assignment)?
    ;

// Logisches ODER: a || b
// Beispiel: x > 5 || y < 3
logicalOr
    : logicalAnd ('||' logicalAnd)*
    ;

// Logisches UND: a && b
// Beispiel: x > 0 && x < 10
logicalAnd
    : equality ('&&' equality)*
    ;

// Gleichheit prüfen: == und !=
// Beispiel: x == 5  oder  x != 0
equality
    : relational (('==' | '!=') relational)*
    ;

// Vergleiche: <, <=, >, >=
// Beispiel: x < 10  oder  y >= 0
relational
    : additive (('<' | '<=' | '>' | '>=') additive)*
    ;

// Addition und Subtraktion: + und -
// Beispiel: x + y  oder  a - b
additive
    : multiplicative (('+' | '-') multiplicative)*
    ;

// Multiplikation, Division, Modulo: *, /, %
// Beispiel: x * 2  oder  10 / 3  oder  7 % 2
multiplicative
    : unary (('*' | '/' | '%') unary)*
    ;

// Unäre Operatoren: !, +, -
// Beispiel: !true  oder  -5
unary
    : ('!' | '+' | '-') unary   // kann mehrfach sein: --5
    | postfix
    ;

// Postfix: Feldzugriff oder Methodenaufruf nach einem Objekt
// Beispiel: point.x  oder  point.getX()
postfix
    : primary (postfixOp)*
    ;

// Postfix-Operationen
postfixOp
    : '.' IDENTIFIER                    // Feldzugriff: obj.feld
    | '.' IDENTIFIER '(' argList? ')'   // Methodenaufruf: obj.methode()
    ;

// Primäre Ausdrücke (höchste Priorität)
primary
    : '(' expression ')'                # parenExpr      // Klammern: (1 + 2)
    | IDENTIFIER '(' argList? ')'       # functionCall   // Funktionsaufruf: add(1, 2)
    | IDENTIFIER                        # identifierExpr // Variable: x
    | literal                           # literalExpr    // Wert: 5, 'a', "hello"
    ;

// Liste von Argumenten für Funktionsaufrufe
// Beispiel: add(1, 2, 3)
argList
    : expression (',' expression)*
    ;

// Literale sind feste Werte im Code
literal
    : INT_LITERAL       // Zahl: 42
    | CHAR_LITERAL      // Zeichen: 'a'
    | STRING_LITERAL    // Text: "hello"
    | TRUE              // true
    | FALSE             // false
    ;

// ============================================================================
// LEXER RULES (Regeln für einzelne Wörter/Tokens)
// ============================================================================

// --- Schlüsselwörter ---
TRUE        : 'true' ;      // Boolean wahr
FALSE       : 'false' ;     // Boolean falsch

// --- Literale ---

// Ganzzahlen: eine oder mehr Ziffern
// Beispiel: 0, 42, 12345
INT_LITERAL
    : [0-9]+                // + bedeutet: eine oder mehr
    ;

// Zeichen in einfachen Anführungszeichen
// Beispiel: 'a', 'B', '\n'
CHAR_LITERAL
    : '\'' (ESCAPE_SEQ | ~['\\]) '\''
    ;
    // ~ bedeutet: alles außer
    // ~['\\] = alles außer ' und \

// Text in doppelten Anführungszeichen
// Beispiel: "hello", "world\n"
STRING_LITERAL
    : '"' (ESCAPE_SEQ | ~["\\])* '"'
    ;

// Escape-Sequenzen für Sonderzeichen
// \n = neue Zeile, \t = Tab, \0 = Null
fragment ESCAPE_SEQ
    : '\\' [0nrtfb"'\\]
    ;
    // fragment = Hilfsregel, kein eigenes Token

// --- Identifier (Namen) ---

// Namen für Variablen, Funktionen, Klassen
// Muss mit Buchstabe oder _ beginnen
// Beispiel: x, myVar, _private, Point2D
IDENTIFIER
    : [a-zA-Z_][a-zA-Z0-9_]*
    ;

// --- Präprozessor ---

// Zeilen mit # werden ignoriert (wie #include)
PREPROCESSOR
    : '#' ~[\r\n]* -> skip      // skip = überspringen
    ;

// --- Kommentare ---

// Einzeilige Kommentare: // bis Zeilenende
LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

// Mehrzeilige Kommentare: /* bis */
BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;
    // .*? = alles bis zum ersten */

// --- Whitespace ---

// Leerzeichen, Tabs, Zeilenumbrüche werden ignoriert
WS
    : [ \t\r\n]+ -> skip
    ;
