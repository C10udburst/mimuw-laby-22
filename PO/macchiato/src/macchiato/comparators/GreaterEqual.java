package macchiato.comparators;

import macchiato.expressions.Expression;
import org.jetbrains.annotations.NotNull;

public class GreaterEqual extends Comparator {
    public GreaterEqual(@NotNull Expression left, @NotNull Expression right) {
        super(left, right);
    }

    @Override
    protected String symbol() {
        return ">=";
    }

    @Override
    public boolean compare(int left, int right) {
        return left >= right;
    }

    public static GreaterEqual of(Expression left, Expression right) {
        return new GreaterEqual(left, right);
    }
}
