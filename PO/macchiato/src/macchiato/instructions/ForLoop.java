package macchiato.instructions;

import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;
import org.jetbrains.annotations.NotNull;

public class ForLoop extends Instruction {
    // region dane
    private final char iteratorName;
    @NotNull private final Expression end;
    @NotNull private final Instruction body;
    // endregion dane

    // region techniczne
    /**
     * Tworzy pętlę for.
     * @param iteratorName nazwa zmiennej iteratora
     * @param end wyrażenie określające wartość końcową iteratora
     * @param body ciało pętli
     */
    public ForLoop(char iteratorName, @NotNull Expression end, @NotNull Instruction body) {
        super();
        this.iteratorName = iteratorName;
        this.end = end;
        this.body = body;
        body.parent = this;
    }

    @Override
    public String toString() {
        return "for "+iteratorName+" in 0.."+ end + " do " + body.getShortName();
    }

    @Override
    public String getShortName() {
        return "for";
    }

    // endregion techniczne

    // region operacje
    @Override
    protected void internalExecute() throws MacchiatoException {
        vars.peek().declare(iteratorName, 0);
        int endValue = end.evaluate(this);
        for (int i = 0; i < endValue; i++) {
            vars.peek().set(iteratorName, i);
            body.execute();
        }
    }

    @Override
    protected void internalDebugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        vars.peek().declare(iteratorName, 0);

        debugger.beforeExecute(this);

        int endValue = end.evaluate(this); // koniec pętli liczymy tylko raz

        for (int i = 0; i < endValue; i++) {
            vars.peek().set(iteratorName, i);
            body.debugExecute(debugger);
        }
    }
    // endregion operacje
}
