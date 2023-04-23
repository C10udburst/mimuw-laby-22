package macchiato.comparators;

import macchiato.expressions.Expression;

public class Equals extends Comparator {
    public Equals(Expression left, Expression right) {
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
