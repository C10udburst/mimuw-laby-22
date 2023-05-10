package macchiato.instructions;

import macchiato.Declaration;
import macchiato.debugging.DebugHook;
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

    /**
     * Wykonuje blok główny. Po bezbłędnym wykonaniu wypisuje na standardowe wyjście wartości wszystkich zmiennych w głównym bloku.
     * Jeśli wystąpi błąd, wypisuje błąd i wartości wszystkich zmiennych w bloku, w którym wystąpił.
     */
    @Override
    public void execute() {
        try {
            super.execute();
        } catch (MacchiatoException e) {
            System.err.println("An error occured: " + e.getMessage());
            System.out.println(e.context.dumpVars());
        } finally {
            System.out.println(dumpVars());
        }
    }

    @Override
    public void debugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        super.debugExecute(debugger);
        System.out.println(dumpVars());
    }
}
