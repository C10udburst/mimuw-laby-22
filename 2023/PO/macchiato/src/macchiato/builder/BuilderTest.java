package macchiato.builder;

import macchiato.comparators.Equals;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.MacchiatoException;
import macchiato.exceptions.UndeclaredProcedureException;
import macchiato.expressions.*;
import macchiato.instructions.*;
import macchiato.instructions.procedures.ProcedureBlock;
import macchiato.instructions.procedures.ProcedureInvocation;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BuilderTest {

    @Test
    void t1() throws MacchiatoException {
        MainBlock mainBlock = new ProgramBuilder()
                .declareVariable('a', Constant.of(2))
                .declareVariable('b', Add.of(Constant.of(1), Variable.named('a')))
                .forLoop('i', Constant.of(3),
                        new BlockBuilder()
                                .assign('a', Multiply.of(Variable.named('a'), Variable.named('b')))
                )
                .print('a')
                .add(() -> new PrintStdOut('a'))
                .build();
        LinkedList<Class<? extends Instruction>> expected = new LinkedList<>(List.of(
                MainBlock.class,
                ForLoop.class,
                Block.class,
                Assignment.class,
                Block.class,
                Assignment.class,
                Block.class,
                Assignment.class,
                PrintStdOut.class,
                PrintStdOut.class
        ));
        DebugHook hook = instruction -> assertInstanceOf(expected.removeFirst(), instruction);
        mainBlock.debugExecute(hook);
        assertTrue(expected.isEmpty());
        assertEquals(3, mainBlock.getVariable('b'));
        assertEquals(2 * 3 * 3 * 3, mainBlock.getVariable('a'));
    }

    @Test
    void t2() throws MacchiatoException { // Test z polecenia przed 10 VI 2023.
        var program = ProgramBuilder.create()
                .declareVariable('x', Constant.of(57))
                .declareVariable('y', Constant.of(15))
                .declareProcedure("out", List.of('a'),
                        ProcedureBuilder.create()
                                .print('a')
                )
                .assign('x', Subtract.of(Variable.named('x'), Variable.named('y')))
                .invoke("out", Map.of('a', Variable.named('x')))
                .invoke("out", Map.of('a', Constant.of(125)))
                .build();

        assertDoesNotThrow(() -> program.getProcedure("out"));
        assertThrows(UndeclaredProcedureException.class, () -> program.getProcedure("in"));

        LinkedList<Class<? extends Instruction>> expected = new LinkedList<>(List.of(
                MainBlock.class,
                Assignment.class,
                ProcedureInvocation.class,
                ProcedureBlock.class,
                PrintStdOut.class,
                ProcedureInvocation.class,
                ProcedureBlock.class,
                PrintStdOut.class
        ));
        DebugHook hook = instruction -> assertInstanceOf(expected.removeFirst(), instruction);
        program.debugExecute(hook);
        assertTrue(expected.isEmpty());
    }

    @Test
    void t3() throws MacchiatoException { // Test z polecenia z dnia 10 VI 2023.
        var program = new ProgramBuilder()
                .declareVariable('x', Constant.of(101))
                .declareVariable('y', Constant.of(1))
                .declareProcedure("out", List.of('a'), new BlockBuilder()
                        .assign('a', Add.of(Variable.named('a'), Variable.named('x')))
                        .print('a')
                        .build()
                )
                .assign('x', Subtract.of(Variable.named('x'), Variable.named('y')))
                .invoke("out", Map.of('a', Variable.named('x'))) // x = 100, print(100+100)
                .invoke("out", Map.of('a', Constant.of(100))) // print(100+100)
                .add(new BlockBuilder()
                        .declareVariable('x', Constant.of(10))
                        .invoke("out", Map.of('a', Constant.of(100))) // print(100+10)
                        .build()
                )
                .build();
        LinkedList<Integer> expected = new LinkedList<>(List.of(
                100 + 100,
                100 + 100,
                100 + 10
        ));
        DebugHook hook = instruction -> assertDoesNotThrow(() -> {
            if (instruction instanceof PrintStdOut stdOut) {
                assertEquals(expected.removeFirst(), stdOut.getVariable(stdOut.getVariableName()));
            }
        });
        program.debugExecute(hook);
    }

    @Test
    void expressionSimplifier() { // Testuje upraszczanie wyrażeń metodami statycznymi.
        Expression expression = Add.of(
                Add.of(
                        Modulo.of(Multiply.of(Constant.of(5), Constant.of(11)), Constant.of(3)),
                        Multiply.of(Constant.of(0), Variable.named('z'))
                ),
                Modulo.of(Variable.named('z'), Constant.of(1))
        );
        assertInstanceOf(Constant.class, expression);
        assertEquals((5 * 11) % 3, ((Constant) expression).evaluate(ProgramBuilder.create().build()));

        assertThrows(ArithmeticException.class, () -> Modulo.of(Constant.of(5), Constant.of(0)));
    }

    @Test
    void builderExceptions() {
        ProgramBuilder builder = ProgramBuilder.create();
        assertThrows(IllegalArgumentException.class, () -> builder.add(() -> null));
        assertThrows(IllegalArgumentException.class, () -> builder.add(() -> new MainBlock(List.of(), List.of(), Map.of())));
        assertThrows(IllegalArgumentException.class, () -> builder.add(builder));
        assertThrows(IllegalArgumentException.class, () -> builder.forLoop('a', Constant.of(1), builder));
        assertThrows(IllegalArgumentException.class, () -> builder.declareProcedure("a", List.of(), builder));
        assertThrows(IllegalArgumentException.class, () -> builder.ifThen(Equals.of(Constant.of(1), Constant.of(1)), builder));
        assertThrows(IllegalArgumentException.class, () -> builder.ifThenElse(Equals.of(Constant.of(1), Constant.of(1)), builder, builder));
        assertThrows(IllegalArgumentException.class, () -> builder.ifThen(Equals.of(Constant.of(1), Constant.of(1)), () -> null));

        assertThrows(Exception.class, () -> ProcedureBuilder.create().declareProcedure("a", List.of(), builder));
        assertThrows(UnsupportedOperationException.class, () -> ProcedureBuilder.create().declareVariable('a', Constant.of(1)));
    }
}
