package macchiato.builder;

import macchiato.instructions.MainBlock;

public class ProgramBuilder extends AbstractBlockBuilder<MainBlock, ProgramBuilder> {

    public MainBlock build() {
        return new MainBlock(declarations, instructions, procedures);
    }

    public static ProgramBuilder create() {
        return new ProgramBuilder();
    }
}
