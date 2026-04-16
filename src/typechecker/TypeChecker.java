import java.util.*;

public class TypeChecker {
    
    // holds the result of type checking: possible types and environment
    public static class TypeResult {
        private final Set<Type> types;
        private final TypeEnvironment environment;
        
        public TypeResult(Set<Type> types, TypeEnvironment environment) {
            this.types = new HashSet<>(types);
            this.environment = environment;
        }
        
        // single type result
        public TypeResult(Type type, TypeEnvironment environment) {
            this.types = new HashSet<>();
            this.types.add(type);
            this.environment = environment;
        }
        
        // void-only result (for statements that don't return)
        public TypeResult(TypeEnvironment environment) {
            this.types = new HashSet<>();
            this.types.add(Type.voidType());
            this.environment = environment;
        }
        
        public Set<Type> getTypes() { return new HashSet<>(types); }
        public Type getType() { 
            if (types.size() == 1) {
                return types.iterator().next();
            }
            throw new RuntimeException("TypeResult contains multiple types, use getTypes() instead");
        }
        public TypeEnvironment getEnvironment() { return environment; }
    }
    
    // main entry point for type checking
    public static TypeResult checkProgram(Sequence program) {
        TypeEnvironment typeEnv = new TypeEnvironment();  // variables
        TypeEnvironment functionTypeEnv = new TypeEnvironment();  // functions
        
        typeEnv.enterScope();
        functionTypeEnv.enterScope();
        
        List<Statement> mainStatements = program.getStatements();
        
        TypeResult result = checkStatements(mainStatements, typeEnv, functionTypeEnv);
        return new TypeResult(result.getTypes(), result.getEnvironment());
    }
    
    private static TypeResult checkStatements(List<Statement> statements, TypeEnvironment typeEnv, TypeEnvironment funTypeEnv) {
        if (statements.isEmpty()) {
            return new TypeResult(typeEnv);
        }
        
        TypeEnvironment currentEnv = typeEnv;
        
        // start with first statement
        TypeResult firstResult = checkStatement(statements.get(0), currentEnv, funTypeEnv);
        Set<Type> accumulatedTypes = new HashSet<>(firstResult.getTypes());
        
        // check remaining statements
        for (int i = 1; i < statements.size(); i++) {
            // unreachable if no void path exists
            if (!accumulatedTypes.contains(Type.voidType())) {
                throw new RuntimeException("Unreachable code: Statement " + (i + 1) + " cannot be reached because previous statement always returns");
            }
            
            Statement stmt = statements.get(i);
            TypeResult result = checkStatement(stmt, currentEnv, funTypeEnv);
            
            Set<Type> stmtTypes = result.getTypes();
            
            // ensure type compatibility
            Set<Type> nonVoidAccumulated = new HashSet<>(accumulatedTypes);
            nonVoidAccumulated.remove(Type.voidType());
            
            if (!nonVoidAccumulated.isEmpty()) {
                // check type consistency across statements
                Set<Type> nonVoidStmt = new HashSet<>(stmtTypes);
                nonVoidStmt.remove(Type.voidType());

                if (!nonVoidStmt.isEmpty() && !nonVoidStmt.equals(nonVoidAccumulated)) {
                    throw new RuntimeException("Type mismatch in statement sequence: " + "accumulated types " + nonVoidAccumulated + " incompatible with statement types " + nonVoidStmt);
                }
            }
            
            // merge types
            accumulatedTypes = new HashSet<>(nonVoidAccumulated);
            accumulatedTypes.addAll(stmtTypes);
        }
        
        return new TypeResult(accumulatedTypes, currentEnv);
    }
    

    private static TypeResult checkStatement(Statement stmt, TypeEnvironment typeEnv, TypeEnvironment funTypeEnv) {
        if (stmt instanceof VariableDeclaration) {
            VariableDeclaration decl = (VariableDeclaration) stmt;
            
            // no duplicates
            if (typeEnv.containsInCurrentScope(decl.getName())) {
                throw new RuntimeException("Duplicate variable name: " + decl.getName());
            }
            
            Type declType = Type.fromString(decl.getType());
            typeEnv.addBinding(decl.getName(), declType);
            return new TypeResult(typeEnv);
            
        }
        else if (stmt instanceof AssignmentStatement) {
            AssignmentStatement assign = (AssignmentStatement) stmt;
            

            Type varType = typeEnv.lookup(assign.getVariable().getName());
            

            TypeResult exprResult = checkExpression(assign.getExpression(), typeEnv, funTypeEnv);
            Type exprType = exprResult.getType();
            
            // types must match
            if (!varType.equals(exprType)) {
                throw new RuntimeException("Type mismatch in assignment: cannot assign " + exprType + " to variable of type " + varType);
            }
            
            return new TypeResult(typeEnv);
            
        }
        else if (stmt instanceof ReturnStatement) {
            ReturnStatement ret = (ReturnStatement) stmt;
            if (ret.hasValue()) {
                // return with value
                TypeResult result = checkExpression(ret.getExpression(), typeEnv, funTypeEnv);
                return new TypeResult(result.getType(), typeEnv);
            } 
            else {
                // bare return (void)
                return new TypeResult(Type.voidType(), typeEnv);
            }
            
        } 
        else if (stmt instanceof IfStatement) {
            IfStatement ifStmt = (IfStatement) stmt;
            
            // condition must be boolean
            TypeResult condResult = checkExpression(ifStmt.getCondition(), typeEnv, funTypeEnv);
            if (!condResult.getType().isBoolType()) {
                throw new RuntimeException("If condition must be boolean, got: " + condResult.getType());
            }
            

            TypeResult thenResult = checkScopedStatement(ifStmt.getThenStatement(), typeEnv, funTypeEnv);
            Set<Type> resultTypes = new HashSet<>(thenResult.getTypes());
            
            // handle else branch
            if (ifStmt.getElseStatement() != null) {
                TypeResult elseResult = checkScopedStatement(ifStmt.getElseStatement(), typeEnv, funTypeEnv);
                resultTypes.addAll(elseResult.getTypes());
            }
            else {
                resultTypes.add(Type.voidType());
            }
            
            return new TypeResult(resultTypes, typeEnv);
            
        } 
        else if (stmt instanceof WhileStatement) {
            WhileStatement whileStmt = (WhileStatement) stmt;

            // condition must be boolean
            TypeResult condResult = checkExpression(whileStmt.getCondition(), typeEnv, funTypeEnv);
            if (!condResult.getType().isBoolType()) {
                throw new RuntimeException("While condition must be boolean, got: " + condResult.getType());
            }
            
            TypeResult bodyResult = checkScopedStatement(whileStmt.getBody(), typeEnv, funTypeEnv);
            Set<Type> resultTypes = new HashSet<>(bodyResult.getTypes());
            resultTypes.add(Type.voidType()); // loop might not run
            
            return new TypeResult(resultTypes, typeEnv);
            
        } 
        else if (stmt instanceof FunctionDeclaration) {
            FunctionDeclaration funcDecl = (FunctionDeclaration) stmt;

            // no duplicate parameters
            Set<String> paramNames = new HashSet<>();
            for (VariableDeclaration param : funcDecl.getParameters()) {
                if (paramNames.contains(param.getName())) {
                    throw new RuntimeException("Duplicate parameter name: " + param.getName());
                }
                paramNames.add(param.getName());
            }

            // make function type object
            Type returnType = Type.fromString(funcDecl.getReturnType());
            List<Type> paramTypes = new ArrayList<>();
            for (VariableDeclaration param : funcDecl.getParameters()) {
                paramTypes.add(Type.fromString(param.getType()));
            }
            if (funTypeEnv.containsInCurrentScope(funcDecl.getName())) {
                throw new RuntimeException("Duplicate function name: " + funcDecl.getName());
            }
            Type funcType = Type.functionType(returnType, paramTypes);
            funTypeEnv.addBinding(funcDecl.getName(), funcType); 
            
            // check body with parameters in fresh environment
            TypeEnvironment typEnv = new TypeEnvironment();
            typEnv.enterScope();
            for (VariableDeclaration param : funcDecl.getParameters()) {
                Type paramType = Type.fromString(param.getType());
                typEnv.addBinding(param.getName(), paramType);
            }
            
            // check function body and validate return types
            TypeResult bodyResult = checkStatements(funcDecl.getBody().getStatements(), typEnv, funTypeEnv);
            validateFunctionReturnType(funcDecl.getName(), returnType, bodyResult.getTypes());
            
            return new TypeResult(typeEnv);
            
        } 
        else if (stmt instanceof FunctionCall) {
            FunctionCall call = (FunctionCall) stmt;
            checkFunctionCall(call.getFunctionName(), call.getArguments(), typeEnv, funTypeEnv);
            return new TypeResult(typeEnv);
            
        } 
        else if (stmt instanceof ScopedBlock) {
            ScopedBlock block = (ScopedBlock) stmt;
            return checkScopedStatement(block.getBody(), typeEnv, funTypeEnv);
            
        } 
        else if (stmt instanceof Sequence) {
            Sequence seq = (Sequence) stmt;
            return checkStatements(seq.getStatements(), typeEnv, funTypeEnv);
            
        } 
        else {
            throw new RuntimeException("Unknown statement type: " + stmt.getClass().getSimpleName());
        }
    }
    
    // check statement in a new scope
    private static TypeResult checkScopedStatement(Statement stmt, TypeEnvironment typeEnv, TypeEnvironment funTypeEnv) {
        typeEnv.enterScope();
        funTypeEnv.enterScope();
        TypeResult result = checkStatement(stmt, typeEnv, funTypeEnv);
        typeEnv.exitScope();
        funTypeEnv.exitScope();
        return new TypeResult(result.getTypes(), typeEnv);
    }
    
    private static TypeResult checkExpression(Expression expr, TypeEnvironment typeEnv, TypeEnvironment funTypeEnv) {
        if (expr instanceof Variable) {
            Variable var = (Variable) expr;
            Type type = typeEnv.lookup(var.getName());
            return new TypeResult(type, typeEnv);
            
        } 
        else if (expr instanceof NumericLiteral) {
            return new TypeResult(Type.intType(), typeEnv);
            
        } 
        else if (expr instanceof BooleanLiteral) {
            return new TypeResult(Type.boolType(), typeEnv);
            
        } 
        else if (expr instanceof BinaryOperation) {
            BinaryOperation binOp = (BinaryOperation) expr;
            return checkBinaryOperation(binOp, typeEnv, funTypeEnv);
            
        } 
        else if (expr instanceof FunctionCallExpression) {
            FunctionCallExpression call = (FunctionCallExpression) expr;
            return checkFunctionCall(call.getFunctionName(), call.getArguments(), typeEnv, funTypeEnv);
            
        } 
        else {
            throw new RuntimeException("Unknown expression type: " + expr.getClass().getSimpleName());
        }
    }
    
    private static TypeResult checkBinaryOperation(BinaryOperation binOp, TypeEnvironment typeEnv, TypeEnvironment funTypeEnv) {
        TypeResult leftResult = checkExpression(binOp.getLeft(), typeEnv, funTypeEnv);
        TypeResult rightResult = checkExpression(binOp.getRight(), typeEnv, funTypeEnv);
        
        Type leftType = leftResult.getType();
        Type rightType = rightResult.getType();
        
        switch (binOp.getOperator()) {
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                // arithmetic: int × int → int
                if (!leftType.isIntType() || !rightType.isIntType()) {
                    throw new RuntimeException("Arithmetic operation requires int operands, got: " + leftType + " " + binOp.getOperator() + " " + rightType);
                }
                return new TypeResult(Type.intType(), typeEnv);
                
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_EQUAL:
            case GREATER_EQUAL:
                // comparison: int × int → bool
                if (!leftType.isIntType() || !rightType.isIntType()) {
                    throw new RuntimeException("Comparison operation requires int operands, got: " + leftType + " " + binOp.getOperator() + " " + rightType);
                }
                return new TypeResult(Type.boolType(), typeEnv);
                
            case EQUALS:
            case NOT_EQUALS:
                // equality: same types → bool
                if (!leftType.equals(rightType)) {
                    throw new RuntimeException("Equality operation requires same types, got: " + leftType + " " + binOp.getOperator() + " " + rightType);
                }
                return new TypeResult(Type.boolType(), typeEnv);
                
            default:
                throw new RuntimeException("Unknown binary operator: " + binOp.getOperator());
        }
    }
    
    private static TypeResult checkFunctionCall(String functionName, List<Expression> arguments, TypeEnvironment typeEnv, TypeEnvironment funTypeEnv) {
        // find function
        Type funcType;
        try {
            funcType = funTypeEnv.lookup(functionName);
        }
        catch (RuntimeException e) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }
        
        FunctionType funcTypeObj = funcType.asFunctionType();
        
        // validate argument count
        if (arguments.size() != funcTypeObj.getParameterCount()) {
            throw new RuntimeException("Function " + functionName + " expects " + funcTypeObj.getParameterCount() + " arguments, got " + arguments.size());
        }
        
        // argument types matches parameter types
        List<Type> paramTypes = funcTypeObj.getParameterTypes();
        for (int i = 0; i < arguments.size(); i++) {
            TypeResult argResult = checkExpression(arguments.get(i), typeEnv, funTypeEnv);
            Type argType = argResult.getType();
            Type expectedType = paramTypes.get(i);
            
            if (!argType.equals(expectedType)) {
                throw new RuntimeException("Function " + functionName + " argument " + (i + 1) + " expects type " + expectedType + ", got " + argType);
            }
        }
        
        return new TypeResult(funcTypeObj.getReturnType(), typeEnv);
    }
    
    // ensure function returns match declared type on all paths
    private static void validateFunctionReturnType(String functionName, Type declaredReturnType, Set<Type> bodyTypes) {
        Set<Type> actualReturnTypes = new HashSet<>(bodyTypes);
        boolean hasVoidPath = actualReturnTypes.remove(Type.voidType());
        
        if (declaredReturnType.equals(Type.voidType())) {
            // void functions can't return values
            if (!actualReturnTypes.isEmpty()) {
                throw new RuntimeException("Void function '" + functionName + "' cannot return non-void values: " + actualReturnTypes);
            }
        }
        else {
            // non-void functions must return on all paths
            if (actualReturnTypes.isEmpty()) {
                throw new RuntimeException("Function '" + functionName + "' declared to return " + declaredReturnType + " but never returns a value");
            }
            if (actualReturnTypes.size() > 1 || 
                !actualReturnTypes.iterator().next().equals(declaredReturnType)) {
                throw new RuntimeException("Function '" + functionName + "' declared to return " + declaredReturnType + " but returns: " + actualReturnTypes);
            }
            if (hasVoidPath) {
                throw new RuntimeException("Function '" + functionName + "' declared to return " + declaredReturnType + " but some paths don't return a value");
            }
        }
    }
}
