package macchiato.instructions.procedures;

import macchiato.builder.BlockBuilder;
import macchiato.builder.ProcedureBuilder;
import macchiato.builder.ProgramBuilder;
import macchiato.comparators.Equals;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.IllegalArgumentsException;
import macchiato.exceptions.MacchiatoException;
import macchiato.exceptions.MissingArgumentException;
import macchiato.exceptions.UndeclaredProcedureException;
import macchiato.expressions.*;
import macchiato.instructions.*;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ProcedureTest {

    /**
     * Testuje procedury za pomocą rekurencyjnego przejścia DFS (in-order, left-root-right) drzewa binarnego trie składającego się z liczb z cyfr 1 lub 2.
     */
    @Test
    void treeTraverse() throws MacchiatoException {
        int depth = 8;

        LinkedList<Integer> expected = new LinkedList<>();
        calculateDFS(expected, depth, 0);

        Procedure dfs = new Procedure(List.of('d', 'v'), List.of(
                new IfStatement(new Equals(new Variable('d'), new Constant(0)),
                        new PrintStdOut('v'), // jeśli głębokość jest równa 0, to wypisujemy wartość
                        new Block(List.of(), List.of(
                        // jeśli głębokość jest większa od 0, to wywołujemy się rekurencyjnie
                        // testowanie poprzez wyliczanie jako argument
                        new ProcedureInvocation("dfs", Map.of(
                            'd', new Subtract(new Variable('d'), new Constant(1)),
                            'v', new Add(new Multiply(new Variable('v'), new Constant(10)), new Constant(1))
                        )),
                        new PrintStdOut('v'),
                        // testowanie poprzez podmianę obecnych zmiennych
                        new Assignment('v', new Add(new Multiply(new Variable('v'), new Constant(10)), new Constant(2))),
                        new Assignment('d', new Subtract(new Variable('d'), new Constant(1))),
                        new ProcedureInvocation("dfs", Map.of(
                            'd', new Variable('d'),
                            'v', new Variable('v')
                        ))
                ))
        )));

        MainBlock main = new MainBlock(List.of(),
                List.of(
                        new ProcedureInvocation("dfs", Map.of(
                                'd', new Constant(depth),
                                'v', new Constant(0)
                        ))
                ),
                Map.of("dfs", dfs)
        );

        DebugHook debugger = instruction -> {
            if (instruction instanceof PrintStdOut print) {
                assertDoesNotThrow(() -> assertEquals(expected.poll(), print.getVariable(print.getVariableName())));
            }
        };

        main.debugExecute(debugger);

        assertTrue(expected.isEmpty());
    }

    /**
     * Wylicza oczekiwany wynik dla procedury DFS w teście {@link #treeTraverse()}.
     */
    private static void calculateDFS(List<Integer> expected, int depth, int value) {
        if (depth > 0) {
            calculateDFS(expected, depth - 1, value * 10 + 1);
            expected.add(value);
            calculateDFS(expected, depth - 1, value * 10 + 2);
        } else {
            expected.add(value);
        }
    }

    @Test
    void getProcedureTest() throws UndeclaredProcedureException {
        /*
         * Main
         *  |--- pa(a,b)
         *  |--- Block b1
         *         |--- pb(a,b)
         *         |--- Block b2
         *                |--- pc(a,b)
         */

        Procedure pc = new Procedure(List.of('a', 'b'), List.of());
        Block b2 = new Block(List.of(), List.of(), Map.of("pc", pc));
        Procedure pb = new Procedure(List.of('a', 'b'), List.of(b2));
        Block b1 = new Block(List.of(), List.of(), Map.of("pb", pb));
        Procedure pa = new Procedure(List.of('a', 'b'), List.of(b1));
        MainBlock main = new MainBlock(List.of(), List.of(), Map.of("pa", pa));

        assertEquals(pa, main.getProcedure("pa"));
        assertEquals(pa, b1.getProcedure("pa"));
        assertEquals(pa, b2.getProcedure("pa"));

        assertEquals(pb, b1.getProcedure("pb"));
        assertEquals(pb, b2.getProcedure("pb"));
        assertThrows(UndeclaredProcedureException.class, () -> main.getProcedure("pb"));

        assertEquals(pc, b2.getProcedure("pc"));
        assertThrows(UndeclaredProcedureException.class, () -> main.getProcedure("pc"));
        assertThrows(UndeclaredProcedureException.class, () -> b1.getProcedure("pc"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void initTest() {
        assertThrows(Exception.class, () -> {
            Procedure p = new Procedure(List.of(), List.of());
            new Block(List.of(), List.of(), Map.of(
                    "*", p
            ));
        });
        assertThrows(Exception.class, () -> new Block(List.of(), List.of(), Map.of(
                "a", null
        )));
        assertThrows(Exception.class, () -> {
            Procedure p1 = new Procedure(List.of(), List.of());
            Procedure p2 = new Procedure(List.of(), List.of());
            new Block(List.of(), List.of(), Map.of(
                    "a", p1,
                    "a", p2
            ));
        });
    }

    @Test
    void invocationTest() {
        // używamy pustego hooka, bo MainBlock handle'uje wszystkie wyjątki
        DebugHook hook = (Instruction) -> {};

        assertThrows(IllegalArgumentsException.class, () -> {
            Procedure p = new Procedure(List.of(), List.of());
            MainBlock main = new MainBlock(List.of(), List.of(
                    new ProcedureInvocation("a", Map.of('a', new Constant(1)))
            ), Map.of(
                    "a", p
            ));
            main.debugExecute(hook);
        });

        assertThrows(UndeclaredProcedureException.class, () -> {
            MainBlock main = new MainBlock(List.of(), List.of(
                    new ProcedureInvocation("a", Map.of('a', new Constant(1)))
            ), Map.of());
            main.debugExecute(hook);
        });

        assertThrows(MissingArgumentException.class, () -> {
            Procedure p = new Procedure(List.of('a'), List.of());
            MainBlock main = new MainBlock(List.of(), List.of(
                    new ProcedureInvocation("a", Map.of('b', new Constant(1)))
            ), Map.of(
                    "a", p
            ));
            main.debugExecute(hook);
        });
    }

    /**
     * Testuje działanie procedur algorytmem hanoi.
     */
    @Test
    void hanoi() throws MacchiatoException {

        int n = 8;

        LinkedList<Integer> expected = new LinkedList<>();
        calculateHanoi(expected, n, 0, 2);
        MainBlock program = ProgramBuilder.create()
                .declareProcedure("hanoi", List.of('n','f', 't'),
                    ProcedureBuilder.create()
                            .ifThenElse(Equals.of(Variable.named('n'), Constant.of(1)),
                                    BlockBuilder.create()
                                            .declareVariable('p', Add.of(
                                                    Add.of(Constant.of(100),
                                                            Multiply.of(Constant.of(10), Variable.named('f'))
                                                            ),
                                                    Variable.named('t')
                                            ))
                                            .print('p'),
                                    BlockBuilder.create()
                                            .declareVariable('a', Subtract.of(Constant.of(3), Add.of(Variable.named('f'), Variable.named('t'))))
                                            .declareVariable('p', Add.of(
                                                            Multiply.of(Constant.of(100), Variable.named('n')),
                                                            Add.of(
                                                                    Multiply.of(Constant.of(10), Variable.named('f')),
                                                                    Variable.named('t')
                                                            )
                                                    ))
                                            .invoke("hanoi", Map.of(
                                                    'n', Subtract.of(Variable.named('n'), Constant.of(1)),
                                                    'f', Variable.named('f'),
                                                    't', Variable.named('a')
                                            ))
                                            .print('p')
                                            .invoke("hanoi", Map.of(
                                                    'n', Subtract.of(Variable.named('n'), Constant.of(1)),
                                                    'f', Variable.named('a'),
                                                    't', Variable.named('t')
                                            ))
                            )
                )
                .invoke("hanoi", Map.of(
                        'n', Constant.of(n),
                        'f', Constant.of(0),
                        't', Constant.of(2)
                ))
                .build();

        DebugHook hook = (Instruction) -> {
            if (Instruction instanceof PrintStdOut print) {
                assertDoesNotThrow(() -> assertEquals(expected.poll(), print.getVariable(print.getVariableName())));
            }
        };

        program.debugExecute(hook);
        assertTrue(expected.isEmpty());
    }

    /**
     * Wylicza oczekiwany wynik dla procedury Hanoi w teście {@link #hanoi()}.
     * Reprezentacja: 100*ile_klockow + 10*skad + dokad
     */
    private static void calculateHanoi(List<Integer> expected, int n, int from, int to) {
        int tmp = 3 - from - to;
        if (n == 1) {
            expected.add(100 + 10 * from + to);
        } else {
            calculateHanoi(expected, n - 1, from, tmp);
            expected.add(n * 100 + from * 10 + to);
            calculateHanoi(expected, n - 1, tmp, to);
        }
    }
}
