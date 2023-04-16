package expressions;

public abstract class Function extends Expression {
    Expression arg;

    public Function(Expression arg) {
        this.arg = arg;
    }
}
