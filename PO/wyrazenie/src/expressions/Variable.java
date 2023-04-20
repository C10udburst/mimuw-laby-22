package expressions;

public class Variable extends Expression {

    // region dane

    // endregion

    // region techniczne

    public Variable() {}

    @Override
    public String toString() {
        return "x";
    }

    // endregion

    // region operacje

    @Override
    public double evaluate(double x) {
        return x;
    }

    @Override
    public Expression derivative() {
        return new One();
    }

    @Override
    public double integrate(double a, double b, int n) {
        // ∫ x = x^2 / 2
        return (b*b - a*a)/2;
    }

    // endregion
}
