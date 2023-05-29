package macchiato.expressions;

import macchiato.exceptions.DivideByZeroException;
import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

public class Divide extends Operator {
    // region dane
    // endregion

    // region techniczne
    public Divide(@NotNull Expression arg1, @NotNull Expression arg2) {
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
    // endregion

    // region operacje
    @Override
    public int evaluate(@NotNull Instruction context) throws MacchiatoException {
        int arg2Value = arg2.evaluate(context);
        if (arg2Value == 0)
            throw new DivideByZeroException(this, context);
        return arg1.evaluate(context) / arg2Value;
    }
    // endregion
}
