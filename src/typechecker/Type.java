// base class for all types in the language
public abstract class Type {
    // methods for type creation
    public static Type intType() {
        return IntType.getInstance();
    }
    
    public static Type boolType() {
        return BoolType.getInstance();
    }
    
    public static Type functionType(Type returnType, java.util.List<Type> parameterTypes) {
        return new FunctionType(returnType, parameterTypes);
    }
    
    public static Type voidType() {
        return VoidType.getInstance();
    }
    
    // parse type from string representation
    public static Type fromString(String typeString) {
        switch (typeString) {
            case "int":
                return Type.intType();
            case "bool":
                return Type.boolType();
            case "void":
                return Type.voidType();
            default:
                throw new RuntimeException("Unknown type: " + typeString);
        }
    }
    
    public abstract String toString();
    
    public abstract boolean equals(Object other);
    
    public FunctionType asFunctionType() { 
        throw new RuntimeException("Internal error: Invalid function type cast"); 
    }
    
    public boolean isIntType() { return false; }
    public boolean isBoolType() { return false; }
}

// integer type (singleton)
class IntType extends Type {
    private static IntType instance;
    
    private IntType() {}
    
    public static IntType getInstance() {
        if (instance == null) {
            instance = new IntType();
        }
        return instance;
    }
    
    @Override
    public boolean isIntType() { return true; }
    
    @Override
    public String toString() {
        return "int";
    }
    
    @Override
    public boolean equals(Object other) {
        return this == other;
    }
}

// boolean type (singleton)
class BoolType extends Type {
    private static BoolType instance;
    
    private BoolType() {}
    
    public static BoolType getInstance() {
        if (instance == null) {
            instance = new BoolType();
        }
        return instance;
    }
    
    @Override
    public boolean isBoolType() { return true; }
    
    @Override
    public String toString() {
        return "bool";
    }
    
    @Override
    public boolean equals(Object other) {
        return this == other;
    }
}

// Void type (singleton)
class VoidType extends Type {
    private static VoidType instance;
    
    private VoidType() {}
    
    public static VoidType getInstance() {
        if (instance == null) {
            instance = new VoidType();
        }
        return instance;
    }
    
    @Override
    public String toString() {
        return "void";
    }
    
    @Override
    public boolean equals(Object other) {
        return this == other;
    }
}

// function type (not singleton, used internally for function type signatures)
class FunctionType extends Type {
    private final Type returnType;
    private final java.util.List<Type> parameterTypes;
    
    public FunctionType(Type returnType, java.util.List<Type> parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = new java.util.ArrayList<>(parameterTypes);
    }
    
    @Override
    public FunctionType asFunctionType() { return this; }
    
    public Type getReturnType() { return returnType; }
    public java.util.List<Type> getParameterTypes() { return new java.util.ArrayList<>(parameterTypes); }
    public int getParameterCount() { return parameterTypes.size(); }
    
    // for debugging
    @Override
    public String toString() {
        throw new RuntimeException("Internal error: Function type toString() should never be called");
    }
    // for debugging
    @Override
    public boolean equals(Object other) {
        throw new RuntimeException("Internal error: Function type equals() should never be called");
    }
}