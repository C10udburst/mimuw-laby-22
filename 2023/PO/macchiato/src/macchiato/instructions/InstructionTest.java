package macchiato.instructions;

import macchiato.Declaration;
import macchiato.exceptions.MacchiatoException;
import macchiato.exceptions.UndeclaredVariableException;
import macchiato.expressions.Add;
import macchiato.expressions.Constant;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test implementacji instrukcji.
 * Zadeklarowany w pakiecie macchiato.instructions, ponieważ aby sprawdzić poprawność działania klasy Instruction, musimy odwołać się do jego wewnętrznych struktur.
 */
class InstructionTest {

    /** Testy funkcji {@link Instruction#dumpVars()} */
    @Test
    void dumpVars() {
        List<Declaration> declarations = List.of(
                new Declaration('a', new Constant(1)),
                new Declaration('b', new Add(new Constant(2), new Constant(3)))
        );
        Block innerBlock = new Block(Collections.emptyList(), Collections.emptyList());
        MainBlock mainBlock = new MainBlock(declarations, List.of(innerBlock));
        mainBlock.execute();
        assertEquals("a: 1, b: 5", innerBlock.dumpVars());
    }

    /** Testy sprawdzające poprawność działania zmiennych. */
    @Test
    void variableScopes() throws MacchiatoException {
        List<Declaration> declarations = List.of(
                new Declaration('a', new Constant(1)),
                new Declaration('b', new Add(new Constant(2), new Constant(3)))
        );
        Block innerBlock = new Block(Collections.emptyList(), Collections.emptyList());
        MainBlock mainBlock = new MainBlock(declarations, List.of(innerBlock));
        mainBlock.execute();
        assertEquals(1, innerBlock.getVariable('a'));
        assertEquals(5, innerBlock.getVariable('b'));
        assertThrows(UndeclaredVariableException.class, () -> {
            assert innerBlock.vars != null;
            innerBlock.vars.get('a');
        });
        assertThrows(UndeclaredVariableException.class, () -> innerBlock.getVariable('c'));
        assertThrows(UndeclaredVariableException.class, () -> innerBlock.setVariable('c', 1));
        innerBlock.setVariable('a', 2);
        assertEquals(2, innerBlock.getVariable('a'));
        assertEquals(2, mainBlock.getVariable('a'));
    }

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
}