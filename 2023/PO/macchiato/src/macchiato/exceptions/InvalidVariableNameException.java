package macchiato.exceptions;

import macchiato.instructions.Instruction;

public class InvalidVariableNameException extends MacchiatoException {
    private final char name;

    public InvalidVariableNameException(char name) {
        super("Invalid variable name: " + name);
        this.name = name;
    }

    public char getName() {
        return name;
    }
}
