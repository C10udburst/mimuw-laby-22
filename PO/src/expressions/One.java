package expressions;

public class One extends Constant {
    public One() {
        super(1.0);
    }

    @Override
    public String toString() {
        return "1";
    }

    @Override
    public Expression multiply(Expression e) {
        return e;
    }

    @Override
    protected Expression multiply2(Expression e) {
        return e;
    }
}
