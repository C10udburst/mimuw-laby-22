package macchiato.instructions.procedures;

import macchiato.Variables;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.Block;
import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Stack;

public class ProcedureBlock extends Block {

    @NotNull
    protected final Stack<Variables> varStack = new Stack<>();

    public ProcedureBlock(@NotNull List<Instruction> instructions) {
        super(List.of(), instructions);
    }

    @Override
    public String getShortName() {
        return "procedure";
    }

    @Override
    public void execute() throws MacchiatoException {
        push(null);  // nie chcemy, aby blok wyczyścił zmienne
        super.execute();
    }

    @Override
    public void debugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        push(null);  // nie chcemy, aby blok wyczyścił zmienne
        super.debugExecute(debugger);
    }

    @Override
    protected void declareVariables()  {
        pop();
    }

    /**
     * Wrzuca na stos aktualny stan zmiennych i ustawia nowy.
     * @param next nowy stan zmiennych
     */
    protected void push(Variables next) {
        varStack.push(vars);
        vars = next;
    }

    /**
     * Zdejmuje ze stosu aktualny stan zmiennych i ustawia go.
     */
    protected void pop() {
        vars = varStack.pop();
    }

    protected void setParent(Instruction parent) {
        this.parent = parent;
    }

}
