package macchiato.comparators;

import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;
import macchiato.instructions.Instruction;

public abstract class Comparator {
    // region dane

    Expression left;
    Expression right;

    // endregion dane

    // region techniczne
    public Comparator(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return left.toString() + " " + symbol() + " " + right.toString();
    }

    /**
     * Zwraca priorytet operatora, używany przy wypisywaniu.
     * @return symbol porównania
     */
    protected abstract String symbol();

    // endregion techniczne

    // region operacje
    /**
     * Wykonuje porównanie.
     * @return wynik porównania
     * @throws MacchiatoException jeśli wystąpi błąd podczas ewaluacji wyrażeń
     */
    public boolean execute(Instruction context) throws MacchiatoException {
        return compare(left.evaluate(context), right.evaluate(context));
    }

    /**
     * Porównuje dwie liczby.
     * @param left lewa liczba
     * @param right prawa liczba
     * @return wynik porównania
     */
    public abstract boolean compare(int left, int right);
    // endregion operacje
}
