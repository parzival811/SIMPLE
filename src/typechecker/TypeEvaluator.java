import java_cup.runtime.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// main class for running the SIMPLE type checker
public class TypeEvaluator {
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java TypeEvaluator <test-case-file>");
            System.exit(1);
        }
        
        String filename = args[0];
        
        try {
            // read and parse the program
            Sequence program = parseProgram(filename);
            
            // run the type checker
            TypeChecker.TypeResult result = TypeChecker.checkProgram(program);
            
            // print the final type environment state
            printFinalTypeState(result.getEnvironment());
            
        } catch (Exception e) {
            System.err.println("Type Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    // parse a SIMPLE program from file
    private static Sequence parseProgram(String filename) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);

        StringReader reader = new StringReader(content);
        SimpleLexer lexer = new SimpleLexer(reader);
        SimpleParser parser = new SimpleParser(lexer);

        Symbol result = parser.parse();
        return (Sequence) result.value;
    }

    // print the final state of the type environment
    private static void printFinalTypeState(TypeEnvironment typeEnv) {
        Map<String, Type> bindings = typeEnv.getCurrentBindings();
        
        if (bindings.isEmpty()) {
            System.out.println("Final type environment: (empty)");
            return;
        }
        
        System.out.println("Final type environment:");
        for (Map.Entry<String, Type> entry : bindings.entrySet()) {
            System.out.println("  " + entry.getKey() + " : " + entry.getValue());
        }
    }
}
