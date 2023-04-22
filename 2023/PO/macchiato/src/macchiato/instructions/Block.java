package macchiato.instructions;

import macchiato.Debugger;
import macchiato.Declaration;
import macchiato.Variables;
import macchiato.exceptions.MacchiatoException;

import java.util.ArrayList;
import java.util.List;

public class Block extends Instruction {
    // region dane

    private final List<Declaration> declarations;
    private final List<Instruction> instructions;

    // endregion dane

    public Block(List<Declaration> declarations, List<Instruction> instructions) {
        super(new Variables());

        this.declarations = declarations;
        this.instructions = instructions;

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

    @Override
    public void execute() throws MacchiatoException {
        for (Declaration declaration : declarations)
            vars.declare(declaration.getName(), declaration.execute(this));
        for (Instruction instruction : instructions)
            instruction.execute();
    }

    @Override
    public void debugExecute(Debugger debugger) throws MacchiatoException {
        debugger.beforeExecute(this);
        for (Declaration declaration : declarations) // TODO: czy deklaracja ma być debugowalna?
            vars.declare(declaration.getName(), declaration.execute(this));
        for (Instruction instruction : instructions)
            instruction.debugExecute(debugger);
    }
}
