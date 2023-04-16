package expressions;

public class Cosine extends Function {
    public Cosine(Expression arg) {
        super(arg);
    }

    @Override
    public String toString() {
        return "cos(" + arg.toString() + ")";
    }

    @Override
    public double evaluate(double x) {
        return Math.cos(x);
    }

    @Override
    public Expression derivative() {
        return Constant.fromDouble(-1f).multiply(new Sine(arg).multiply(arg.derivative()));
    }
}
