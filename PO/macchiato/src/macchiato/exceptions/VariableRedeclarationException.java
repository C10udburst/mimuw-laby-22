package macchiato.exceptions;

import macchiato.instructions.Instruction;

public class VariableRedeclarationException extends MacchiatoException {
    public final char name;

    public VariableRedeclarationException(char name, Instruction context) {
        super("Variable " + name + " already declared in current scope.", context);
        this.name = name;
    }
}
