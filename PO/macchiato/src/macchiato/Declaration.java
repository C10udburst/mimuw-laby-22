package macchiato;

import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;
import macchiato.instructions.Instruction;

/**
 * Klasa reprezentująca deklarację zmiennej. Zawiera nazwę zmiennej i wyrażenie, które staje się jej wartością.
 */
public class Declaration {
    private final char name;
    private final Expression expression;

    @Override
    public String toString() {
        return name + " = " + expression;
    }

    /** Tworzy deklarację zmiennej.
     * @param name nazwa zmiennej
     * @param expression wyrażenie, które staje się wartością zmiennej
     */
    public Declaration(char name, Expression expression) {
        this.name = name;
        this.expression = expression;
    }

    /**
     * Ewaluuje wyrażenie i zwraca jego wartość.
     * @param parent instrukcja, która zawiera tę deklarację
     * @return wartość wyrażenia
     */
    public int execute(Instruction parent) throws MacchiatoException {
        return expression.evaluate(parent);
    }

    /**
     * Zwraca nazwę zmiennej.
     * @return nazwa zmiennej
     */
    public char getName() {
        return name;
    }
}
