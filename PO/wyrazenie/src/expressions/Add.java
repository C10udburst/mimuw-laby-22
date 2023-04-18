package expressions;

public class Add extends Operator {

    // region techniczne
    protected Add(Expression arg1, Expression arg2) {
        super(arg1, arg2);
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public String symbol() {
        return "+";
    }

    // endregion


    // region operacje
    @Override
    public double evaluate(double x) {
        return arg1.evaluate(x)+ arg2.evaluate(x);
    }

    @Override
    public Expression derivative() {
        // Pochodna sumy jest sumą pochodnych
        return arg2.derivative().add(arg1.derivative());
    }

    @Override
    public double integrate(double a, double b, int n) {
        // Całka z sumy jest sumą całek
        return arg1.integrate(a, b, n) + arg2.integrate(a, b, n);
    }

    // endregion
}
