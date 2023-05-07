package macchiato.instructions;

import macchiato.Declaration;
import macchiato.Variables;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Block extends Instruction {
    // region dane
    @NotNull private final List<Declaration> declarations;
    @NotNull private final List<Instruction> instructions;
    // endregion dane

    // region techniczne
    /**
     * Tworzy blok instrukcji.
     * @param declarations deklaracje zmiennych, które mają być zadeklarowane w tym bloku przed wykonaniem instrukcji
     * @param instructions instrukcje do wykonania
     */
    public Block(@NotNull List<Declaration> declarations, @NotNull List<Instruction> instructions) {
        super(new Variables());

        this.declarations = declarations;
        this.instructions = instructions;

        // ustawia ten blok jako nadrzędny dla wszystkich instrukcji
        for (Instruction instruction : instructions)
            instruction.parent = this;

        assert vars != null; // nie powinno się zdarzyć, bo konstruktor tworzy nowe zmienne
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Block: \n");
        sb.append("\t");
        sb.append("Deklaracje (").append(declarations.size()).append("): {");
        for (int i = 0; i < declarations.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(declarations.get(i));
        }
        sb.append("}, ");
        sb.append("Instrukcje (").append(instructions.size()).append("): {");
        for (int i = 0; i < instructions.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(instructions.get(i).getShortName());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String getShortName() {
        return "block";
    }

    // endregion techniczne

    // region operacje
    @Override
    public void execute() throws MacchiatoException {
        super.execute();
        assert vars != null; // nie powinno się zdarzyć, bo konstruktor tworzy nowe zmienne
        for (Declaration declaration : declarations) // ustala wartości zmiennych
            vars.declare(declaration.getName(), declaration.execute(this));
        for (Instruction instruction : instructions)
            instruction.execute();
    }

    @Override
    public void debugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        super.debugExecute(debugger);
        assert vars != null; // nie powinno się zdarzyć, bo konstruktor tworzy nowe zmienne
        for (Declaration declaration : declarations) // ustala wartości zmiennych
            vars.declare(declaration.getName(), declaration.execute(this));
        debugger.beforeExecute(this);
        for (Instruction instruction : instructions)
            instruction.debugExecute(debugger);
    }
    // endregion operacje
}
