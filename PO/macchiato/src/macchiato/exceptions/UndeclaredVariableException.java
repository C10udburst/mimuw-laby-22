package macchiato.exceptions;

import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

public class UndeclaredVariableException extends MacchiatoException {
    public final char name;

    public UndeclaredVariableException(char name, @NotNull Instruction context) {
        super("Undeclared variable: " + name, context);
        this.name = name;
    }
}
