package macchiato.expressions;

import macchiato.instructions.Instruction;

public class Constant extends Expression {
    // region dane
    
    int value;

    // endregion

    // region techniczne

    protected Constant(int v) {
        value = v;
    }

    /**
     * Tworzy stałą o wartości podanej jako argument, jeśli wartość jest 0 lub 1, to zwraca odpowiednią klasę
     * @param v wartość stałej
     * @return stała o wartości podanej jako argument
     */
    public static Constant fromInt(int v) {
        if (Math.abs(v) == 0.0) // = 0 == -0
            return new Zero();
        else if (v == 1.0)
            return new One();
        else
            return new Constant(v);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    // endregion

    // region operacje

    @Override
    public int evaluate(Instruction context) {
        return value;
    }

    @Override
    public Expression multiply(Expression e) {
        if (e instanceof Constant constant)
            return Constant.fromInt(value * constant.value);
        else
            return super.multiply(e);
    }

    @Override
    protected Expression multiply2(Expression e) {
        if (e instanceof Constant constant)
            return Constant.fromInt(value * constant.value);
        else
            return super.multiply2(e);
    }

    @Override
    public Expression add(Expression e) {
        if (e instanceof Constant constant)
            return Constant.fromInt(value + constant.value);
        else
            return super.add(e);
    }

    @Override
    protected Expression add2(Expression e) {
        if (e instanceof Constant constant)
            return Constant.fromInt(value + constant.value);
        else
            return super.add2(e);
    }

    // endregion
}
