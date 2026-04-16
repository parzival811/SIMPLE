import java.util.*;

public class Closure {
    private final List<String> parameters;
    private final Sequence body;
    private final Environment valueEnv;
    
    public Closure(List<String> parameters, Sequence body, Environment valueEnv) {
        this.parameters = parameters;
        this.body = body;
        this.valueEnv = valueEnv;
    }
    
    // Create closure from function declaration
    public static Closure fromFunctionDeclaration(FunctionDeclaration func, Environment valueEnv) {
        List<String> paramNames = new ArrayList<>();
        for (VariableDeclaration param : func.getParameters()) {
            paramNames.add(param.getName());
        }
        return new Closure(paramNames, func.getBody(), valueEnv);
    }
    
    public List<String> getParameters() {
        return new ArrayList<>(parameters);
    }
    
    public Sequence getBody() {
        return body;
    }
    
    public Environment getValueEnv() {
        return valueEnv;
    }
    
    public int getParameterCount() {
        return parameters.size();
    }
}
