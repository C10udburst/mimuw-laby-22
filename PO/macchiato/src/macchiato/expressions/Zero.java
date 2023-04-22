package macchiato.expressions;

public class Zero extends Constant {

    // region dane

    // endregion

    // region techniczne

    public Zero() {
        super(0);
    }

    @Override
    public String toString() {
        return "0";
    }

    // endregion

    // region operacje

    @Override
    public Expression multiply(Expression e) {
        return this;
    }

    @Override
    protected Expression multiply2(Expression e) {
        return this;
    }

    @Override
    public Expression add(Expression e) {
        return e;
    }

    @Override
    protected Expression add2(Expression e) {
        return e;
    }

    // endregion
}
