package macchiato.instructions;

import macchiato.Debugger;
import macchiato.Declaration;
import macchiato.exceptions.MacchiatoException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Klasa reprezentująca blok główny programu.
 * Od zwykłego bloku różni się tym, że po wykonaniu wypisuje na standardowe wyjście wartości wszystkich zmiennych.
 */
public class MainBlock extends Block {
    // region dane
    // endregion dane

    // region techniczne
    /**
     * Tworzy nowy blok główny.
     * @param declarations deklaracje zmiennych.
     * @param instructions instrukcje.
     */
    public MainBlock(@NotNull List<Declaration> declarations, @NotNull List<Instruction> instructions) {
        super(declarations, instructions);
    }
    // endregion techniczne

    @Override
    public void execute() throws MacchiatoException {
        super.execute();
        System.out.println(dumpVars());
    }

    @Override
    public void debugExecute(@NotNull Debugger debugger) throws MacchiatoException {
        super.debugExecute(debugger);
        System.out.println(dumpVars());
    }
}
