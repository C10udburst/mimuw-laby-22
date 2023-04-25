package macchiato.exceptions;

import macchiato.expressions.Expression;

public class DivideByZeroException extends MacchiatoException {

    public final Expression expression;

    public DivideByZeroException(Expression expression) {
        super("Division by zero in expression: " + expression.toString() + ".");
        this.expression = expression;
    }
}
