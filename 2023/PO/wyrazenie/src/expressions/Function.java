package expressions;

public abstract class Function extends Expression {
    // region dane

    Expression arg;

    // endregion

    // region techniczne

    public Function(Expression arg) {
        this.arg = arg;
    }

    // endregion

    // region operacje

    // endregion
}
