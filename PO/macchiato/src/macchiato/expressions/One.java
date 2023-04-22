package macchiato.expressions;

public class One extends Constant {
    // region dane

    // endregion

    // region techniczne

    public One() {
        super(1);
    }

    @Override
    public String toString() {
        return "1";
    }

    // endregion

    // region operacje

    @Override
    public Expression multiply(Expression e) {
        return e;
    }

    @Override
    protected Expression multiply2(Expression e) {
        return e;
    }

    // endregion
}
