package macchiato.expressions;

import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.Instruction;

public class Multiply extends Operator {
    // region dane
    // endregion

    // region techniczne
    public Multiply(Expression arg1, Expression arg2) {
        super(arg1, arg2);
    }

    @Override
    public int priority() {
        return 500;
    }

    @Override
    public String symbol() {
        return "*";
    }
    // endregion

    // region operacje
    @Override
    public int evaluate(Instruction context) throws MacchiatoException {
        return arg1.evaluate(context) * arg2.evaluate(context);
    }
    // endregion
}
