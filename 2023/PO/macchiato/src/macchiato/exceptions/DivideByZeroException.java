package macchiato.exceptions;

import macchiato.expressions.Expression;

public class DivideByZeroException extends MacchiatoException {

    public DivideByZeroException(Expression expression) {
        super("Division by zero in expression: " + expression.toString() + ".");
    }
}
