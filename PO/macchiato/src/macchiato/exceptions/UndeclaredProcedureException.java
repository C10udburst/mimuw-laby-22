package macchiato.exceptions;

import macchiato.instructions.Instruction;

public class UndeclaredProcedureException extends MacchiatoException {
    public final String name;

    public UndeclaredProcedureException(Instruction context, String name) {
        super("Undeclared procedure: " + name, context);
        this.name = name;
    }
}
