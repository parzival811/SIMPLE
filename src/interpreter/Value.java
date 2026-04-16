// Runtime value representation for the SIMPL interpreter
// Represents values that can be stored in variables and passed as function arguments

public abstract class Value {
    
    // Factory method to create integer values
    public static Value intValue(int value) {
        return new IntValue(value);
    }
    
    // Factory method to create boolean values
    public static Value boolValue(boolean value) {
        return new BoolValue(value);
    }
    
    // Factory method to create closure values
    public static Value closureValue(Closure closure) {
        return new ClosureValue(closure);
    }
    
    // Abstract method to get string representation
    public abstract String toString();
    
    // Direct value extraction methods (no type checking)
    public int asInt() { 
        throw new RuntimeException("Cannot convert to integer: " + this); 
    }
    
    public boolean asBool() { 
        throw new RuntimeException("Cannot convert to boolean: " + this); 
    }
    
    public Closure asClosure() { 
        throw new RuntimeException("Cannot convert to closure: " + this); 
    }
}

// Integer value implementation
class IntValue extends Value {
    private final int value;
    
    public IntValue(int value) {
        this.value = value;
    }
    
    @Override
    public int asInt() { return value; }
    
    @Override
    public String toString() {
        return Integer.toString(value);
    }
}

// Boolean value implementation
class BoolValue extends Value {
    private final boolean value;
    
    public BoolValue(boolean value) {
        this.value = value;
    }
    
    @Override
    public boolean asBool() { return value; }
    
    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}

// Closure value implementation
class ClosureValue extends Value {
    private final Closure closure;
    
    public ClosureValue(Closure closure) {
        this.closure = closure;
    }
    
    @Override
    public Closure asClosure() { return closure; }
    
    @Override
    public String toString() {
        return "closure";
    }
}
