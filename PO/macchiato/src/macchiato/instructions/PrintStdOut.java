package macchiato.instructions;

import macchiato.debugging.DebugHook;
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
        super(false);
        if (!Character.isLetter(variable)) {
            throw new IllegalArgumentException("Variable name must be a letter");
        }
        this.variable = variable;
    }
    @Override
    public String toString() {
        return "print " + variable;
    }

    @Override
    public String getShortName() {
        return "print";
    }

    // endregion techniczne

    // region operacje
    @Override
    public void execute() throws MacchiatoException {
        super.execute();
        System.out.println(getVariable(variable));
    }

    @Override
    public void debugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        super.debugExecute(debugger);
        debugger.beforeExecute(this);
        System.out.println(getVariable(variable));
    }

    /**
     * Zwraca nazwę zmiennej, której wartość zostanie wypisana.
     * Używane przez MacchiatoTest.
     * @return nazwa zmiennej
     */
    public char getVariableName() {
        return variable;
    }
    // endregion operacje
}
