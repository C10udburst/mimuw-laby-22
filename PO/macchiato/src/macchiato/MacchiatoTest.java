package macchiato;

import macchiato.comparators.Equals;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.*;
import macchiato.instructions.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
        public void beforeExecute(Instruction instruction) {
            if (instruction instanceof PrintStdOut print) {
                assertDoesNotThrow(() -> {
                    assertEquals(expected.poll(), instruction.getVariable(print.getVariableName()));
                });
            }
        }
    }

    /**
     * Klasa sprawdzająca, czy każde wypisanie liczby jest liczbą pierwszą
     */
    private record PrimePrintDebugHook() implements DebugHook {

        @Override
        public void beforeExecute(Instruction instruction) {
            if (instruction instanceof PrintStdOut print) {
                assertDoesNotThrow(() -> {
                    assertTrue(isPrime(instruction.getVariable(print.getVariableName())), "Liczba " + instruction.getVariable(print.getVariableName()) + " nie jest liczbą pierwszą");
                });
            }
        }

        /**
         * Metoda sprawdzająca, czy podana liczba jest liczbą pierwszą
         * @param number liczba do sprawdzenia
         * @return true jeśli jest liczbą pierwszą, false w przeciwnym wypadku
         */
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
}