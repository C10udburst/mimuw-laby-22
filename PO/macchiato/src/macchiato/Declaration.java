package macchiato;

import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;
import macchiato.instructions.Instruction;

public class Declaration {
    private final char name;
    private final Expression expression;

    @Override
    public String toString() {
        return name + " = " + expression;
    }

    public Declaration(char name, Expression expression) {
        this.name = name;
        this.expression = expression;
    }

    public int execute(Instruction parent) throws MacchiatoException {
        return expression.evaluate(parent);
    }

    public char getName() {
        return name;
    }
}
