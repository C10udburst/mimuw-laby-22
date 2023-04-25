package macchiato.instructions;

import macchiato.Debugger;
import macchiato.Declaration;
import macchiato.Variables;
import macchiato.exceptions.MacchiatoException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Block extends Instruction {
    // region dane
    private final List<Declaration> declarations;
    private final List<Instruction> instructions;
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

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Block {\n");
        sb.append("Declarations (").append(declarations.size()).append("):\n");
        for (Declaration declaration : declarations)
            sb.append(declaration).append("\n");
        sb.append("Instructions: ").append(instructions.size()).append("\n");
        sb.append("Variables:\n").append(dumpVars());
        sb.append("\n}");
        return sb.toString();
    }
    // endregion techniczne

    // region operacje
    @Override
    public void execute() throws MacchiatoException {
        assert vars != null; // nie powinno się zdarzyć, bo konstruktor tworzy nowe zmienne
        for (Declaration declaration : declarations)
            vars.declare(declaration.getName(), declaration.execute(this));
        for (Instruction instruction : instructions)
            instruction.execute();
    }

    @Override
    public void debugExecute(@NotNull Debugger debugger) throws MacchiatoException {
        assert vars != null; // nie powinno się zdarzyć, bo konstruktor tworzy nowe zmienne
        for (Declaration declaration : declarations)
            vars.declare(declaration.getName(), declaration.execute(this));
        debugger.beforeExecute(this);
        for (Instruction instruction : instructions)
            instruction.debugExecute(debugger);
    }
    // endregion operacje
}
