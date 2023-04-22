package macchiato.instructions;

import macchiato.Debugger;
import macchiato.exceptions.MacchiatoException;

public class PrintStdOut extends Instruction {
    // region dane

    char variable;

    // endregion dane

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

    @Override
    public void execute() throws MacchiatoException {
        System.out.println(getVariable(variable));
    }

    @Override
    public void debugExecute(Debugger debugger) throws MacchiatoException {
        debugger.beforeExecute(this);
        System.out.println(getVariable(variable));
    }
}
