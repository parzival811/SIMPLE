/* JFlex specification for SIMPLE language lexical analyzer */

import java_cup.runtime.Symbol;

%%

%class SimpleLexer
%unicode
%cup
%line
%column

%{
    // Helper method to create tokens with values
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

/* Macro definitions */
DIGIT = [0-9]
LETTER = [a-zA-Z]
IDENTIFIER = {LETTER}({LETTER}|{DIGIT}|_)*
INTEGER = {DIGIT}+
WHITESPACE = [ \t\f\r\n]+

%%

/* Keywords */
"begin"         { return symbol(sym.BEGIN); }
"end"           { return symbol(sym.END); }
"while"         { return symbol(sym.WHILE); }
"do"            { return symbol(sym.DO); }
"done"          { return symbol(sym.DONE); }
"if"            { return symbol(sym.IF); }
"then"          { return symbol(sym.THEN); }
"else"          { return symbol(sym.ELSE); }
"fi"            { return symbol(sym.FI); }
"function"      { return symbol(sym.FUNCTION); }
"return"        { return symbol(sym.RETURN); }
"int"           { return symbol(sym.INT); }
"bool"          { return symbol(sym.BOOL); }
"void"          { return symbol(sym.VOID); }
"true"          { return symbol(sym.TRUE); }
"false"         { return symbol(sym.FALSE); }

/* Operators */
"="             { return symbol(sym.ASSIGN); }
"+"             { return symbol(sym.PLUS); }
"-"             { return symbol(sym.MINUS); }
"*"             { return symbol(sym.MULTIPLY); }
"/"             { return symbol(sym.DIVIDE); }
"%"             { return symbol(sym.MODULO); }
"<"             { return symbol(sym.LESS_THAN); }
">"             { return symbol(sym.GREATER_THAN); }
"<="            { return symbol(sym.LESS_EQUAL); }
">="            { return symbol(sym.GREATER_EQUAL); }
"=="            { return symbol(sym.EQUALS); }
"!="            { return symbol(sym.NOT_EQUALS); }

/* Delimiters */
"("             { return symbol(sym.LPAREN); }
")"             { return symbol(sym.RPAREN); }
";"             { return symbol(sym.SEMICOLON); }
","             { return symbol(sym.COMMA); }

/* Identifiers and literals */
{IDENTIFIER}    { return symbol(sym.IDENTIFIER, yytext()); }
{INTEGER}       { return symbol(sym.INTEGER, Integer.valueOf(yytext())); }

/* Whitespace - ignore */
{WHITESPACE}    { /* ignore */ }

/* Comments - ignore */
"#"[^\r\n]*     { /* ignore single line comments */ }

/* Error handling */
.               { throw new Error("Illegal character <" + yytext() + "> at line " + (yyline + 1) + ", column " + (yycolumn + 1)); }
