package macchiato.comparators;

import macchiato.expressions.Expression;
import org.jetbrains.annotations.NotNull;

public class Equals extends Comparator {
    public Equals(@NotNull Expression left, @NotNull Expression right) {
        super(left, right);
    }

    @Override
    protected String symbol() {
        return "=";
    }

    @Override
    public boolean compare(int left, int right) {
        return left == right;
    }
}
