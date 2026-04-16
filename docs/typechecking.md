# SIMPLE Language: Typing Rules and Type Checking Examples

Grammar and abstract syntax are defined in [docs/language-specification.md](language-specification.md).

---

## Typing Rules

### Type Environments
- **Type Environment (p)**: Maps variable names to types with stack-based scoping  
- **Function Type Environment (w)**: Maps function names to function types
- **Type Results**: Set of possible types for control flow analysis with type compatibility checking

### Literals  
```
n is integer
----------------------------------------
p,w |- n -> {int},p,w 

----------------------------------------
p,w |- true -> {bool},p,w

----------------------------------------
p,w |- false -> {bool},p,w
```

### Binary Operations
```
p,w |- e1 -> int,p,w    p,w |- e2 -> int,p,w
---------------------------------------------
p,w |- e1 op e2 -> {int},p,w
```
where `op ∈ {+, -, *, /, %}` (arithmetic operations)

```
p,w |- e1 -> int,p,w    p,w |- e2 -> int,p,w
---------------------------------------------
p,w |- e1 op e2 -> {bool},p,w
```
where `op ∈ {<, >, <=, >=}` (comparison operations)
```
p,w |- e1 -> T,p,w    p,w |- e2 -> T,p,w
------------------------------------------
p,w |- e1 op e2 -> {bool},p,w
```
where `op ∈ {==, !=}` (equality operations)

### Variable lookup
```
p(x) = t
------------------
p,w |- x -> t,p,w
```

### Variable Declaration
```
x ∉ p    p' = p[x |-> T]
------------------------------
p,w |- T x -> {void}, p',w

```

The implementation accepts `int`, `bool`, and `void` in type positions.

### Assignment
```
x ∈ p
p,w |- e -> t,p,w    p(x) = t
------------------------------
p,w |- x = e; -> {void},p,w
```

### If Statement
```
p,w |- e -> bool,p,w
p,w |- block_content1 -> T1,p,w
------------------------------------------------------------
p,w |- if (e) then block_content1 fi -> T1 U {void},p,w


p,w |- e -> bool,p,w
p,w |- block_content1 -> T1,p,w
p,w |- block_content2 -> T2,p,w
----------------------------------------------------------------------------
p,w |- if (e) then block_content1 else block_content2 fi -> T1 U T2,p,w
```

### Return Statement
```
p,w |- e -> t,p,w
------------------------
p,w |- return e -> t,p,w

----------------------------
p,w |- return -> {void},p,w
```

### While Loop
```
p,w |- e -> bool,p,w    p,w |- block_content -> t,p,w
---------------------------------------------------------
p,w |- while (e) do block_content done -> t U {void},p,w
```

### Function Declaration
```
pars=[(param_1,param_type_1), (param_2,param_type_2),...(param_n,param_type_n)]
param1 != param2 != .... != param_n
p_func = fresh_scope()   
for i from 1 to n, p_func' = p_func[param_i |-> param_type_i]
p_func',w |- body -> T,p_func',w    T == ret_t
f ∉ w    
w' = w[f |-> FunctionType(ret_t, {param_type_1,param_type_2,...param_type_n})]
-------------------------------------------------------------------------------
p,w |- function ret_t f(pars) begin body end -> {void},p,w'
```

Function body checking is performed in a fresh variable environment initialized only with function parameters.

### Function Call
```
w(f) = FunctionType(ret_t, [t1, ..., tn])
p,w |- args -> [t1, ..., tn],p,w
------------------------------------------------------------------------------
p,w |- f(args) -> ret_t,p,w
```

### Statement Sequences
```
p,w |- s1 -> T1,p,w    p,w |- s2 -> T2,p,w
void ∈ T1
for all t ∈ T1, t != void => t ∈ T2 || T2 = {void}
--------------------------------------------------------------
p,w |- s1; s2; -> {T1\{void}} ∪ T2,p,w
```

Here typing rules are specified for 2 consecutive statements, Sequence comprising of more than 2 statements will have a typing rule which is an aggregation of this rule. For eg- first 2 statements' types will be accumulated using above rule then this accumulated type will be accumulated with the 3rd statement's type and this aggregation keeps going on.  

In addition, the implementation reports unreachable statements when the accumulated type set no longer contains `void`.

### Type Checking Rules
- **Variable Scoping**: 
  - Stack-based type environments
  - Each scoped block creates a new type scope
  - No duplicate variables
  
- **Function Scoping**: 
  - Functions have isolated type environments (pure functions)
  - Functions can only access their parameters and local variables
  - No duplicate function names
  
- **Control Flow Analysis**: Set-based type results for path analysis
  - Unreachable code detection after definite returns
  - Missing return path validation for non-void functions
  - Type compatibility checking across statement sequences

---

## Type Checking Examples

### Example 1: Basic Type Checking - PASS
```simple
begin
  int x;
  x = 42;
end
```

**Type Checking Output:**
```
Final type environment:
  x : int
```

### Example 2: Function with Return Type - PASS
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

**Type Checking Output:**
```
Final type environment:
  y : int
```

### Example 3: Void Function - PASS
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

**Type Checking Output:**
```
Final type environment: (empty)
```

### Example 4: Type Error Detection - FAIL
```simple
begin
  int x;
  x = true;
end
```

**Type Checking Output:**
```
Type Error: Type mismatch in assignment: cannot assign bool to variable of type int
```

### Example 5: Missing Return Path - FAIL
```simple
begin
  function int missing_return(bool flag) begin
    if (flag) then
      return 42;
    fi
  end
end
```

**Type Checking Output:**
```
Type Error: Function 'missing_return' declared to return int but some paths don't return a value
```

### Example 6: Unreachable Code Detection - FAIL
```simple
begin
  function int unreachable_test() begin
    int x;
    return 42;
    x = 10;
  end
end
```

**Type Checking Output:**
```
Type Error: Unreachable code: Statement 3 cannot be reached because previous statement always returns
```

### Example 7: Complex Control Flow - PASS
```simple
begin
  function int nested_return(int x, bool flag1, bool flag2) begin
    if (flag1) then
      if (flag2) then
        return x * 2;
      else
        return x + 10;
      fi
    else
      while (x > 0) do
        if (x % 2 == 0) then
            return x / 2;
        fi
        x = x - 1;
      done
      return 0;
    fi
  end
end
```

**Type Checking Output:**
```
Final type environment: (empty)

```

### Example 8: Duplicate Parameter Names - FAIL
```simple
begin
  function int test(int x, bool x) begin
    return 1;
  end
end
```

**Type Checking Output:**
```
Type Error: Duplicate parameter name: x
```

---