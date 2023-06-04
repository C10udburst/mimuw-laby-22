package macchiato.expressions;

import macchiato.exceptions.DivideByZeroException;
import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

public class Modulo extends Operator {
    // region dan
    // endregion

    // region techniczne
    public Modulo(@NotNull Expression arg1, @NotNull Expression arg2) {
        super(arg1, arg2);
    }

    @Override
    public int priority() {
        return 500;
    }

    @Override
    public String symbol() {
        return "%";
    }
    // endregion

    // region operacje
    @Override
    public int evaluate(@NotNull Instruction context) throws MacchiatoException {
        int arg2Value = arg2.evaluate(context);
        if (arg2Value == 0)
            throw new DivideByZeroException(this, context);
        return arg1.evaluate(context) % arg2Value;
    }
    // endregion

    // region fabryka
    public static Expression of(@NotNull Expression arg1, @NotNull Expression arg2) {
        if (arg2 instanceof Constant a2 && a2.value == 0)
            throw new ArithmeticException("Modulo by zero");
        if (arg1 instanceof Constant a1 && arg2 instanceof Constant a2)
            return Constant.of(a1.value % a2.value);
        if (arg2 instanceof Constant a2 && a2.value == 1)
            return Constant.of(0);
        if (arg1 instanceof Constant a1 && a1.value == 0)
            return Constant.of(0);
        return new Modulo(arg1, arg2);
    }
    // endregion
}
