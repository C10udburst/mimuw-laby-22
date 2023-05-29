package macchiato.exceptions;

import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

public class InvalidVariableNameException extends MacchiatoException {
    public final char name;

    public InvalidVariableNameException(char name, @NotNull Instruction context) {
        super("Invalid variable name: " + name, context);
        this.name = name;
    }
}
