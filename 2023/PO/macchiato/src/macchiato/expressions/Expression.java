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
    // endregion
}
