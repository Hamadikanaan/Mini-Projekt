grammar MiniCpp;

// ============================================================================
// PARSER RULES
// ============================================================================

program
    : (classDecl | functionDecl)* EOF
    ;

// --- Klassen ---
classDecl
    : 'class' IDENTIFIER (':' 'public' IDENTIFIER)? '{' 'public' ':' classMember* '}' ';'?
    ;

classMember
    : fieldDecl
    | methodDecl
    | constructorDecl
    ;

fieldDecl
    : type IDENTIFIER ';'
    ;

methodDecl
    : 'virtual'? type IDENTIFIER '(' paramList? ')' block
    ;

constructorDecl
    : IDENTIFIER '(' paramList? ')' block
    ;

// --- Funktionen ---
functionDecl
    : type IDENTIFIER '(' paramList? ')' block
    ;

paramList
    : param (',' param)*
    ;

param
    : type '&'? IDENTIFIER
    ;

// --- Typen ---
type
    : 'bool'
    | 'int'
    | 'char'
    | 'string'
    | 'void'
    | IDENTIFIER      // Klassentyp
    ;

// --- Statements ---
block
    : '{' statement* '}'
    ;

statement
    : block                                                     # blockStmt
    | varDecl                                                   # varDeclStmt
    | 'if' '(' expression ')' statement ('else' statement)?     # ifStmt
    | 'while' '(' expression ')' statement                      # whileStmt
    | 'return' expression? ';'                                  # returnStmt
    | expression ';'                                            # exprStmt
    | ';'                                                       # emptyStmt
    ;

varDecl
    : type '&'? IDENTIFIER ('=' expression)? ';'
    ;

// --- Expressions (Präzedenz von niedrig nach hoch) ---
expression
    : assignment
    ;

assignment
    : logicalOr ('=' assignment)?   // rechtsassoziativ
    ;

logicalOr
    : logicalAnd ('||' logicalAnd)*
    ;

logicalAnd
    : equality ('&&' equality)*
    ;

equality
    : relational (('==' | '!=') relational)*
    ;

relational
    : additive (('<' | '<=' | '>' | '>=') additive)*
    ;

additive
    : multiplicative (('+' | '-') multiplicative)*
    ;

multiplicative
    : unary (('*' | '/' | '%') unary)*
    ;

unary
    : ('!' | '+' | '-') unary
    | postfix
    ;

postfix
    : primary (postfixOp)*
    ;

postfixOp
    : '.' IDENTIFIER                    // Feldzugriff
    | '.' IDENTIFIER '(' argList? ')'   // Methodenaufruf
    ;

primary
    : '(' expression ')'                            # parenExpr
    | IDENTIFIER '(' argList? ')'                   # functionCall
    | IDENTIFIER                                    # identifierExpr
    | literal                                       # literalExpr
    ;

argList
    : expression (',' expression)*
    ;

literal
    : INT_LITERAL
    | CHAR_LITERAL
    | STRING_LITERAL
    | TRUE
    | FALSE
    ;

// ============================================================================
// LEXER RULES
// ============================================================================

// --- Schlüsselwörter ---
TRUE        : 'true' ;
FALSE       : 'false' ;

// --- Literale ---
INT_LITERAL
    : [0-9]+
    ;

CHAR_LITERAL
    : '\'' (ESCAPE_SEQ | ~['\\]) '\''
    ;

STRING_LITERAL
    : '"' (ESCAPE_SEQ | ~["\\])* '"'
    ;

fragment ESCAPE_SEQ
    : '\\' [0nrtfb"'\\]
    ;

// --- Identifier ---
IDENTIFIER
    : [a-zA-Z_][a-zA-Z0-9_]*
    ;

// --- Präprozessor (als Kommentar behandeln) ---
PREPROCESSOR
    : '#' ~[\r\n]* -> skip
    ;

// --- Kommentare ---
LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

// --- Whitespace ---
WS
    : [ \t\r\n]+ -> skip
    ;
