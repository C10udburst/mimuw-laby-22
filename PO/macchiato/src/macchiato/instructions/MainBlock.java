package macchiato.instructions;

import macchiato.Debugger;
import macchiato.Declaration;
import macchiato.exceptions.MacchiatoException;

import java.util.List;

/**
 * Klasa reprezentująca blok główny programu.
 */
public class MainBlock extends Block {

    /**
     * Tworzy nowy blok główny.
     * @param declarations deklaracje zmiennych.
     * @param instructions instrukcje.
     */
    public MainBlock(List<Declaration> declarations, List<Instruction> instructions) {
        super(declarations, instructions);
    }

    @Override
    public void execute() throws MacchiatoException {
        super.execute();
        System.out.println(dumpVars());
    }

    @Override
    public void debugExecute(Debugger debugger) throws MacchiatoException {
        super.debugExecute(debugger);
        System.out.println(dumpVars());
    }
}
