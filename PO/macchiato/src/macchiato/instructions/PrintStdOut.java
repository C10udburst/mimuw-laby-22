package macchiato.instructions;

import macchiato.Debugger;
import macchiato.exceptions.MacchiatoException;
import org.jetbrains.annotations.NotNull;

public class PrintStdOut extends Instruction {
    // region dane
    private final char variable;
    // endregion dane

    // region techniczne
    /**
     * Wypisuje na standardowe wyjście wartość zmiennej.
     * @param variable nazwa zmiennej
     */
    public PrintStdOut(char variable) {
        super(null);
        this.variable = variable;
    }
    @Override
    public String toString() {
        return "print " + variable;
    }
    // endregion techniczne

    // region operacje
    @Override
    public void execute() throws MacchiatoException {
        System.out.println(getVariable(variable));
    }

    @Override
    public void debugExecute(@NotNull Debugger debugger) throws MacchiatoException {
        debugger.beforeExecute(this);
        System.out.println(getVariable(variable));
    }
    // endregion operacje
}
