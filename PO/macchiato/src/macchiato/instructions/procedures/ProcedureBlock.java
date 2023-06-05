package macchiato.instructions.procedures;

import macchiato.Variables;
import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.Block;
import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProcedureBlock extends Block {

    private Variables args;

    /**
     * Należy używać tylko w {@link Procedure} oraz twożyc jedynie za pomocą {@link macchiato.builder.ProcedureBuilder}.
     */
    public ProcedureBlock(@NotNull List<Instruction> instructions) {
        super(List.of(), instructions);
    }

    @Override
    public String getShortName() {
        return "procedure";
    }

    protected void setParent(Instruction parent) {
        this.parent = parent;
    }

    protected void setArgs(@NotNull Variables args) {
        this.args = args;
    }

    @Override
    protected void declareVariables() throws MacchiatoException {
        super.declareVariables();
        // zamień zmienne na argumenty
        vars.pop();
        vars.push(args);
    }
}
