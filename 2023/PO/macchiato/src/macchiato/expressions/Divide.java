package macchiato.expressions;

import macchiato.exceptions.DivideByZeroException;
import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.Instruction;

public class Divide extends Operator {
    // region dane

    // endregion

    // region techniczne

    public Divide(Expression arg1, Expression arg2) {
        super(arg1, arg2);
    }

    @Override
    public int priority() {
        return 500;
    }

    @Override
    public String symbol() {
        return "/";
    }


    @Override
    public int evaluate(Instruction context) throws MacchiatoException {
        int arg2Value = arg2.evaluate(context);
        if (arg2Value == 0)
            throw new DivideByZeroException();
        return arg1.evaluate(context) / arg2Value;
    }
}
