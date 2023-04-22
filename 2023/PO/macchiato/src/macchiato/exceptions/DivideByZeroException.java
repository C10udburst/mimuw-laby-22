package macchiato.exceptions;

import macchiato.expressions.Expression;

public class DivideByZeroException extends ExpressionException {

    public DivideByZeroException() {
        super("Division by zero.");
    }
}
