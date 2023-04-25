package macchiato.instructions;

import macchiato.Debugger;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;
import org.jetbrains.annotations.NotNull;

public class Assignment extends Instruction {
    // region dane
    private final Expression expression;
    private final char variable;
    // endregion dane

    // region techniczne
    /**
     * Konstruktor
     * @param variable zmienna, do której przypisujemy wartość
     * @param expression wyrażenie, którego wartość przypisujemy
     */
    public Assignment(char variable, Expression expression) {
        super(null);
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return variable + " = " + expression.toString();
    }
    // endregion techniczne

    // region operacje
    @Override
    public void execute() throws MacchiatoException {
        setVariable(variable, expression.evaluate(this));
    }

    @Override
    public void debugExecute(@NotNull Debugger debugger) throws MacchiatoException {
        debugger.beforeExecute(this);
        setVariable(variable, expression.evaluate(this));
    }
    // endregion operacje
}
