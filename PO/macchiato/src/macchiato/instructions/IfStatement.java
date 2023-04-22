package macchiato.instructions;

import macchiato.Debugger;
import macchiato.Variables;
import macchiato.comparators.Comparator;
import macchiato.exceptions.MacchiatoException;
import org.jetbrains.annotations.Nullable;

public class IfStatement extends Instruction {
    // region dane

    protected Comparator comparator;
    @Nullable
    protected Instruction thenInstruction;
    @Nullable
    protected Instruction elseInstruction;

    // endregion dane

    /**
     * Tworzy instrukcję warunkową. Jeśli instrukcja warunkowa nie ma instrukcji else, to należy przekazać null.
     * @param comparator warunek, który ma być sprawdzony
     * @param thenInstruction instrukcja, która ma być wykonana, jeśli warunek jest prawdziwy
     * @param elseInstruction instrukcja, która ma być wykonana, jeśli warunek jest fałszywy
     */
    public IfStatement(Comparator comparator, @Nullable Instruction thenInstruction, @Nullable Instruction elseInstruction) {
        super(null);
        this.comparator = comparator;
        this.thenInstruction = thenInstruction;
        this.elseInstruction = elseInstruction;

        if (thenInstruction != null)
            thenInstruction.parent = this;
        if (elseInstruction != null)
            elseInstruction.parent = this;
    }

    @Override
    public String toString() {
        return "if " + comparator.toString() + "";
    }

    @Override
    public void execute() throws MacchiatoException {
        if (comparator.execute(this)) {
            if (thenInstruction != null)
                thenInstruction.execute();
        } else {
            if (elseInstruction != null)
                elseInstruction.execute();
        }
    }

    @Override
    public void debugExecute(Debugger debugger) throws MacchiatoException {
        if (comparator.execute(this)) {
            if (thenInstruction != null)
                thenInstruction.debugExecute(debugger);
        } else {
            if (elseInstruction != null)
                elseInstruction.debugExecute(debugger);
        }
    }
}
