package macchiato.builder;

import macchiato.instructions.Block;

public class BlockBuilder extends AbstractBlockBuilder<Block, BlockBuilder> {
    @Override
    public Block build() {
        return new Block(declarations, instructions, procedures);
    }

    public static BlockBuilder create() {
        return new BlockBuilder();
    }
}
