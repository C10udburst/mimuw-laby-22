package macchiato.instructions;

import macchiato.Declaration;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import macchiato.exceptions.UndeclaredProcedureException;
import macchiato.instructions.procedures.Procedure;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Block extends Instruction {
    // region dane
    @NotNull private final List<Declaration> declarations;
    @NotNull private final List<Instruction> instructions;
    @NotNull private final Map<String, Procedure> procedures;
    // endregion dane

    // region techniczne

    /**
     * Tworzy blok instrukcji. Należy użyć {@link macchiato.builder.BlockBuilder} zamiast konstruktora.
     *
     * @param declarations deklaracje zmiennych, które mają być zadeklarowane w tym bloku przed wykonaniem instrukcji
     * @param instructions instrukcje do wykonania
     */
    public Block(@NotNull List<Declaration> declarations, @NotNull List<Instruction> instructions, @NotNull Map<String, Procedure> procedures) {
        super();

        this.declarations = declarations;
        this.instructions = instructions;
        this.procedures = procedures;
        for (Map.Entry<String, Procedure> entry : procedures.entrySet()) {
            if (!entry.getKey().matches("^[a-z]+$")) {
                throw new IllegalArgumentException("Invalid procedure name: " + entry.getKey());
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("Procedure " + entry.getKey() + " is null");
            }
        }

        // ustawia ten blok jako nadrzędny dla wszystkich instrukcji
        for (Instruction instruction : instructions)
            instruction.parent = this;
    }

    public Block(@NotNull List<Declaration> declarations, @NotNull List<Instruction> instructions) {
        this(declarations, instructions, Map.of());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getShortName());
        sb.append(":");
        if (declarations.size() > 0) {
            sb.append("\n\tDeclarations (").append(declarations.size()).append("): {");
            for (int i = 0; i < declarations.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(declarations.get(i));
            }
            sb.append("}, ");
        }
        if (instructions.size() > 0) {
            sb.append("\n\tInstructions (").append(instructions.size()).append("): {");
            for (int i = 0; i < instructions.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(instructions.get(i).getShortName());
            }
            sb.append("}");
        }
        if (procedures.size() > 0) {
            sb.append(", ");
            sb.append("\n\tProcedures (").append(procedures.size()).append("): {");
            for (Map.Entry<String, Procedure> entry : procedures.entrySet()) {
                sb.append(entry.getKey()).append(' ').append(entry.getValue()).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());  // procedures.size() > 0, więc nie ma problemu z out of bounds
            sb.append("}");
        }
        return sb.toString();
    }

    @Override
    public String getShortName() {
        return "block";
    }

    // endregion techniczne

    // region operacje
    @Override
    protected void internalExecute() throws MacchiatoException {
        declareVariables();
        for (Instruction instruction : instructions)
            instruction.execute();
    }

    @Override
    protected void internalDebugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        declareVariables();
        debugger.beforeExecute(this);
        for (Instruction instruction : instructions)
            instruction.debugExecute(debugger);
    }

    /**
     * Deklaruje wartości zmiennych w tym bloku.
     * @throws MacchiatoException jeśli wystąpi błąd podczas deklaracji
     */
    protected void declareVariables() throws MacchiatoException {
        for (Declaration declaration : declarations) // ustala wartości zmiennych
            vars.peek().declare(declaration.getName(), declaration.execute(this));
    }

    @Override
    public @NotNull Procedure getProcedure(String name) throws UndeclaredProcedureException {
        Procedure procedure = procedures.get(name);
        if (procedure != null) return procedure;
        return super.getProcedure(name);
    }

    @Override
    public @NotNull HashSet<String> declaredProcedures() {
        HashSet<String> declared = super.declaredProcedures();
        declared.addAll(procedures.keySet());
        return declared;
    }

    // endregion operacje
}
