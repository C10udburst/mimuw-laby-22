package macchiato.builder;

import macchiato.Declaration;
import macchiato.instructions.procedures.Procedure;
import macchiato.instructions.procedures.ProcedureBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ProcedureBuilder extends AbstractBlockBuilder<ProcedureBlock, ProcedureBuilder> {

    @Override
    public ProcedureBlock build() {
        return new ProcedureBlock(instructions);
    }

    @Override
    public ProcedureBuilder declareVariable(@NotNull Declaration declaration) {
        throw new UnsupportedOperationException("Cannot declare variables in a procedure");
    }

    @Override
    public ProcedureBuilder declareProcedure(@NotNull String name, @NotNull Procedure procedure) {
        throw new UnsupportedOperationException("Cannot declare procedures in a procedure");
    }

    public Procedure buildProcedure(List<Character> arguments) {
        return new Procedure(arguments, build());
    }

    public Procedure buildProcedure(Character... arguments) {
        return buildProcedure(Arrays.asList(arguments));
    }

    public static ProcedureBuilder create() {
        return new ProcedureBuilder();
    }
}
