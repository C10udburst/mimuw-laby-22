package macchiato;

import macchiato.builder.BlockBuilder;
import macchiato.builder.ProgramBuilder;
import macchiato.comparators.Equals;
import macchiato.comparators.LessEqual;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.*;
import macchiato.instructions.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test implementacji języka Macchiato. Testujemy pełne programy, a nie pojedyncze instrukcje.
 * Nie testujemy parsowania.
 */
class MacchiatoTest {

    /**
     * Klasa używana do testowania instrukcji {@link PrintStdOut}
     */
    private record PrintDebugHook(LinkedList<Integer> expected) implements DebugHook {
        @Override
        public void beforeExecute(@NotNull Instruction instruction) {
            if (instruction instanceof PrintStdOut print) {
                assertDoesNotThrow(() -> assertEquals(expected.poll(), instruction.getVariable(print.getVariableName())));
            }
        }
    }

    /**
     * Klasa sprawdzająca, czy każde wypisanie liczby jest liczbą pierwszą
     */
    private record PrimePrintDebugHook() implements DebugHook {

        @Override
        public void beforeExecute(@NotNull Instruction instruction) {
            if (instruction instanceof PrintStdOut print) {
                assertDoesNotThrow(() -> assertTrue(isPrime(instruction.getVariable(print.getVariableName())), "Liczba " + instruction.getVariable(print.getVariableName()) + " nie jest liczbą pierwszą"));
            }
        }

        private boolean isPrime(int number) {
            if (number <= 1) {
                return false;
            }
            for (int i = 2; i < number; i++) {
                if (number % i == 0)
                    return false;
            }
            return true;
        }
    }

    /**
     * Test z przykładowego pseudokodu z polecenia
     */
    @Test
    void primeTest() throws MacchiatoException {
        int n = 120;
        /*
            n = (30)
            do
                for: k = 0..(n 1 -)
                    block
                        p = (1)
                        k = (k 2 +)
                    do {
                        for: i = 0..(k 2 -)
                            block
                                i = (i 2 +)
                            do {
                                if: (k i %) = (0)
                                    set: p = (0)
                            }
                        if: (p) = (1)
                            print: k
                    }
         */
        MainBlock mainBlock = ProgramBuilder.create()
                .declareVariable('n', n)
                .forLoop('k', Subtract.of(Variable.named('n'), Constant.of(1)), BlockBuilder.create()
                        .declareVariable('p',1)
                        .declareVariable('k', Add.of(Variable.named('k'), Constant.of(2)))
                        .forLoop('i', Subtract.of(Variable.named('k'), Constant.of(2)), BlockBuilder.create()
                                .declareVariable('i', Add.of(Variable.named('i'), Constant.of(2)))
                                .ifThen(Equals.of(Modulo.of(Variable.named('k'), Variable.named('i')), Constant.of(0)), BlockBuilder.create()
                                        .assign('p', Constant.of(0))
                                )
                        )
                )
                .build();
        DebugHook debugHook = new PrimePrintDebugHook();
        mainBlock.debugExecute(debugHook);
    }

    /**
     * Test sprawdzający poprawność wyliczania liczb Fibonacciego
     */
    @Test
    void fibonacci() throws MacchiatoException {
        int count = 40;
        LinkedList<Integer> expected = new LinkedList<>(Arrays.asList(1, 1));
        for (int i = 2; i < count; i++) {
            expected.add(expected.get(i - 1) + expected.get(i - 2));
        }
        MainBlock mainBlock = new MainBlock(
                List.of(
                        new Declaration('a', new Constant(0)),
                        new Declaration('b', new Constant(1))
                ),
                List.of(
                        new ForLoop('i', new Constant(count),
                                new Block(
                                        List.of(
                                                new Declaration('t', new Variable('a'))
                                        ),
                                        List.of(
                                                new Assignment('t', new Variable('a')),
                                                new Assignment('a', new Variable('b')),
                                                new Assignment('b', new Add(new Variable('t'), new Variable('b'))),
                                                new PrintStdOut('a')
                                        )
                                )
                        )

                )
        );
        DebugHook debugHook = new PrintDebugHook(expected);
        mainBlock.debugExecute(debugHook);
    }

    /**
     * Test sprawdzający poprawność wyliczania silni
     */
    @Test
    void moduloFactorial() throws MacchiatoException {
        int max = 56;
        int modulo = Integer.MAX_VALUE - 10;
        LinkedList<Integer> intermediateResults = new LinkedList<>();
        int expected = factorial(max, modulo, intermediateResults);
        DebugHook debugHook = new PrintDebugHook(intermediateResults);

        MainBlock mainBlock = ProgramBuilder.create()
                .declareVariable('n', max)
                .declareVariable('m', modulo)
                .declareVariable('f', 1)
                .forLoop('i', Variable.named('n'), BlockBuilder.create()
                        .assign('f', Modulo.of(Multiply.of(Modulo.of(Variable.named('f'), Variable.named('m')), Add.of(Variable.named('i'), Constant.of(1))), Variable.named('m')))
                        .print('f')
                )
                .build();

        mainBlock.debugExecute(debugHook);
        assertEquals(expected, mainBlock.getVariable('f'));
    }

    private static int factorial(int n, int modulo, List<Integer> intermediateResults) {
        int result = 1;
        for (int i = 1; i <= n; i++) {
            result = (result * i) % modulo;
            intermediateResults.add(result);
        }
        return result;
    }

    /**
     * Test sprawdzający poprawność wyliczania silni procedurami (tail recursion)
     */
    @Test
    void tailRec() throws MacchiatoException {
        int max = 100;
        int modulo = Integer.MAX_VALUE - 10;
        LinkedList<Integer> intermediateResults = new LinkedList<>();
        factorial(max, modulo, intermediateResults);
        DebugHook debugHook = new PrintDebugHook(intermediateResults);

        MainBlock mainBlock = ProgramBuilder.create()
                .declareProcedure("tailrec", List.of('i', 'f'), BlockBuilder.create()
                        .assign('f', Modulo.of(Multiply.of(Modulo.of(Variable.named('f'), Constant.of(modulo)), Variable.named('i')), Constant.of(modulo)))
                        .assign('i', Add.of(Variable.named('i'), Constant.of(1)))
                        .print('f')
                        .ifThen(LessEqual.of(Variable.named('i'), Constant.of(max)), BlockBuilder.create()
                                .invoke("tailrec", Map.of(
                                        'i', Variable.named('i'),
                                        'f', Variable.named('f')
                                ))
                        )
                )
                .build();

        mainBlock.debugExecute(debugHook);


    }

}