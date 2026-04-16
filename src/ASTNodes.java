import java.util.List;
import java.util.ArrayList;

// SIMPL (Simple Imperative Programming Language) AST Implementation


// base class for all AST nodes in SIMPL
abstract class ASTNode {

    // abstract method for string representation of the AST node
    public abstract String toString(int indent);

    @Override
    public String toString() {
        return toString(0);
    }
    
    
    // helper method to create indentation strings
    protected String getIndent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("  "); // 2 spaces per level
        }
        return sb.toString();
    }
}

// abstract base class for all expressions in SIMPL
abstract class Expression extends ASTNode {
}

// abstract base class for all statements in SIMPL
abstract class Statement extends ASTNode {
}

// AST class for variable
class Variable extends Expression {
    private String name;
    
    public Variable(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString(int indent) {
        return getIndent(indent) + "Variable(" + name + ")";
    }
}

// AST class for numeric literal
class NumericLiteral extends Expression {
    private int value;
    
    public NumericLiteral(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    @Override
    public String toString(int indent) {
        return getIndent(indent) + "NumericLiteral(" + value + ")";
    }
}

// AST class for boolean literal
class BooleanLiteral extends Expression {
    private boolean value;
    
    public BooleanLiteral(boolean value) {
        this.value = value;
    }
    
    public boolean getValue() {
        return value;
    }
    
    @Override
    public String toString(int indent) {
        return getIndent(indent) + "BooleanLiteral(" + value + ")";
    }
}

// AST class for binary operation
class BinaryOperation extends Expression {
    public enum Operator {
        PLUS("+"),
        MINUS("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        MODULO("%"),
        LESS_THAN("<"),
        GREATER_THAN(">"),
        LESS_EQUAL("<="),
        GREATER_EQUAL(">="),
        EQUALS("=="),
        NOT_EQUALS("!=");
        
        private final String symbol;
        
        Operator(String symbol) {
            this.symbol = symbol;
        }
        
        @Override
        public String toString() {
            return symbol;
        }
    }
    
    private Expression left;
    private Operator operator;
    private Expression right;
    
    public BinaryOperation(Expression left, Operator operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    
    public Expression getLeft() { return left; }
    public Operator getOperator() { return operator; }
    public Expression getRight() { return right; }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("BinaryOperation(").append(operator).append(")\n");
        sb.append(left.toString(indent + 1)).append("\n");
        sb.append(right.toString(indent + 1));
        return sb.toString();
    }
}

// AST class for assignment statement
class AssignmentStatement extends Statement {
    private Variable variable;
    private Expression expression;
    
    public AssignmentStatement(Variable variable, Expression expression) {
        this.variable = variable;
        this.expression = expression;
    }
    
    public Variable getVariable() { return variable; }
    public Expression getExpression() { return expression; }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("AssignmentStatement\n");
        sb.append(variable.toString(indent + 1)).append("\n");
        sb.append(expression.toString(indent + 1));
        return sb.toString();
    }
}

// AST class for variable declaration statement
class VariableDeclaration extends Statement {
    private String type;
    private String name;
    
    public VariableDeclaration(String type, String name) {
        this.type = type;
        this.name = name;
    }
    
    public String getType() { return type; }
    public String getName() { return name; }
    
    @Override
    public String toString(int indent) {
        return getIndent(indent) + "VariableDeclaration(" + type + " " + name + ")";
    }
}

// AST class for return statement
class ReturnStatement extends Statement {
    private Expression expression; // null for bare return
    
    public ReturnStatement() {
        this.expression = null;
    }
    
    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }
    
    public Expression getExpression() { return expression; }
    public boolean hasValue() { return expression != null; }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("ReturnStatement");
        if (expression != null) {
            sb.append("\n").append(expression.toString(indent + 1));
        }
        return sb.toString();
    }
}

 // AST class for sequence of statements
class Sequence extends Statement {
    private List<Statement> statements;
    
    public Sequence() {
        this.statements = new ArrayList<>();
    }
    
    public Sequence(List<Statement> statements) {
        this.statements = new ArrayList<>(statements);
    }
    
    public void addStatement(Statement statement) {
        statements.add(statement);
    }
    
    public List<Statement> getStatements() {
        return new ArrayList<>(statements);
    }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("Sequence\n");
        for (int i = 0; i < statements.size(); i++) {
            sb.append(statements.get(i).toString(indent + 1));
            if (i < statements.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}

// AST class for scoped block (begin...end with local scope)
class ScopedBlock extends Statement {
    private Sequence body;
    
    public ScopedBlock(Sequence body) {
        this.body = body;
    }
    
    public Sequence getBody() { return body; }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("ScopedBlock\n");
        sb.append(body.toString(indent + 1));
        return sb.toString();
    }
}

// AST class for while loop
class WhileStatement extends Statement {
    private Expression condition;
    private Statement body;
    
    public WhileStatement(Expression condition, Statement body) {
        this.condition = condition;
        this.body = body;
    }
    
    public Expression getCondition() { return condition; }
    public Statement getBody() { return body; }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("WhileStatement\n");
        sb.append(getIndent(indent + 1)).append("Condition:\n");
        sb.append(condition.toString(indent + 2)).append("\n");
        sb.append(getIndent(indent + 1)).append("Body:\n");
        sb.append(body.toString(indent + 2));
        return sb.toString();
    }
}

// AST class for if statement
class IfStatement extends Statement {
    private Expression condition;
    private Statement thenStatement;
    private Statement elseStatement; // can be null for if without else
    
    public IfStatement(Expression condition, Statement thenStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = null;
    }
    
    public IfStatement(Expression condition, Statement thenStatement, Statement elseStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
    }
    
    public Expression getCondition() { return condition; }
    public Statement getThenStatement() { return thenStatement; }
    public Statement getElseStatement() { return elseStatement; }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("IfStatement\n");
        sb.append(getIndent(indent + 1)).append("Condition:\n");
        sb.append(condition.toString(indent + 2)).append("\n");
        sb.append(getIndent(indent + 1)).append("Then:\n");
        sb.append(thenStatement.toString(indent + 2));
        if (elseStatement != null) {
            sb.append("\n").append(getIndent(indent + 1)).append("Else:\n");
            sb.append(elseStatement.toString(indent + 2));
        }
        return sb.toString();
    }
}

 // AST class for function declaration
class FunctionDeclaration extends Statement {
    private String name;
    private String returnType;
    private List<VariableDeclaration> parameters;
    private Sequence body;
    
    public FunctionDeclaration(String name, String returnType, List<VariableDeclaration> parameters, Sequence body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
        this.body = body;
    }
    
    public FunctionDeclaration(String name, String returnType, Sequence body) {
        this(name, returnType, new ArrayList<>(), body);
    }
    
    public String getName() { return name; }
    public String getReturnType() { return returnType; }
    public List<VariableDeclaration> getParameters() { return new ArrayList<>(parameters); }
    public Sequence getBody() { return body; }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("FunctionDeclaration(").append(name).append(" : ").append(returnType).append(")\n");
        
        if (!parameters.isEmpty()) {
            sb.append(getIndent(indent + 1)).append("Parameters:\n");
            for (int i = 0; i < parameters.size(); i++) {
                sb.append(parameters.get(i).toString(indent + 2));
                if (i < parameters.size() - 1) {
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }
        
        sb.append(getIndent(indent + 1)).append("Body:\n");
        sb.append(body.toString(indent + 2));
        
        return sb.toString();
    }
}

 // AST class for function call as expression
class FunctionCallExpression extends Expression {
    private String functionName;
    private List<Expression> arguments;
    
    public FunctionCallExpression(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = new ArrayList<>(arguments);
    }
    
    public FunctionCallExpression(String functionName) {
        this(functionName, new ArrayList<>());
    }
    
    public String getFunctionName() { return functionName; }
    public List<Expression> getArguments() { return new ArrayList<>(arguments); }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("FunctionCallExpression(").append(functionName).append(")");
        
        if (!arguments.isEmpty()) {
            sb.append("\n").append(getIndent(indent + 1)).append("Arguments:\n");
            for (int i = 0; i < arguments.size(); i++) {
                sb.append(arguments.get(i).toString(indent + 2));
                if (i < arguments.size() - 1) {
                    sb.append("\n");
                }
            }
        }
        
        return sb.toString();
    }
}

 // AST class for function call as statement
class FunctionCall extends Statement {
    private String functionName;
    private List<Expression> arguments;
    
    public FunctionCall(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = new ArrayList<>(arguments);
    }
    
    public FunctionCall(String functionName) {
        this(functionName, new ArrayList<>());
    }
    
    public String getFunctionName() { return functionName; }
    public List<Expression> getArguments() { return new ArrayList<>(arguments); }
    
    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent)).append("FunctionCall(").append(functionName).append(")");
        
        if (!arguments.isEmpty()) {
            sb.append("\n").append(getIndent(indent + 1)).append("Arguments:\n");
            for (int i = 0; i < arguments.size(); i++) {
                sb.append(arguments.get(i).toString(indent + 2));
                if (i < arguments.size() - 1) {
                    sb.append("\n");
                }
            }
        }
        
        return sb.toString();
    }
}

