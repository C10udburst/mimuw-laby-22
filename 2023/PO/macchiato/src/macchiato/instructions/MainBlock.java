package macchiato.instructions;

import macchiato.Declaration;
import macchiato.exceptions.MacchiatoException;

import java.util.List;

public class MainBlock extends Block {

    public MainBlock(List<Declaration> declarations, List<Instruction> instructions) {
        super(declarations, instructions);
    }

    @Override
    public void execute() throws MacchiatoException {
        super.execute();
        System.out.println(dumpVars());
    }
}
