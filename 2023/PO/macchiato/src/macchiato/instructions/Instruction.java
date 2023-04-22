package macchiato.instructions;

import macchiato.Debugger;
import macchiato.Variables;
import macchiato.exceptions.InvalidVariableNameException;
import macchiato.exceptions.MacchiatoException;
import macchiato.exceptions.UndeclaredVariableException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Instruction {
    // region dane

    protected Instruction parent;
    protected Variables vars;

    // endregion dane

    public Instruction(@NotNull Variables vars) {
        this.vars = vars;
    }

    public abstract String toString();

    public String dumpVars() {
        return vars.toString();
    }

    public int getVariable(char name) throws InvalidVariableNameException, UndeclaredVariableException {
        try {
            return vars.get(name);
        } catch (UndeclaredVariableException e) {
            if (parent == null)
                throw e;
            return parent.getVariable(name);
        }
    }

    public abstract void execute() throws MacchiatoException;

    public abstract void debugExecute(Debugger debugger) throws MacchiatoException;

    public @Nullable Instruction getParent(int depth) {
        if (depth == 0)
            return this;
        if (parent == null)
            return null;
        return parent.getParent(depth - 1);
    }
}
