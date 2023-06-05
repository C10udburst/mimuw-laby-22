package macchiato;

import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;
import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

/**
 * Klasa reprezentująca deklarację zmiennej. Zawiera nazwę zmiennej i wyrażenie, które staje się jej wartością.
 */
public class Declaration {
    private final char name;
    @NotNull
    private final Expression expression;

    @Override
    public String toString() {
        return name + " = " + expression;
    }

    /** Tworzy deklarację zmiennej.
     * @param name nazwa zmiennej
     * @param expression wyrażenie, które staje się wartością zmiennej
     */
    public Declaration(char name, @NotNull Expression expression) {
        if (!Character.isLetter(name))
            throw new IllegalArgumentException("Variable name must be a letter");
        this.name = name;
        this.expression = expression;
    }

    /**
     * Ewaluuje wyrażenie i zwraca jego wartość.
     * @param parent instrukcja, która zawiera tę deklarację
     * @return wartość wyrażenia
     */
    public int execute(@NotNull Instruction parent) throws MacchiatoException {
        return expression.evaluate(parent);
    }

    /**
     * Zwraca nazwę zmiennej.
     * @return nazwa zmiennej
     */
    public char getName() {
        return name;
    }

    public static Declaration of(char name, @NotNull Expression expression) {
        return new Declaration(name, expression);
    }
}
