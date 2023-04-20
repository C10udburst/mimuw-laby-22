package expressions;

public class Constant extends Expression {
    // region dane
    
    double value;

    // endregion

    // region techniczne

    protected Constant(double v) {
        value = v;
    }

    /**
     * Tworzy stałą o wartości podanej jako argument, jeśli wartość jest 0 lub 1, to zwraca odpowiednią klasę
     * @param v wartość stałej
     * @return stała o wartości podanej jako argument
     */
    public static Constant fromDouble(double v) {
        if (Math.abs(v) == 0.0) // = 0 == -0
            return new Zero();
        else if (v == 1.0)
            return new One();
        else
            return new Constant(v);
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    // endregion

    // region operacje

    @Override
    public double evaluate(double x) {
        return value;
    }

    @Override
    public Expression derivative() {
        return new Zero();
    }

    @Override
    public double integrate(double a, double b, int n) {
        return (b-a)*value;
    }

    @Override
    public Expression multiply(Expression e) {
        if (e instanceof Constant constant)
            return Constant.fromDouble(value * constant.value);
        else
            return super.multiply(e);
    }

    @Override
    protected Expression multiply2(Expression e) {
        if (e instanceof Constant constant)
            return Constant.fromDouble(value * constant.value);
        else
            return super.multiply2(e);
    }

    @Override
    public Expression add(Expression e) {
        if (e instanceof Constant constant)
            return Constant.fromDouble(value + constant.value);
        else
            return super.add(e);
    }

    @Override
    protected Expression add2(Expression e) {
        if (e instanceof Constant constant)
            return Constant.fromDouble(value + constant.value);
        else
            return super.add2(e);
    }

    // endregion
}
