package macchiato.instructions;

import macchiato.Debugger;
import macchiato.Variables;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;

public class ForLoop extends Instruction {
    // region dane

    char iteratorName;
    Expression end;
    Instruction body;

    // endregion dane

    /**
     * Tworzy pętlę for.
     * @param iteratorName nazwa zmiennej iteratora
     * @param end wyrażenie określające wartość końcową iteratora
     * @param body ciało pętli
     */
    public ForLoop(char iteratorName, Expression end, Instruction body) {
        super(new Variables());
        this.iteratorName = iteratorName;
        this.end = end;
        this.body = body;
        body.parent = this;
    }

    @Override
    public String toString() {
        return "for "+iteratorName+" in 0.."+end.toString();
    }

    @Override
    public void execute() throws MacchiatoException {
        vars.declare(iteratorName, 0);
        int endValue = end.evaluate(this);
        for (int i = 0; i < endValue; i++) {
            vars.set(iteratorName, i);
            body.execute();
        }
    }

    @Override
    public void debugExecute(Debugger debugger) throws MacchiatoException {
        debugger.beforeExecute(this);
        vars.declare(iteratorName, 0);
        int endValue = end.evaluate(this);
        for (int i = 0; i < endValue; i++) {
            vars.set(iteratorName, i);
            body.debugExecute(debugger);
        }
    }
}
