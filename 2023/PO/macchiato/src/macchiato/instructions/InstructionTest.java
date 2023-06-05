package macchiato.instructions;

import macchiato.builder.BlockBuilder;
import macchiato.builder.ProgramBuilder;
import macchiato.comparators.Equals;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Add;
import macchiato.expressions.Constant;
import macchiato.expressions.Variable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test implementacji instrukcji.
 * Zadeklarowany w pakiecie macchiato.instructions, ponieważ aby sprawdzić poprawność działania klasy Instruction, musimy odwołać się do jego wewnętrznych struktur.
 */
class InstructionTest {

    /** Testy funkcji {@link Instruction#getParent(int)} )} */
    @Test
    void getParent() {
        ArrayList<Block> blocks = new ArrayList<>();
        blocks.add(new Block(Collections.emptyList(), Collections.emptyList()));
        for (int i = 0; i < 5; i++) {
            blocks.add(new Block(Collections.emptyList(), List.of(blocks.get(blocks.size() - 1))));
        }
        // mainBlock { block[5] { block[4] { block[3] { block[2] { block[1] { block[0] } } } } } }
        MainBlock mainBlock = new MainBlock(Collections.emptyList(), List.of(blocks.get(blocks.size() - 1)));
        for (int i = 0; i <= 5; i++) {
            assertEquals(blocks.get(i), blocks.get(0).getParent(i));
        }
        assertEquals(mainBlock, blocks.get(0).getParent(6));
        assertNull(blocks.get(0).getParent(7));
    }

    @Test
    void assignment() throws MacchiatoException {
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 1)
                .assign('a', Add.of(Variable.named('a'), Variable.named('a')))
                .build();
        prog.debugExecute(i -> {
        });
        assertEquals(2, prog.getVariable('a'));
    }

    @Test
    void forLoop() throws MacchiatoException {
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 1)
                .forLoop('i', Constant.of(10), BlockBuilder.create()
                        .assign('a', Add.of(Variable.named('a'), Variable.named('i')))
                )
                .build();
        prog.debugExecute(i -> {
        });
        assertEquals(46, prog.getVariable('a'));
    }

    @Test
    void ifStatement() throws MacchiatoException {
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 1)
                .ifThenElse(Equals.of(Variable.named('a'), Constant.of(1)), BlockBuilder.create()
                                .assign('a', Constant.of(2)),
                        BlockBuilder.create()
                                .assign('a', Constant.of(3))
                )
                .build();
        prog.debugExecute(i -> {});
        assertEquals(2, prog.getVariable('a'));
    }
}