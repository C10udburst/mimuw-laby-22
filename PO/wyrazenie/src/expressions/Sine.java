package expressions;

public class Sine extends Function {
    // region dane

    // endregion

    // region techniczne 

    public Sine(Expression arg) {
        super(arg);
    }

    @Override
    public String toString() {
        return "sin(" + arg.toString() + ")";
    }

    // endregion

    // region operacje

    @Override
    public double evaluate(double x) {
        return Math.sin(x);
    }

    @Override
    public Expression derivative() {
        return (new Cosine(arg)).multiply(arg.derivative());
    }

    // endregion
}
