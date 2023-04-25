package macchiato.exceptions;

public class InvalidVariableNameException extends MacchiatoException {
    public final char name;

    public InvalidVariableNameException(char name) {
        super("Invalid variable name: " + name);
        this.name = name;
    }
}
