import java.util.*;

public class Environment {
    
    private static abstract class Entry {
    }
    
    private static class Binding extends Entry {
        final String name;
        final Value value;
        
        Binding(String name, Value value) {
            this.name = name;
            this.value = value;
        }
    }
    
    private static class Marker extends Entry {
    }
    
    private final List<Entry> stack;
    
    public Environment() {
        this.stack = new ArrayList<>();
    }
    
    private Environment(List<Entry> stack) {
        this.stack = new ArrayList<>(stack);
    }
    
    public Environment copy() {
        return new Environment(stack);
    }
    
    public void enterScope() {
        stack.add(new Marker());
    }
    
    public void exitScope() {
        while (!stack.isEmpty()) {
            Entry entry = stack.remove(stack.size() - 1);
            if (entry instanceof Marker) {
                break;
            }
        }
    }
    
    public void addBinding(String name, Value value) {
        stack.add(new Binding(name, value));
    }
    
    public Value lookup(String name) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Entry entry = stack.get(i);
            if (entry instanceof Binding) {
                Binding binding = (Binding) entry;
                if (binding.name.equals(name)) {
                    return binding.value;
                }
            }
        }
        throw new RuntimeException("Variable not found: " + name);
    }
    
    public void updateBinding(String name, Value value) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Entry entry = stack.get(i);
            if (entry instanceof Binding) {
                Binding binding = (Binding) entry;
                if (binding.name.equals(name)) {
                    stack.set(i, new Binding(name, value));
                    return;
                }
            }
        }
        throw new RuntimeException("Variable not found for update: " + name);
    }
    
    public static Value getInitialValue(String type) {
        switch (type) {
            case "int":
                return Value.intValue(0);
            case "bool":
                return Value.boolValue(false);
            default:
                throw new RuntimeException("Unknown type: " + type);
        }
    }
    
    public Map<String, Value> getCurrentBindings() {
        Map<String, Value> bindings = new LinkedHashMap<>();
        
        for (Entry entry : stack) {
            if (entry instanceof Binding) {
                Binding binding = (Binding) entry;
                bindings.put(binding.name, binding.value);
            }
        }
        
        return bindings;
    }
}
