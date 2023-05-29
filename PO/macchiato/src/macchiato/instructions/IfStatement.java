package macchiato.instructions;

import macchiato.comparators.Comparator;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IfStatement extends Instruction {
    // region dane
    @NotNull protected final Comparator comparator;
    @NotNull protected final Instruction thenInstruction;
    @Nullable protected final Instruction elseInstruction;
    // endregion dane

    // region techniczne
    /**
     * Tworzy instrukcję warunkową. Jeśli instrukcja warunkowa nie ma instrukcji else, to należy przekazać null.
     * @param comparator warunek, który ma być sprawdzony
     * @param thenInstruction instrukcja, która ma być wykonana, jeśli warunek jest prawdziwy
     * @param elseInstruction instrukcja, która ma być wykonana, jeśli warunek jest fałszywy
     */
    public IfStatement(@NotNull Comparator comparator, @NotNull Instruction thenInstruction, @Nullable Instruction elseInstruction) {
        super(false);
        this.comparator = comparator;
        this.thenInstruction = thenInstruction;
        this.elseInstruction = elseInstruction;

        thenInstruction.parent = this;
        if (elseInstruction != null)
            elseInstruction.parent = this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("if ");
        sb.append(comparator);
        sb.append(" then ");
        sb.append(thenInstruction.getShortName());
        if (elseInstruction != null) {
            sb.append(" else ");
            sb.append(elseInstruction.getShortName());
        }
        return sb.toString();
    }

    @Override
    public String getShortName() {
        return "if";
    }

    // endregion techniczne

    // region operacje
    @Override
    public void execute() throws MacchiatoException {
        super.execute();
        if (comparator.execute(this)) {
            thenInstruction.execute();
        } else {
            if (elseInstruction != null)
                elseInstruction.execute();
        }
    }

    @Override
    public void debugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        super.debugExecute(debugger);
        debugger.beforeExecute(this);
        if (comparator.execute(this)) {
            thenInstruction.debugExecute(debugger);
        } else {
            if (elseInstruction != null)
                elseInstruction.debugExecute(debugger);
        }
    }
    // endregion operacje
}
