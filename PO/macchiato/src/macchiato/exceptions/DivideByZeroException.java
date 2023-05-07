package macchiato.exceptions;

import macchiato.expressions.Expression;
import org.jetbrains.annotations.NotNull;

public class DivideByZeroException extends MacchiatoException {

    @NotNull public final Expression expression;

    public DivideByZeroException(@NotNull Expression expression) {
        super("Division by zero in expression: " + expression + ".");
        this.expression = expression;
    }
}
