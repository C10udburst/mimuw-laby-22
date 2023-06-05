package macchiato.instructions.procedures;


import macchiato.Variables;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class Procedure {

    @NotNull
    private final List<Character> arguments;

    @NotNull
    protected final ProcedureBlock block;


    public Procedure(@NotNull List<Character> arguments, @NotNull ProcedureBlock block) {
        this.block = block;
        this.arguments = arguments;
    }

    public Procedure(@NotNull List<Character> arguments, @NotNull List<Instruction> instructions) {
        this(arguments, new ProcedureBlock(instructions));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("proc(");
        for (char argument : arguments) {
            sb.append(argument).append(", ");
        }
        if (!arguments.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(")");
        return sb.toString();
    }

    public void setParent(Instruction parent) {
        block.setParent(parent);
    }

    /**
     * Wykonuje procedurę.
     *
     * @param invoker instrukcja wywołująca procedurę
     * @throws MacchiatoException jeśli wystąpi błąd
     */
    public void execute(ProcedureInvocation invoker) throws MacchiatoException {
        block.push(makeVars(invoker));
        block.execute();
        block.pop();
    }

    /**
     * Wykonuje procedurę w trybie debugowania.
     *
     * @param invoker  instrukcja wywołująca procedurę
     * @param debugger obiekt debugujący
     * @throws MacchiatoException jeśli wystąpi błąd
     */
    public void debugExecute(ProcedureInvocation invoker, DebugHook debugger) throws MacchiatoException {
        block.push(makeVars(invoker));
        block.debugExecute(debugger);
        block.pop();
    }

    /**
     * Wylicza argumenty procedury.
     *
     * @param invoker instrukcja wywołująca procedurę
     * @return zmienne z argumentami
     * @throws MacchiatoException jeśli wystąpi błąd
     */
    private Variables makeVars(ProcedureInvocation invoker) throws MacchiatoException {
        invoker.checkArguments(arguments.size());
        Variables vars = new Variables(block);
        for (char argument : arguments) {
            vars.declare(argument, invoker.evaluateArgument(argument));
        }
        return vars;
    }

    public Iterator<Character> getArguments() {
        return arguments.iterator();
    }
}
