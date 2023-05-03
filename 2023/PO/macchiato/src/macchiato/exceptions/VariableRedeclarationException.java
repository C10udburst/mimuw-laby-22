package macchiato.exceptions;

public class VariableRedeclarationException extends MacchiatoException {
    public final char name;

    public VariableRedeclarationException(char name) {
        super("Variable " + name + " already declared in current scope.");
        this.name = name;
    }
}
