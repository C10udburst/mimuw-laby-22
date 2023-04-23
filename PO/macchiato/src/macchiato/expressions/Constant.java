package macchiato.expressions;

import macchiato.instructions.Instruction;

public class Constant extends Expression {
    // region dane
    
    int value;

    // endregion

    // region techniczne

    public Constant(int v) {
        value = v;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    // endregion

    // region operacje

    @Override
    public int evaluate(Instruction context) {
        return value;
    }

    // endregion
}
