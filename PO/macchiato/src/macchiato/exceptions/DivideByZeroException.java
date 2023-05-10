package macchiato.exceptions;

import macchiato.expressions.Expression;
import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

public class DivideByZeroException extends MacchiatoException {

    @NotNull public final Expression expression;

    public DivideByZeroException(@NotNull Expression expression, @NotNull Instruction context) {
        super("Division by zero in expression: " + expression + ".", context);
        this.expression = expression;
    }
}
