package macchiato.exceptions;

public class UndeclaredVariableException extends MacchiatoException {
    public final char name;

    public UndeclaredVariableException(char name) {
        super("Undeclared variable: " + name);
        this.name = name;
    }
}
