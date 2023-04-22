package macchiato.exceptions;

public class UndeclaredVariableException extends MacchiatoException {
    private final char name;

    public UndeclaredVariableException(char name) {
        super("Undeclared variable: " + name);
        this.name = name;
    }

    public char getName() {
        return name;
    }
}
