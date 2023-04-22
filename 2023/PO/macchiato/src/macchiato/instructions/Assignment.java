package macchiato.instructions;

import macchiato.Debugger;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;

public class Assignment extends Instruction {
    // region dane

    Expression expression;
    char variable;

    // endregion dane

    public Assignment(char variable, Expression expression) {
        super(null);
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return variable+ " = " + expression.toString();
    }

    @Override
    public void execute() throws MacchiatoException {
        setVariable(variable, expression.evaluate(this));
    }

    @Override
    public void debugExecute(Debugger debugger) throws MacchiatoException {
        debugger.beforeExecute(this);
        setVariable(variable, expression.evaluate(this));
    }
}
