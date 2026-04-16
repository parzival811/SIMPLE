# SIMPLE Language: Abstract Syntax and Operational Semantics

## Abstract Syntax

### Program Structure
```
Program ::= ScopedBlock

ScopedBlock ::= "begin" BlockContent "end"

BlockContent ::= Declarations FunctionDefinitions StatementList
              | Declarations FunctionDefinitions  
              | Declarations StatementList
              | Declarations
              | FunctionDefinitions StatementList
              | FunctionDefinitions
              | StatementList
              | ε  // empty

Declarations ::= Declarations VariableDeclaration ";"
              | VariableDeclaration ";"

FunctionDefinitions ::= FunctionDefinitions FunctionDeclaration
                     | FunctionDeclaration

StatementList ::= StatementList Statement
               | Statement
```

### Statements

```
Statement ::= SimpleStatement ";"
           | CompoundStatement

SimpleStatement ::= Assignment
                 | ReturnStatement
                 | FunctionCall

CompoundStatement ::= WhileStatement
                   | IfStatement
                   | ScopedBlock
```

#### Variable Declaration
```
VariableDeclaration ::= Type IDENTIFIER

Type ::= "int" | "bool" | "void"
```

#### Assignment Statement
```
Assignment ::= Variable "=" Expression
```

#### Return Statement
```
ReturnStatement ::= "return" Expression
                 | "return"
```

#### Control Flow
```
WhileStatement ::= "while" "(" Expression ")" "do" BlockContent "done"

IfStatement ::= "if" "(" Expression ")" "then" BlockContent "fi"
             | "if" "(" Expression ")" "then" BlockContent "else" BlockContent "fi"
```

#### Function Declaration
```
FunctionDeclaration ::= "function" Type IDENTIFIER "(" ParameterList ")" "begin" BlockContent "end"

ParameterList ::= ParameterListNonEmpty
               | ε  // empty

ParameterListNonEmpty ::= ParameterListNonEmpty "," Parameter
                       | Parameter

Parameter ::= Type IDENTIFIER
```

#### Function Call
```
FunctionCall ::= IDENTIFIER "(" ArgumentList ")"

ArgumentList ::= ArgumentListNonEmpty
              | ε  // empty

ArgumentListNonEmpty ::= ArgumentListNonEmpty "," Expression
                      | Expression
```

### Expressions
```
Expression ::= Expression "==" Expression     // precedence: lowest
            | Expression "!=" Expression
            | Expression "<" Expression       // precedence: comparison
            | Expression ">" Expression
            | Expression "<=" Expression
            | Expression ">=" Expression
            | Expression "+" Expression       // precedence: additive
            | Expression "-" Expression
            | Expression "*" Expression       // precedence: multiplicative (highest)
            | Expression "/" Expression
            | Expression "%" Expression
            | "-" Expression                  // unary minus
            | "(" Expression ")"              // parentheses
            | Variable
            | NumericLiteral
            | BooleanLiteral
            | FunctionCallExpression

FunctionCallExpression ::= IDENTIFIER "(" ArgumentList ")"

Variable ::= IDENTIFIER

NumericLiteral ::= INTEGER

BooleanLiteral ::= "true" | "false"
```

### Operator Precedence (Low to High)
```
1. Equality:        ==, !=         (left associative)
2. Comparison:      <, >, <=, >=   (left associative)  
3. Additive:        +, -            (left associative)
4. Multiplicative:  *, /, %         (left associative)
```

### Terminal Symbols
```
IDENTIFIER, INTEGER
BEGIN, END, WHILE, DO, DONE, IF, THEN, ELSE, FI, FUNCTION, RETURN
INT, BOOL, VOID, TRUE, FALSE
ASSIGN (=), PLUS (+), MINUS (-), MULTIPLY (*), DIVIDE (/), MODULO (%)
LESS_THAN (<), GREATER_THAN (>), LESS_EQUAL (<=), GREATER_EQUAL (>=)
EQUALS (==), NOT_EQUALS (!=)
LPAREN ((), RPAREN ()), SEMICOLON (;), COMMA (,)
```

---

## Operational Semantics

### Environments
- **Value Environment (p)**: stack-scoped mapping from variable names to runtime values
- **Function Environment (w)**: stack-scoped mapping from function names to closures
- **Closures**: store parameter names, function body, and an isolated base value environment

### Literals  
```
----------------------------------------
p,w |- n --> (IntValue(n), p)

----------------------------------------
p,w |- true --> (BoolValue(true), p)

----------------------------------------
p,w |- false --> (BoolValue(false), p)
```

### Binary Operations
```
p,w |- e1 --> (v1, p)    p,w |- e2 --> (v2, p)    v3 = eval_op(op, v1, v2)
------------------------------------------------------------------------
p,w |- e1 op e2 --> (v3, p)
```

Where `eval_op` is defined as:
- **Arithmetic**: `+, -, *, /, %` 
- **Comparison**: `<, >, <=, >=, ==, !=` 

**Note**: All expressions are pure and return the original environment unchanged.

### Variable Declaration
```
initialValue(int) = IntValue(0)    p' = p[x |-> IntValue(0)]
---------------------------------------------------------------
p,w |- int x; --> p',w

initialValue(bool) = BoolValue(false)    p' = p[x |-> BoolValue(false)]
------------------------------------------------------------------------
p,w |- bool x; --> p',w
```

The runtime evaluator initializes and supports variable declarations for `int` and `bool`.

### Assignment
```
p,w |- e --> (v, p)    p' = p[x |-> v]
--------------------------------------
p,w |- x = e; --> p',w
```

### If Statement
```
p,w |- e --> (BoolValue(true), p)    p,w |- block_content1 --> p'
----------------------------------------------------------------
p,w |- if (e) then block_content1 else block_content2 fi --> p'

p,w |- e --> (BoolValue(false), p)    p,w |- block_content2 --> p'
------------------------------------------------------------------
p,w |- if (e) then block_content1 else block_content2 fi --> p'
```

### While Loop
```
p,w |- e --> (BoolValue(false), p)
------------------------------------------
p,w |- while (e) do block_content done --> p

p,w |- e --> (BoolValue(true), p)    p,w |- block_content --> p'    p',w |- while (e) do block_content done --> p''
-------------------------------------------------------------------------------------------------------------------
p,w |- while (e) do block_content done --> p''
```

### Function Declaration
```
c = Closure(paramNames, body, p_empty)
w' = w[f |-> c]
---------------------------------------------------------------
p,w |- function ret_t f(params) begin body end --> p,w'
```

`ret_t` and parameter type annotations are part of syntax and used by static type checking.
At runtime, the interpreter stores only parameter names and body in the closure.

### Function Call
```
w(f) = Closure(params, body, p_closure)
p,w |- args --> (values, p)
p_func = p_closure.enterScope()
p_func' = p_func[params |-> values]  
p_func',w |- body --> (v, p_final, _)
--------------------------------------
p,w |- f(args) --> (v, p)
```

### Scoping Rules
- **Variable Scoping**: Stack-based with `enterScope()` and `exitScope()`
  - Each scoped block creates a new variable scope
  - Variables in inner scopes shadow outer scopes
  - Scope automatically cleaned up on block exit
  
- **Function Scoping**: stack-scoped function environment
  - Function declarations are visible in the current and nested scopes
  - Function bodies execute in isolated value environments (no capture of outer runtime variables)
  - Functions can access parameters and function-local declarations only

---

## Example Runs

### Example 1: Simple Assignment
```simple
begin
  int x;
  x = 42;
end
```

**Output:**
```
Final environment:
  x -> 42
```

### Example 2: While Loop
```simple
begin
  int i;
  
  i = 0;
  while (i < 5) do
    i = i + 1;
  done
end
```

**Output:**
```
Final environment:
  i -> 5
```

### Example 3: Typed Function Declaration and Call
```simple
begin
  int y;
  
  function int square(int x) begin
    int result;
    result = x * x;
    return result;
  end
  
  y = square(5);
end
```

**Output:**
```
Final environment:
  y -> 25
```

### Example 4: Function Purity Error
```simple
begin
  int global_var;
  int result1;

  function int pure_func(int x) begin
    int local_var;
    local_var = global_var + x;
    return local_var;
  end

  global_var = 5;
  result1 = pure_func(10);
end
```

**Output:**
```
Error: Variable not found: global_var
```

### Example 5: Void Function
```simple
begin
  function void voidFunc() begin
    int x;
    x = 42;
    return;
  end
  
  voidFunc();
end
```

**Output:**
```
Final environment: (empty)
```

---