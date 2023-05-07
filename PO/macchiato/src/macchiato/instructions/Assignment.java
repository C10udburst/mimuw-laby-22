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
        super(null);
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return variable + " = " + expression.toString();
    }

    @Override
    public String getShortName() {
        return "set";
    }

    // endregion techniczne

    // region operacje
    @Override
    public void execute() throws MacchiatoException {
        super.execute();
        setVariable(variable, expression.evaluate(this));
    }

    @Override
    public void debugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        super.debugExecute(debugger);
        debugger.beforeExecute(this);
        setVariable(variable, expression.evaluate(this));
    }
    // endregion operacje
}
