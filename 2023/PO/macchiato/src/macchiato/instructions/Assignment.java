package macchiato.instructions;

import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;
import org.jetbrains.annotations.NotNull;

public class Assignment extends Instruction {
    // region dane
    @NotNull private final Expression expression;
    private final char variable;
    // endregion dane

    // region techniczne
    /**
     * Konstruktor
     * @param variable zmienna, do której przypisujemy wartość
     * @param expression wyrażenie, którego wartość przypisujemy
     */
    public Assignment(char variable, @NotNull Expression expression) {
        super();
        if (!Character.isLetter(variable)) {
            throw new IllegalArgumentException("Variable name must be a letter");
        }
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return variable + " = " + expression;
    }

    @Override
    public String getShortName() {
        return "set";
    }

    // endregion techniczne

    // region operacje
    @Override
    protected void internalExecute() throws MacchiatoException {
        setVariable(variable, expression.evaluate(this));
    }

    @Override
    protected void internalDebugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        debugger.beforeExecute(this);
        setVariable(variable, expression.evaluate(this));
    }
    // endregion operacje
}
