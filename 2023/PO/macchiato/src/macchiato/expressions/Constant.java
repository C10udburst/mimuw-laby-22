package macchiato.expressions;

import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

public class Constant extends Expression {
    // region dane
    protected final int value;
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
    public int evaluate(@NotNull Instruction context) {
        return value;
    }
    // endregion

    public static Constant of(int v) {
        return new Constant(v);
    }
}
