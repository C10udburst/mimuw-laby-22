package macchiato.exceptions;

import macchiato.instructions.Instruction;

public class MissingArgumentException extends MacchiatoException {
    public final char argumentName;

    public MissingArgumentException(char argumentName, Instruction instruction) {
        super("Missing argument: " + argumentName, instruction);
        this.argumentName = argumentName;
    }
}
