package macchiato.expressions;

import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

public class Subtract extends Operator {
    // region dane

    // end region

    // region techniczne
    public Subtract(@NotNull Expression arg1, @NotNull Expression arg2) {
        super(arg1, arg2);
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public String symbol() {
        return "-";
    }
    // endregion


    // region operacje
    @Override
    public int evaluate(@NotNull Instruction context) throws MacchiatoException {
        return arg1.evaluate(context) - arg2.evaluate(context);
    }
    // endregion

    // region fabryka
    public static Expression of(@NotNull Expression arg1, @NotNull Expression arg2) {
        if (arg1 instanceof Constant a1 && arg2 instanceof Constant a2)
            return Constant.of(a1.value - a2.value);
        if (arg2 instanceof Constant a2 && a2.value == 0)
            return arg1;
        return new Subtract(arg1, arg2);
    }
    // endregion
}
