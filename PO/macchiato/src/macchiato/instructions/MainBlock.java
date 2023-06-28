package macchiato.instructions;

import macchiato.Declaration;
import macchiato.Variables;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.procedures.Procedure;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Klasa reprezentująca blok główny programu.
 * Od zwykłego bloku różni się tym, że po wykonaniu wypisuje na standardowe wyjście wartości wszystkich zmiennych.
 */
public class MainBlock extends Block {
    // region dane
    // endregion dane

    // region techniczne
    /**
     * Tworzy nowy blok główny. Należy użyć {@link macchiato.builder.ProgramBuilder} zamiast konstruktora.
     *
     * @param declarations deklaracje zmiennych.
     * @param instructions instrukcje.
     */
    public MainBlock(@NotNull List<Declaration> declarations, @NotNull List<Instruction> instructions, @NotNull Map<String, Procedure> procedures) {
        super(declarations, instructions, procedures);
    }

    public MainBlock(@NotNull List<Declaration> declarations, @NotNull List<Instruction> instructions) {
        super(declarations, instructions, Map.of());
    }

    @Override
    public String getShortName() {
        return "main";
    }

    // endregion techniczne

    /**
     * Jeśli wystąpi błąd, wypisuje błąd i wartości wszystkich zmiennych w bloku, w którym wystąpił.
     */
    @Override
    public void execute() {
        try {
            super.execute();
        } catch (MacchiatoException e) {
            System.err.println("An error occured: " + e.getMessage());
            System.out.println(e.context.dumpVars());
        }
    }

    @Override
    public void debugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        vars.push(new Variables(this));
        internalDebugExecute(debugger);
        // nie robimy popa, bo to debugger może chcieć zobaczyć wartości zmiennych po wykonaniu
    }

    @Override
    protected void internalExecute() throws MacchiatoException {
        super.internalExecute();
        System.out.println(dumpVars());
    }

    @Override
    protected void internalDebugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        super.internalDebugExecute(debugger);
        System.out.println(dumpVars());
    }
}
