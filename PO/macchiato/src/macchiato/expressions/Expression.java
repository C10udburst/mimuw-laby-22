package macchiato.expressions;

import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.Instruction;

public abstract class Expression {
    // region dane

    // endregion

    // region techniczne

    // wymuszenie implementacji metody toString() w klasach dziedziczących
    @Override
    public abstract String toString();

    /**
     * Zwraca priorytet wyrażenia, czyli kolejność wykonywania operacji.
     * Im większy priorytet, tym wyrażenie jest wykonywane wcześniej w zwykłej notacji.
     * @return priorytet wyrażenia
     */
    public int priority() {
        return 1_000;
    }

    // endregion

    // region operacje

    /**
     * Oblicza wartość wyrażenia dla podanej wartości zmiennej.
     *
     * @return wartość wyrażenia
     */
    public abstract int evaluate(Instruction context) throws MacchiatoException;

    /**
     * Mnoży wyrażenie przez inne wyrażenie (this*e), ale upraszcza je, jeśli to możliwe.
     * @param e wyrażenie, przez które mnożymy
     * @return uproszczone wyrażenie
     */
    public Expression multiply(Expression e) {
        // spróbuj uprościć mnożenie
        // wersja domyślna nie potrafi upraszczać
        return e.multiply2(this);
    }

    /**
     * Mnoży wyrażenie przez inne wyrażenie (e*this), ale upraszcza je, jeśli to możliwe. Używane wewnętrznie przez multiply().
     * @param e wyrażenie, przez które mnożymy
     * @return uproszczone wyrażenie
     */
    protected Expression multiply2(Expression e) {
        // jestem drugim argumentem mnożenia, pierwszy nie potrafił uprościć,
        // może ja potrafię (double-dispatch, podwójne przekierowanie)
        // wersja domyślna-nie potrafi upraszczać
        return new Multiply(e, this);
    }

    /**
     * Dodaje wyrażenie do innego wyrażenia (this+e), ale upraszcza je, jeśli to możliwe.
     * @param e wyrażenie, które dodajemy
     * @return uproszczone wyrażenie
     */
    public Expression add(Expression e) {
        // spróbuj uprościć dodawanie
        // wersja domyślna nie potrafi upraszczać
        return e.add2(this);
    }

    /**
     * Dodaje wyrażenie do innego wyrażenia (e+this), ale upraszcza je, jeśli to możliwe. Używane wewnętrznie przez add().
     * @param e wyrażenie, które dodajemy
     * @return uproszczone wyrażenie
     */
    protected Expression add2(Expression e) {
        // jestem drugim argumentem dodawania, pierwszy nie potrafił uprościć,
        // może ja potrafię (double-dispatch, podwójne przekierowanie)
        // wersja domyślna-nie potrafi upraszczać
        return new Add(e, this);
    }
    // endregion
}
