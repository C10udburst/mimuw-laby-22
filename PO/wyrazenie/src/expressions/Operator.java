package expressions;

public abstract class Operator extends Expression {
    // region dane

    protected Expression arg1;
    protected Expression arg2;

    // endregion

    // region techniczne

    public Operator(Expression arg1, Expression arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    @Override
    public String toString() {
        // Wyświetlanie nawiasów zależy od priorytetów operatorów. Jeśli priorytet operatora jest większy niż priorytet argumentu, to argument jest otoczony nawiasami.
        return (
                (priority() > arg1.priority() ? "(" + arg1.toString() + ")" : arg1.toString())
                + symbol()
                + (priority() >= arg2.priority() ? "(" + arg2.toString() + ")" : arg2.toString())
        );
    }

    // Wymuszenie implementacji metody priority() w klasach dziedziczących
    @Override
    public abstract int priority();

    /**
     * Zwraca symbol operatora. Używany do wyświetlania wyrażenia.
     * @return symbol operatora
     */
    public abstract String symbol();

    // endregion

    // region operacje

    // endregion
}
