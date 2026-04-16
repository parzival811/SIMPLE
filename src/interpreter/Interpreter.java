import java.util.*;

public class Interpreter {
    
    public static class EvalResult {
        private final Value value;
        private final Environment environment;
        private final boolean isReturn;
        
        public EvalResult(Value value, Environment environment, boolean isReturn) {
            this.value = value;
            this.environment = environment;
            this.isReturn = isReturn;
        }
        
        public EvalResult(Value value, Environment environment) {
            this(value, environment, false);
        }
        
        public EvalResult(Environment environment) {
            this(null, environment, false);
        }
        
        public Value getValue() { return value; }
        public Environment getEnvironment() { return environment; }
        public boolean isReturn() { return isReturn; }
        public boolean hasValue() { return value != null; }
    }
    
    public static EvalResult evaluateProgram(Sequence program) {
        Environment globalEnv = new Environment();
        Environment functionEnv = new Environment();
        
        globalEnv.enterScope();
        functionEnv.enterScope();
        
        List<Statement> mainStatements = program.getStatements();
        
        EvalResult result = executeStatements(mainStatements, globalEnv, functionEnv);
        return new EvalResult(result.getValue(), result.getEnvironment());
    }
    
    private static EvalResult executeStatements(List<Statement> statements, Environment env, Environment funEnv) {
        Environment currentEnv = env;
        
        for (Statement stmt : statements) {
            EvalResult result = executeStatement(stmt, currentEnv, funEnv);
            currentEnv = result.getEnvironment();
            
            if (result.isReturn()) {
                return result;
            }
        }
        
        return new EvalResult(currentEnv);
    }
    
    private static EvalResult executeStatement(Statement stmt, Environment env, Environment funEnv) {
        if (stmt instanceof VariableDeclaration) {
            VariableDeclaration decl = (VariableDeclaration) stmt;
            Value initialValue = Environment.getInitialValue(decl.getType());
            env.addBinding(decl.getName(), initialValue);
            return new EvalResult(env);
            
        }
        else if (stmt instanceof AssignmentStatement) {
            AssignmentStatement assign = (AssignmentStatement) stmt;
            EvalResult exprResult = evaluateExpression(assign.getExpression(), env, funEnv);
            env.updateBinding(assign.getVariable().getName(), exprResult.getValue());
            return new EvalResult(env);
            
        }
        else if (stmt instanceof ReturnStatement) {
            ReturnStatement ret = (ReturnStatement) stmt;
            if (ret.hasValue()) {
                EvalResult result = evaluateExpression(ret.getExpression(), env, funEnv);
                return new EvalResult(result.getValue(), env, true);
            } 
            else {
                return new EvalResult(null, env, true);
            }
            
        } 
        else if (stmt instanceof IfStatement) {
            IfStatement ifStmt = (IfStatement) stmt;
            EvalResult condResult = evaluateExpression(ifStmt.getCondition(), env, funEnv);
            
            if (condResult.getValue().asBool()) {
                return executeScopedStatement(ifStmt.getThenStatement(), env, funEnv);
            } 
            else if (ifStmt.getElseStatement() != null) {
                return executeScopedStatement(ifStmt.getElseStatement(), env, funEnv);
            } 
            else {
                return new EvalResult(env);
            }
            
        } 
        else if (stmt instanceof WhileStatement) {
            WhileStatement whileStmt = (WhileStatement) stmt;
            return executeWhileLoop(whileStmt, env, funEnv);
            
        } 
        else if (stmt instanceof FunctionDeclaration) {
            FunctionDeclaration funcDecl = (FunctionDeclaration) stmt;
            Environment emptyEnv = new Environment();
            emptyEnv.enterScope();
            
            Closure closure = Closure.fromFunctionDeclaration(funcDecl, emptyEnv);
            funEnv.addBinding(funcDecl.getName(), Value.closureValue(closure));
            return new EvalResult(env);
            
        } 
        else if (stmt instanceof FunctionCall) {
            FunctionCall call = (FunctionCall) stmt;
            EvalResult result = executeFunctionCall(call.getFunctionName(), call.getArguments(), env, funEnv);
            return new EvalResult(result.getEnvironment());
            
        } 
        else if (stmt instanceof ScopedBlock) {
            ScopedBlock block = (ScopedBlock) stmt;
            return executeBlock(block.getBody(), env, funEnv);
            
        } 
        else if (stmt instanceof Sequence) {
            Sequence seq = (Sequence) stmt;
            return executeStatements(seq.getStatements(), env, funEnv);
            
        } 
        else {
            throw new RuntimeException("Unknown statement type: " + stmt.getClass().getSimpleName());
        }
    }
    
    private static EvalResult executeWhileLoop(WhileStatement whileStmt, Environment env, Environment funEnv) {
        Environment currentEnv = env;
        
        while (true) {
            EvalResult condResult = evaluateExpression(whileStmt.getCondition(), currentEnv, funEnv);
            
            if (!condResult.getValue().asBool()) {
                break;
            }
            
            EvalResult bodyResult = executeScopedStatement(whileStmt.getBody(), currentEnv, funEnv);
            currentEnv = bodyResult.getEnvironment();
            
            if (bodyResult.isReturn()) {
                return bodyResult;
            }
        }
        
        return new EvalResult(currentEnv);
    }
    
    private static EvalResult executeScopedStatement(Statement stmt, Environment env, Environment funEnv) {
        env.enterScope();
        funEnv.enterScope();
        EvalResult result = executeStatement(stmt, env, funEnv);
        Environment newEnv = result.getEnvironment();
        newEnv.exitScope();
        funEnv.exitScope();
        return new EvalResult(result.getValue(), newEnv, result.isReturn());
    }
    
    private static EvalResult executeBlock(Sequence body, Environment env, Environment funEnv) {
        env.enterScope();
        funEnv.enterScope();
        EvalResult result = executeStatements(body.getStatements(), env, funEnv);
        Environment newEnv = result.getEnvironment();
        newEnv.exitScope();
        funEnv.exitScope();
        return new EvalResult(result.getValue(), newEnv, result.isReturn());
    }
    
    private static EvalResult evaluateExpression(Expression expr, Environment env, Environment funEnv) {
        if (expr instanceof Variable) {
            Variable var = (Variable) expr;
            Value value = env.lookup(var.getName());
            return new EvalResult(value, env);
            
        } 
        else if (expr instanceof NumericLiteral) {
            NumericLiteral num = (NumericLiteral) expr;
            return new EvalResult(Value.intValue(num.getValue()), env);
            
        } 
        else if (expr instanceof BooleanLiteral) {
            BooleanLiteral bool = (BooleanLiteral) expr;
            return new EvalResult(Value.boolValue(bool.getValue()), env);
            
        } 
        else if (expr instanceof BinaryOperation) {
            BinaryOperation binOp = (BinaryOperation) expr;
            return evaluateBinaryOperation(binOp, env, funEnv);
            
        } 
        else if (expr instanceof FunctionCallExpression) {
            FunctionCallExpression call = (FunctionCallExpression) expr;
            return executeFunctionCall(call.getFunctionName(), call.getArguments(), env, funEnv);
            
        } 
        else {
            throw new RuntimeException("Unknown expression type: " + expr.getClass().getSimpleName());
        }
    }
    
    private static EvalResult evaluateBinaryOperation(BinaryOperation binOp, Environment env, Environment funEnv) {
        EvalResult leftResult = evaluateExpression(binOp.getLeft(), env, funEnv);
        EvalResult rightResult = evaluateExpression(binOp.getRight(), env, funEnv);
        
        Value leftVal = leftResult.getValue();
        Value rightVal = rightResult.getValue();
        
        switch (binOp.getOperator()) {
            case PLUS:
                return new EvalResult(Value.intValue(leftVal.asInt() + rightVal.asInt()), env);
            case MINUS:
                return new EvalResult(Value.intValue(leftVal.asInt() - rightVal.asInt()), env);
            case MULTIPLY:
                return new EvalResult(Value.intValue(leftVal.asInt() * rightVal.asInt()), env);
            case DIVIDE:
                return new EvalResult(Value.intValue(leftVal.asInt() / rightVal.asInt()), env);
            case MODULO:
                return new EvalResult(Value.intValue(leftVal.asInt() % rightVal.asInt()), env);
            case LESS_THAN:
                return new EvalResult(Value.boolValue(leftVal.asInt() < rightVal.asInt()), env);
            case GREATER_THAN:
                return new EvalResult(Value.boolValue(leftVal.asInt() > rightVal.asInt()), env);
            case LESS_EQUAL:
                return new EvalResult(Value.boolValue(leftVal.asInt() <= rightVal.asInt()), env);
            case GREATER_EQUAL:
                return new EvalResult(Value.boolValue(leftVal.asInt() >= rightVal.asInt()), env);
            case EQUALS:
                if (leftVal instanceof IntValue && rightVal instanceof IntValue) {
                    return new EvalResult(Value.boolValue(leftVal.asInt() == rightVal.asInt()), env);
                }
                if (leftVal instanceof BoolValue && rightVal instanceof BoolValue) {
                    return new EvalResult(Value.boolValue(leftVal.asBool() == rightVal.asBool()), env);
                }
                throw new RuntimeException("Equality requires operands of the same runtime type");
            case NOT_EQUALS:
                if (leftVal instanceof IntValue && rightVal instanceof IntValue) {
                    return new EvalResult(Value.boolValue(leftVal.asInt() != rightVal.asInt()), env);
                }
                if (leftVal instanceof BoolValue && rightVal instanceof BoolValue) {
                    return new EvalResult(Value.boolValue(leftVal.asBool() != rightVal.asBool()), env);
                }
                throw new RuntimeException("Inequality requires operands of the same runtime type");
            default:
                throw new RuntimeException("Unknown binary operator: " + binOp.getOperator());
        }
    }
    
    // Execute function call
    private static EvalResult executeFunctionCall(String functionName, List<Expression> arguments, 
                                                Environment env, Environment funEnv) {
        Value funcValue = funEnv.lookup(functionName);
        Closure closure = funcValue.asClosure();
        
        List<Value> argValues = new ArrayList<>();
        
        for (Expression arg : arguments) {
            EvalResult argResult = evaluateExpression(arg, env, funEnv);
            argValues.add(argResult.getValue());
        }
        
        if (argValues.size() != closure.getParameterCount()) {
            throw new RuntimeException("Function " + functionName + " expects " + closure.getParameterCount() + " arguments, got " + argValues.size());
        }
        
        Environment funcEnv = closure.getValueEnv().copy();
        funcEnv.enterScope();
        
        List<String> params = closure.getParameters();
        for (int i = 0; i < params.size(); i++) {
            funcEnv.addBinding(params.get(i), argValues.get(i));
        }
        
        EvalResult bodyResult = executeStatements(closure.getBody().getStatements(), funcEnv, funEnv);
        funcEnv.exitScope();
        
        return new EvalResult(bodyResult.getValue(), env);
    }
}
