package macchiato.expressions;

import org.jetbrains.annotations.NotNull;

public abstract class Operator extends Expression {
    // region dane
    @NotNull
    protected final Expression arg1;
    @NotNull
    protected final Expression arg2;
    // endregion

    // region techniczne
    public Operator(@NotNull Expression arg1, @NotNull Expression arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    @Override
    public String toString() {
        // Wyświetlanie nawiasów zależy od priorytetów operatorów. Jeśli priorytet operatora jest większy niż priorytet argumentu, to argument jest otoczony nawiasami.
        return (
                (priority() > arg1.priority() ? "(" + arg1 + ")" : arg1.toString())
                        + symbol()
                        + (priority() >= arg2.priority() ? "(" + arg2 + ")" : arg2.toString())
        );
    }

    // Wymuszenie implementacji metody priority() w klasach dziedziczących
    @Override
    public abstract int priority();

    /**
     * Zwraca symbol operatora. Używany do wyświetlania wyrażenia.
     *
     * @return symbol operatora
     */
    public abstract String symbol();
    // endregion

    // region operacje

    // endregion
}
