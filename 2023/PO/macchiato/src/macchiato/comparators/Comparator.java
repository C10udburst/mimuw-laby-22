package macchiato.comparators;

import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;
import macchiato.instructions.Instruction;

public abstract class Comparator {
    // region dane

    Expression left;
    Expression right;

    // endregion dane

    public Comparator(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }


    @Override
    public String toString() {
        return left.toString() + " " + symbol() + " " + right.toString();
    }

    /**
     * @return symbol porównania
     */
    protected abstract String symbol();

    /**
     * Wykonuje porównanie.
     * @return wynik porównania
     * @throws MacchiatoException jeśli wystąpi błąd podczas ewaluacji wyrażeń
     */
    public boolean execute(Instruction context) throws MacchiatoException {
        return compare(left.evaluate(context), right.evaluate(context));
    }

    public abstract boolean compare(int left, int right);
}
