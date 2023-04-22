package macchiato.expressions;

import macchiato.exceptions.InvalidVariableNameException;
import macchiato.exceptions.UndeclaredVariableException;
import macchiato.instructions.Instruction;

public class Variable extends Expression {

    // region dane
    private final char name;

    public Variable(char name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + "";
    }

    @Override
    public int evaluate(Instruction context) throws UndeclaredVariableException, InvalidVariableNameException {
        return context.getVariable(name);
    }

    // endregion


}
