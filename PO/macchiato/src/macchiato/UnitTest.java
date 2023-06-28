package macchiato;

import macchiato.builder.BlockBuilder;
import macchiato.builder.ProcedureBuilder;
import macchiato.builder.ProgramBuilder;
import macchiato.comparators.*;
import macchiato.exceptions.*;
import macchiato.expressions.*;
import macchiato.instructions.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy pojedynczych instrukcji, wyrażeń, porównań, deklaracji itp.
 * Nie są one w stanie przetestować poprawności działania programu, bo testują tylko pojedyncze instrukcje.
 * Używamy pustej lambdy jako debugger, aby MainBlock nie obsługiwał błędów.
 * Testy procedur znajdują się w {@link macchiato.instructions.procedures.ProcedureTest}.
 */
public class UnitTest {

    // region macchiato.instructions

    @Test
    void assignment() throws MacchiatoException { // deklaracja zmiennej i przypisanie
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 1)
                .assign('a', Add.of(Variable.named('a'), Variable.named('a')))
                .build();
        prog.debugExecute(i -> {
        });
        assertEquals(2, prog.getVariable('a'));
    }

    @Test
    void block() throws MacchiatoException { // deklaracja zmiennej i blok
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 1)
                .add(BlockBuilder.create()
                        .declareVariable('a', 2)
                        .print('a')
                )
                .build();
        prog.debugExecute(i -> {
            if (i instanceof PrintStdOut print) {
                assertDoesNotThrow(() -> assertEquals(2, print.getVariable('a')));
            }
        });
    }

    @Test
    void forLoop() throws MacchiatoException { // deklaracja zmiennej i pętla for
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
    void ifStatement() throws MacchiatoException { // instrukcja warunkowa
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

    @Test
    void printStdOut() throws MacchiatoException { // instrukcja wypisania na standardowe wyjście
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 1)
                .print('a')
                .build();
        prog.debugExecute(i -> {});
        /* ignorujemy błędy związane z różnicą CRLF i LF */
        assertEquals("1\na: 1\n", out.toString().replaceAll("\r\n", "\n"));
    }

    // endregion

    // region macchiato.expressions
    @Test
    void constant() throws MacchiatoException { // stała
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 1)
                .assign('a', Constant.of(2))
                .build();
        prog.debugExecute(i -> {
        });
        assertEquals(2, prog.getVariable('a'));
    }

    @Test
    void variable() throws MacchiatoException { // zmienna
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 1)
                .declareVariable('b', 2)
                .assign('a', Variable.named('b'))
                .build();
        prog.debugExecute(i -> {
        });
        assertEquals(2, prog.getVariable('a'));
    }

    @Test
    void add() throws MacchiatoException { // dodawanie
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .assign('a', Add.of(Variable.named('a'), Variable.named('b')))
                .build();
        prog.debugExecute(i -> {
        });
        assertEquals(13+11, prog.getVariable('a'));
    }

    @Test
    void divide() throws MacchiatoException { // dzielenie
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .assign('a', Divide.of(Variable.named('a'), Variable.named('b')))
                .build();
        prog.debugExecute(i -> {
        });
        assertEquals(13/11, prog.getVariable('a'));
    }

    @Test
    void modulo() throws MacchiatoException { // modulo
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .assign('a', Modulo.of(Variable.named('a'), Variable.named('b')))
                .build();
        prog.debugExecute(i -> {
        });
        assertEquals(13%11, prog.getVariable('a'));
    }

    @Test
    void multiply() throws MacchiatoException { // mnożenie
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .assign('a', Multiply.of(Variable.named('a'), Variable.named('b')))
                .build();
        prog.debugExecute(i -> {
        });
        assertEquals(13*11, prog.getVariable('a'));
    }

    @Test
    void subtract() throws MacchiatoException { // odejmowanie
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .assign('a', Subtract.of(Variable.named('a'), Variable.named('b')))
                .build();
        prog.debugExecute(i -> {
        });
        assertEquals(13-11, prog.getVariable('a'));
    }

    // endregion

    // region macchiato.comparators
    @Test
    void equals() throws MacchiatoException { // porównanie ==
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .declareVariable('c', -1)
                .ifThenElse(Equals.of(Variable.named('a'), Variable.named('b')), BlockBuilder.create()
                                .assign('c', Constant.of(1)),
                        BlockBuilder.create()
                                .assign('c', Constant.of(0))
                )
                .build();
        prog.debugExecute(i -> {});
        assertEquals((13==11)?1:0, prog.getVariable('c'));
    }

    @Test
    void notEquals() throws MacchiatoException { // porównanie !=
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .declareVariable('c', -1)
                .ifThenElse(NotEquals.of(Variable.named('a'), Variable.named('b')), BlockBuilder.create()
                                .assign('c', Constant.of(1)),
                        BlockBuilder.create()
                                .assign('c', Constant.of(0))
                )
                .build();
        prog.debugExecute(i -> {});
        assertEquals((13!=11)?1:0, prog.getVariable('c'));
    }

    @Test
    void greaterEqual() throws MacchiatoException { // porównanie >=
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .declareVariable('c', -1)
                .ifThenElse(GreaterEqual.of(Variable.named('a'), Variable.named('b')), BlockBuilder.create()
                                .assign('c', Constant.of(1)),
                        BlockBuilder.create()
                                .assign('c', Constant.of(0))
                )
                .build();
        prog.debugExecute(i -> {});
        assertEquals((13>=11)?1:0, prog.getVariable('c'));
    }

    @Test
    void greaterThan() throws MacchiatoException { // porównanie >
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .declareVariable('c', -1)
                .ifThenElse(GreaterThan.of(Variable.named('a'), Variable.named('b')), BlockBuilder.create()
                                .assign('c', Constant.of(1)),
                        BlockBuilder.create()
                                .assign('c', Constant.of(0))
                )
                .build();
        prog.debugExecute(i -> {});
        assertEquals((13>11)?1:0, prog.getVariable('c'));
    }

    @Test
    void lessEqual() throws MacchiatoException { // porównanie <=
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .declareVariable('c', -1)
                .ifThenElse(LessEqual.of(Variable.named('a'), Variable.named('b')), BlockBuilder.create()
                                .assign('c', Constant.of(1)),
                        BlockBuilder.create()
                                .assign('c', Constant.of(0))
                )
                .build();
        prog.debugExecute(i -> {});
        assertEquals((13<=11)?1:0, prog.getVariable('c'));
    }

    @Test
    void lessThan() throws MacchiatoException { // porównanie <
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 11)
                .declareVariable('c', -1)
                .ifThenElse(LessThan.of(Variable.named('a'), Variable.named('b')), BlockBuilder.create()
                                .assign('c', Constant.of(1)),
                        BlockBuilder.create()
                                .assign('c', Constant.of(0))
                )
                .build();
        prog.debugExecute(i -> {});
        assertEquals((13<11)?1:0, prog.getVariable('c'));
    }

    // endregion

    // region macchiato.exceptions
    @Test
    void divideByZero() { // DivideByZeroException
        MainBlock prog = ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('b', 0)
                .assign('a', Divide.of(Variable.named('a'), Variable.named('b')))
                .build();
        assertThrows(DivideByZeroException.class, () -> prog.debugExecute(i -> {}));
    }

    @Test
    void illegalArguments() { // IllegalArgumentsException
        MainBlock prog = ProgramBuilder.create()
                .declareProcedure("test", List.of('a'), ProcedureBuilder.create()
                )
                .invoke("test", Map.of('a', Constant.of(1), 'b', Constant.of(2)))
                .build();
        assertThrows(IllegalArgumentsException.class, () -> prog.debugExecute(i -> {}));
    }

    @Test
    void missingArgument() { // MissingArgumentException
        MainBlock prog = ProgramBuilder.create()
                .declareProcedure("test", List.of(), ProcedureBuilder.create()
                )
                .invoke("test", Map.of('a', Constant.of(1)))
                .build();
        assertThrows(IllegalArgumentsException.class, () -> prog.debugExecute(i -> {}));
    }

    @Test
    void undeclaredProcedure() { // UndeclaredProcedureException
        MainBlock prog = ProgramBuilder.create()
                .invoke("test", Map.of('a', Constant.of(1)))
                .build();
        assertThrows(UndeclaredProcedureException.class, () -> prog.debugExecute(i -> {}));
    }

    @Test
    void undeclaredVariable() { // UndeclaredVariableException
        MainBlock prog = ProgramBuilder.create()
                .assign('a', Constant.of(1))
                .build();
        assertThrows(UndeclaredVariableException.class, () -> prog.debugExecute(i -> {}));
    }

    @Test
    void variableRedeclaration() { // VariableRedeclarationException
        assertThrows(VariableRedeclarationException.class, () -> ProgramBuilder.create()
                .declareVariable('a', 13)
                .declareVariable('a', 11)
                .build()
                .debugExecute(i -> {}));
    }

    // endregion
}
