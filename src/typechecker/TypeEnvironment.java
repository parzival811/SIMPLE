import java.util.*;

// manages variable/function type bindings with lexical scoping
public class TypeEnvironment {
    
    // stack entries
    private static abstract class Entry {
    }
    
    // variable/function binding entry
    private static class TypeBinding extends Entry {
        final String name;
        final Type type;
        
        TypeBinding(String name, Type type) {
            this.name = name;
            this.type = type;
        }
    }
    
    // scope boundary marker
    private static class Marker extends Entry {
    }
    
    private final List<Entry> stack;
    
    public TypeEnvironment() {
        this.stack = new ArrayList<>();
    }
    
    // enter a new lexical scope
    public void enterScope() {
        stack.add(new Marker());
    }
    
    // exit current scope, removing all bindings until marker
    public void exitScope() {
        while (!stack.isEmpty()) {
            Entry entry = stack.remove(stack.size() - 1);
            if (entry instanceof Marker) {
                break;
            }
        }
    }
    
    public void addBinding(String name, Type type) {
        stack.add(new TypeBinding(name, type));
    }
    
    // basic variable type lookup
    public Type lookup(String name) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Entry entry = stack.get(i);
            if (entry instanceof TypeBinding) {
                TypeBinding binding = (TypeBinding) entry;
                if (binding.name.equals(name)) {
                    return binding.type;
                }
            }
        }
        throw new RuntimeException("Variable not found in type environment: " + name);
    }
    
    // check if a binding already exists in the current lexical scope
    public boolean containsInCurrentScope(String name) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Entry entry = stack.get(i);
            if (entry instanceof Marker) {
                break;
            }
            if (entry instanceof TypeBinding) {
                TypeBinding binding = (TypeBinding) entry;
                if (binding.name.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    // get all current bindings (for debugging/display)
    public Map<String, Type> getCurrentBindings() {
        Map<String, Type> bindings = new LinkedHashMap<>();
        
        for (Entry entry : stack) {
            if (entry instanceof TypeBinding) {
                TypeBinding binding = (TypeBinding) entry;
                bindings.put(binding.name, binding.type);
            }
        }
        
        return bindings;
    }
}
