package macchiato.instructions;

import macchiato.Debugger;
import macchiato.Variables;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Expression;
import org.jetbrains.annotations.NotNull;

public class ForLoop extends Instruction {
    // region dane
    private final char iteratorName;
    private final Expression end;
    private final Instruction body;
    // endregion dane

    // region techniczne
    /**
     * Tworzy pętlę for.
     * @param iteratorName nazwa zmiennej iteratora
     * @param end wyrażenie określające wartość końcową iteratora
     * @param body ciało pętli
     */
    public ForLoop(char iteratorName, @NotNull Expression end, @NotNull Instruction body) {
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
    // endregion techniczne

    // region operacje
    @Override
    public void execute() throws MacchiatoException {
        assert vars != null; // nie powinno się zdarzyć, bo konstruktor tworzy nowe zmienne
        vars.declare(iteratorName, 0);
        int endValue = end.evaluate(this);
        for (int i = 0; i < endValue; i++) {
            vars.set(iteratorName, i);
            body.execute();
        }
    }

    @Override
    public void debugExecute(@NotNull Debugger debugger) throws MacchiatoException {
        assert vars != null; // nie powinno się zdarzyć, bo konstruktor tworzy nowe zmienne
        debugger.beforeExecute(this);
        vars.declare(iteratorName, 0);
        int endValue = end.evaluate(this);
        for (int i = 0; i < endValue; i++) {
            vars.set(iteratorName, i);
            body.debugExecute(debugger);
        }
    }
    // endregion operacje
}
