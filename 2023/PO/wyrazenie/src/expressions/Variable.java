package expressions;

public class Variable extends Expression {

    public Variable() {}

    @Override
    public String toString() {
        return "x";
    }

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
        return (b*b - a*a)/2;
    }
}
