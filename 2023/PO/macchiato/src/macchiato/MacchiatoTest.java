package macchiato;

import macchiato.comparators.Equals;
import macchiato.comparators.LessEqual;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.*;
import macchiato.instructions.*;
import macchiato.instructions.procedures.Procedure;
import macchiato.instructions.procedures.ProcedureInvocation;
import macchiato.parser.Parser;
import macchiato.parser.ParserException;
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
        MainBlock mainBlock = new MainBlock(
                List.of(
                        new Declaration('n', new Constant(n)) // liczba liczb do sprawdzenia
                ),
                List.of(
                        // for: k = 0..(n 1 -)
                        new ForLoop('k', new Subtract(new Variable('n'), new Constant(1)),
                                new Block(
                                        List.of(
                                                new Declaration('p', new Constant(1)), //
                                                new Declaration('k', new Add(new Variable('k'), new Constant(2)))
                                        ),
                                        List.of(
                                                new ForLoop('i', new Subtract(new Variable('k'), new Constant(2)),
                                                        new Block(List.of(
                                                                new Declaration('i', new Add(new Variable('i'), new Constant(2)))
                                                        ), List.of(
                                                                new IfStatement(
                                                                        new Equals(new Modulo(new Variable('k'), new Variable('i')), new Constant(0)),
                                                                        new Assignment('p', new Constant(0)),
                                                                        null
                                                                )
                                                        ))
                                                ),
                                                new IfStatement(new Equals(new Variable('p'), new Constant(1)),
                                                        new PrintStdOut('k'),
                                                        null
                                                )
                                        )
                                )
                        )
                )
        );
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
    void moduloFactorial() throws MacchiatoException, ParserException {
        int max = 56;
        int modulo = Integer.MAX_VALUE - 10;
        LinkedList<Integer> intermediateResults = new LinkedList<>();
        int expected = factorial(max, modulo, intermediateResults);
        DebugHook debugHook = new PrintDebugHook(intermediateResults);
        String src = String.format("""
                n = (%d)
                m = (%d)
                f = (1)
                do
                    for: i = 0..(n)
                        block
                        do {
                            set: f = (f m %% i 1 + * m %%)   :# f = ((f mod m)*(i+1)) mod m
                            print: f
                        }
                """, max, modulo).stripIndent().trim();
        Parser parser = new Parser(src);
        MainBlock mainBlock = parser.parse();
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
        Procedure tailrec = new Procedure(List.of('i', 'f'), List.of(
                new Assignment('f', new Modulo(new Multiply(new Modulo(new Variable('f'), new Constant(modulo)), new Variable('i')), new Constant(modulo))),
                new Assignment('i', new Add(new Variable('i'), new Constant(1))),
                new PrintStdOut('f'),
                new IfStatement(new LessEqual(new Variable('i'), new Constant(max)),
                        new ProcedureInvocation("tailrec", Map.of(
                                'i', new Variable('i'),
                                'f', new Variable('f')
                        )),
                        null
                )
        ));
        MainBlock mainBlock = new MainBlock(List.of(), List.of(
                new ProcedureInvocation("tailrec", Map.of(
                        'i', new Constant(1),
                        'f', new Constant(1)
                ))
            ),
            Map.of(
                    "tailrec", tailrec
            )
        );
        mainBlock.debugExecute(debugHook);
    }

}