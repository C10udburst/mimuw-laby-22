package macchiato.expressions;

import macchiato.exceptions.InvalidVariableNameException;
import macchiato.exceptions.UndeclaredVariableException;
import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

public class Variable extends Expression {

    // region dane
    private final char name;
    // endregion

    // region techniczne
    public Variable(char name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + "";
    }
    // endregion

    // region operacje
    @Override
    public int evaluate(@NotNull Instruction context) throws UndeclaredVariableException, InvalidVariableNameException {
        return context.getVariable(name);
    }
    // endregion
}
