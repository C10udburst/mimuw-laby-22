package macchiato.comparators;

import macchiato.expressions.Expression;
import org.jetbrains.annotations.NotNull;

public class LessEqual extends Comparator {
    public LessEqual(@NotNull Expression left, @NotNull Expression right) {
        super(left, right);
    }

    @Override
    protected String symbol() {
        return "<=";
    }

    @Override
    public boolean compare(int left, int right) {
        return left <= right;
    }

    public static LessEqual of(Expression left, Expression right) {
        return new LessEqual(left, right);
    }
}
