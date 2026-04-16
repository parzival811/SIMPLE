import java_cup.runtime.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// Main class for running the SIMPLE interpreter
// Usage: java Evaluator <test-case-file>
public class Evaluator {
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Evaluator <test-case-file>");
            System.exit(1);
        }
        
        String filename = args[0];
        
        try {
            // Read and parse the program
            Sequence program = parseProgram(filename);
            
            // Run the interpreter
            Interpreter.EvalResult result = Interpreter.evaluateProgram(program);
            
            // Print the final environment state
            printFinalState(result.getEnvironment());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    // Parse a SIMPLE program from file
    private static Sequence parseProgram(String filename) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);

        StringReader reader = new StringReader(content);
        SimpleLexer lexer = new SimpleLexer(reader);
        SimpleParser parser = new SimpleParser(lexer);

        Symbol result = parser.parse();
        return (Sequence) result.value;
    }

    // Print the final state of the environment
    private static void printFinalState(Environment env) {
        Map<String, Value> bindings = env.getCurrentBindings();
        
        if (bindings.isEmpty()) {
            System.out.println("Final environment: (empty)");
            return;
        }
        
        System.out.println("Final environment:");
        for (Map.Entry<String, Value> entry : bindings.entrySet()) {
            System.out.println("  " + entry.getKey() + " -> " + entry.getValue());
        }
    }
}
