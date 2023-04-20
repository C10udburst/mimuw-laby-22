package expressions;

public class Multiply extends Operator {
    // region dane

    // endregion

    // region techniczne

    protected Multiply(Expression arg1, Expression arg2) {
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
    public double evaluate(double x) {
        return arg1.evaluate(x) * arg2.evaluate(x);
    }

    @Override
    public Expression derivative() {
        // arg1' * arg2 + arg1 * arg2'
        return arg1.multiply(arg2.derivative()).add(arg2.multiply(arg1.derivative()));
    }

    // endregion
}
