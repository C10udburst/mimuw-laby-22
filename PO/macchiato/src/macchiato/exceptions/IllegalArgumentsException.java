package macchiato.exceptions;

import macchiato.instructions.Instruction;

public class IllegalArgumentsException extends MacchiatoException {
    public IllegalArgumentsException(Instruction instruction) {
        super("Illegal amount of arguments", instruction);
    }
}
