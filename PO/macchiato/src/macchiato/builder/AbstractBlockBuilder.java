package macchiato.builder;

import macchiato.Declaration;
import macchiato.comparators.Comparator;
import macchiato.expressions.Constant;
import macchiato.expressions.Expression;
import macchiato.expressions.Variable;
import macchiato.instructions.*;
import macchiato.instructions.procedures.Procedure;
import macchiato.instructions.procedures.ProcedureBlock;
import macchiato.instructions.procedures.ProcedureInvocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unused")
public abstract class AbstractBlockBuilder<T extends Block, U extends AbstractBlockBuilder<T, U>> implements InstructionLike {
    @NotNull
    protected final List<Instruction> instructions;
    @NotNull
    protected final List<Declaration> declarations;
    @NotNull
    protected final Map<String, Procedure> procedures;

    public AbstractBlockBuilder() {
        this.instructions = new ArrayList<>();
        this.declarations = new ArrayList<>();
        this.procedures = new HashMap<>();
    }

    @Override
    public Instruction toInstruction() {
        return build();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{" +
                "\n\tinstructions=" +
                instructions +
                "\n\tdeclarations=" +
                declarations +
                "\n\tprocedures=" +
                procedures +
                '}';
    }

    public abstract T build();

    // region add

    @SuppressWarnings("unchecked")
    public U add(@NotNull Instruction instruction) {
        this.instructions.add(canBeChild(instruction));
        return (U) this;
    }

    /**
     * @param instructions Lista instrukcji, które mają zostać dodane do bloku
     */
    @SuppressWarnings("unchecked")
    public U add(@NotNull List<InstructionLike> instructions) {
        for (InstructionLike instruction : instructions) {
            if (instruction == this)
                throw new IllegalArgumentException("Cannot add instruction to itself");
            add(instruction.toInstruction());
        }
        return (U) this;
    }

    /**
     * @param instructions Lista instrukcji, które mają zostać dodane do bloku
     */
    public U add(@NotNull InstructionLike... instructions) {
        return add(Arrays.asList(instructions));
    }
    // endregion

    // region declareVariable

    /**
     * @param declaration Deklaracja, która ma zostać dodana do bloku
     */
    @SuppressWarnings("unchecked")
    public U declareVariable(@NotNull Declaration declaration) {
        declarations.add(declaration);
        return (U) this;
    }

    /**
     * @param v     nazwa zmiennej
     * @param value wartość zmiennej
     */
    public U declareVariable(char v, int value) {
        return declareVariable(Declaration.of(v, Constant.of(value)));
    }

    /**
     * @param v          nazwa zmiennej
     * @param expression wyrażenie, które ma zostać przypisane do zmiennej
     */
    public U declareVariable(char v, Expression expression) {
        return declareVariable(Declaration.of(v, expression));
    }

    /**
     * @param v          nazwa zmiennej
     * @param expression wyrażenie, które ma zostać przypisane do zmiennej
     */
    public U declareVariable(@NotNull Variable v, Expression expression) {
        return declareVariable(v.name, expression);
    }
    // endregion

    // region declareProcedure

    /**
     * @param procedure procedura, która ma zostać dodana do bloku
     */
    @SuppressWarnings("unchecked")
    public U declareProcedure(@NotNull String name, @NotNull Procedure procedure) {
        procedures.put(name, procedure);
        return (U) this;
    }

    /**
     * @param name nazwa procedury
     * @param args argumenty procedury
     * @param body ciało procedury
     */
    public U declareProcedure(@NotNull String name, @NotNull List<Character> args, @NotNull ProcedureBlock body) {
        return declareProcedure(name, new Procedure(args, body));
    }

    /**
     * @param name    nazwa procedury
     * @param args    argumenty procedury
     * @param builder ciało procedury
     */
    public U declareProcedure(@NotNull String name, @NotNull List<Character> args, @NotNull ProcedureBuilder builder) {
        return declareProcedure(name, builder.buildProcedure(args));
    }

    /**
     * @param name         nazwa procedury
     * @param args         argumenty procedury
     * @param instructions ciało procedury
     */
    public U declareProcedure(@NotNull String name, @NotNull List<Character> args, @NotNull List<InstructionLike> instructions) {
        List<Instruction> instructionsList = new ArrayList<>();
        for (InstructionLike instruction : instructions) {
            if (instruction == this)
                throw new IllegalArgumentException("Cannot add instruction to itself");
            instructionsList.add(canBeChild(instruction.toInstruction()));
        }
        return declareProcedure(name, args, new ProcedureBlock(instructionsList));
    }

    /**
     * @param name         nazwa procedury
     * @param args         argumenty procedury
     * @param instructions ciało procedury
     */
    public U declareProcedure(@NotNull String name, @NotNull List<Character> args, InstructionLike... instructions) {
        return declareProcedure(name, args, Arrays.asList(instructions));
    }
    // endregion

    // region print

    /**
     * Dodaj instrukcję wypisującą wartość zmiennej v na standardowe wyjście
     *
     * @param v nazwa zmiennej
     */
    public U print(char v) {
        return add(new PrintStdOut(v));
    }

    /**
     * Dodaj instrukcję wypisującą wartość wyrażenia expression na standardowe wyjście
     *
     * @param variable zmienna, której wartość ma zostać wypisana
     */
    public U print(@NotNull Variable variable) {
        return print(variable.name);
    }
    // endregion

    // region assign

    /**
     * Dodaj instrukcję ustawiającą wartość zmiennej v na wartość wyrażenia expression
     *
     * @param v          nazwa zmiennej
     * @param expression wyrażenie, które ma zostać przypisane do zmiennej
     */
    public U assign(char v, Expression expression) {
        return add(new Assignment(v, expression));
    }

    /**
     * Dodaj instrukcję ustawiającą wartość zmiennej variable na wartość wyrażenia expression
     *
     * @param variable   zmienna, której wartość ma zostać ustawiona
     * @param expression wyrażenie, które ma zostać przypisane do zmiennej
     */
    public U assign(@NotNull Variable variable, Expression expression) {
        return assign(variable.name, expression);
    }

    /**
     * Dodaj instrukcję ustawiającą wartość zmiennej v na wartość value
     *
     * @param v     nazwa zmiennej
     * @param value wartość, która ma zostać przypisana do zmiennej
     */
    public U assign(char v, int value) {
        return assign(v, Constant.of(value));
    }
    // endregion

    // region ifThen

    /**
     * dodaj instrukcję warunkową do bloku
     *
     * @param condition warunek instrukcji warunkowej
     * @param then      instrukcja, która ma zostać wykonana, jeśli warunek jest spełniony
     * @param otherwise instrukcja, która ma zostać wykonana, jeśli warunek nie jest spełniony
     */
    public U ifThenElse(@NotNull Comparator condition, @NotNull InstructionLike then, @Nullable InstructionLike otherwise) {
        if (then == this || otherwise == this)
            throw new IllegalArgumentException("Instruction cannot be added to itself");
        Instruction otherwiseInstruction = otherwise == null ? null : otherwise.toInstruction();
        return add(new IfStatement(condition, canBeChild(then.toInstruction()), otherwiseInstruction == null ? null : canBeChild(otherwiseInstruction)));
    }

    /**
     * dodaj instrukcję warunkową do bloku
     *
     * @param condition warunek instrukcji warunkowej
     * @param then      instrukcja, która ma zostać wykonana, jeśli warunek jest spełniony
     */
    public U ifThen(@NotNull Comparator condition, @NotNull InstructionLike then) {
        if (then == this)
            throw new IllegalArgumentException("Instruction cannot be added to itself");
        return ifThenElse(condition, then, null);
    }
    // endregion

    // region forLoop

    /**
     * dodaj pętle iterującą po zmiennej iterator od 0 do to
     *
     * @param iterator nazwa zmiennej, która będzie iteratorem pętli
     * @param to       wyrażenie, które określa, do jakiej wartości ma iterować pętla
     * @param body     instrukcja, która ma zostać wykonana w każdej iteracji pętli
     */
    public U forLoop(char iterator, Expression to, @NotNull InstructionLike body) {
        if (body == this)
            throw new IllegalArgumentException("Instruction cannot be added to itself");
        return add(new ForLoop(iterator, to, canBeChild(body.toInstruction())));
    }

    /**
     * dodaj pętle iterującą po zmiennej iterator od 0 do to
     *
     * @param iterator nazwa zmiennej, która będzie iteratorem pętli
     * @param to       wyrażenie, które określa, do jakiej wartości ma iterować pętla
     * @param body     instrukcja, która ma zostać wykonana w każdej iteracji pętli
     */
    public U forLoop(@NotNull Variable iterator, Expression to, @NotNull InstructionLike body) {
        return forLoop(iterator.name, to, body);
    }
    // endregion

    // region invoke

    /**
     * dodaj inwokację procedury
     *
     * @param name nazwa procedury
     * @param args argumenty procedury
     */
    public U invoke(@NotNull String name, @NotNull Map<Character, Expression> args) {
        return add(new ProcedureInvocation(name, args));
    }

    /**
     * Funkcja pomocnicza, sprawdzająca, czy instrukcja może być dzieckiem innej instrukcji
     * @throws IllegalArgumentException jeśli instrukcja nie może być dzieckiem innej instrukcji
     */
    private static <T extends Instruction> T canBeChild(T instruction) {
        if (instruction instanceof MainBlock)
            throw new IllegalArgumentException("Main block cannot be a child of any instruction");
        if (instruction instanceof ProcedureBlock)
            throw new IllegalArgumentException("Procedure block can only be a child of a procedure");
        if (instruction == null)
            throw new IllegalArgumentException("Instruction cannot be null");
        return instruction;
    }
    // endregion
}
