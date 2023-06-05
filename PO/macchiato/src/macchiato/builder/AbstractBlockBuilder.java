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
public abstract class AbstractBlockBuilder<T extends Block, U extends AbstractBlockBuilder<T, U>> {
    @NotNull protected final List<Instruction> instructions;
    @NotNull protected final List<Declaration> declarations;
    @NotNull protected final Map<String, Procedure> procedures;

    public AbstractBlockBuilder() {
        this.instructions = new ArrayList<>();
        this.declarations = new ArrayList<>();
        this.procedures = new HashMap<>();
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

    /**
     * @param instructions Lista instrukcji, które mają zostać dodane do bloku
     */
    public U add(@NotNull List<Instruction> instructions) {
        this.instructions.addAll(instructions);
        return (U) this;
    }

    /**
     * @param instructions Lista instrukcji, które mają zostać dodane do bloku
     */
    public U add(@NotNull Instruction... instructions) {
        return add(Arrays.asList(instructions));
    }

    /**
     * @param blockBuilder Blok, który ma zostać dodany do tego bloku
     */
    public U add(AbstractBlockBuilder<?,?> blockBuilder) {
        if (blockBuilder.equals(this)) throw new IllegalArgumentException("Cannot add a block to itself");
        return add(blockBuilder.build());
    }
    // endregion

    // region declareVariable
    /**
     * @param declaration Deklaracja, która ma zostać dodana do bloku
     */
    public U declareVariable(@NotNull Declaration declaration) {
        declarations.add(declaration);
        return (U) this;
    }

    /**
     * @param v nazwa zmiennej
     * @param value wartość zmiennej
     */
    public U declareVariable(char v, int value) {
        return declareVariable(Declaration.of(v, Constant.of(value)));
    }

    /**
     * @param v nazwa zmiennej
     * @param expression wyrażenie, które ma zostać przypisane do zmiennej
     */
    public U declareVariable(char v, Expression expression) {
        return declareVariable(Declaration.of(v, expression));
    }

    /**
     * @param v nazwa zmiennej
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
     * @param name nazwa procedury
     * @param args argumenty procedury
     * @param builder ciało procedury
     */
    public U declareProcedure(@NotNull String name, @NotNull List<Character> args, @NotNull ProcedureBuilder builder) {
        return declareProcedure(name, builder.buildProcedure(args));
    }

    /**
     * @param name nazwa procedury
     * @param args argumenty procedury
     * @param instructions ciało procedury
     */
    public U declareProcedure(@NotNull String name, @NotNull List<Character> args, @NotNull List<Instruction> instructions) {
        return declareProcedure(name, args, new ProcedureBlock(instructions));
    }
    // endregion

    // region print
    /**
     * Dodaj instrukcję wypisującą wartość zmiennej v na standardowe wyjście
     * @param v nazwa zmiennej
     */
    public U print(char v) {
        return add(new PrintStdOut(v));
    }

    /**
     * Dodaj instrukcję wypisującą wartość wyrażenia expression na standardowe wyjście
     * @param variable zmienna, której wartość ma zostać wypisana
     */
    public U print(@NotNull Variable variable) {
        return print(variable.name);
    }
    // endregion

    // region assign
    /**
     * Dodaj instrukcję ustawiającą wartość zmiennej v na wartość wyrażenia expression
     * @param v nazwa zmiennej
     * @param expression wyrażenie, które ma zostać przypisane do zmiennej
     */
    public U assign(char v, Expression expression) {
        return add(new Assignment(v, expression));
    }

    public U assign(@NotNull Variable variable, Expression expression) {
        return assign(variable.name, expression);
    }

    // endregion

    // region ifThen
    /**
     * dodaj instrukcję warunkową do bloku
     * @param condition warunek instrukcji warunkowej
     * @param then instrukcja, która ma zostać wykonana, jeśli warunek jest spełniony
     * @param otherwise instrukcja, która ma zostać wykonana, jeśli warunek nie jest spełniony
     */
    public U ifThenElse(@NotNull Comparator condition, @NotNull Instruction then, @Nullable Instruction otherwise) {
        return add(new IfStatement(condition, then, otherwise));
    }

    /**
     * dodaj instrukcję warunkową do bloku
     * @param condition warunek instrukcji warunkowej
     * @param then blok, który ma zostać wykonany, jeśli warunek jest spełniony
     * @param otherwise blok, który ma zostać wykonany, jeśli warunek nie jest spełniony
     */
    public U ifThenElse(@NotNull Comparator condition, AbstractBlockBuilder<?,?> then, AbstractBlockBuilder<?,?> otherwise) {
        return ifThenElse(condition, then.build(), otherwise == null ? null : otherwise.build());
    }

    /**
     * dodaj instrukcję warunkową do bloku
     * @param condition warunek instrukcji warunkowej
     * @param then instrukcja, która ma zostać wykonana, jeśli warunek jest spełniony
     */
    public U ifThen(@NotNull Comparator condition, @NotNull Instruction then) {
        return ifThenElse(condition, then, null);
    }

    /**
     * dodaj instrukcję warunkową do bloku
     * @param condition warunek instrukcji warunkowej
     * @param then blok, który ma zostać wykonany, jeśli warunek jest spełniony
     */
    public U ifThen(@NotNull Comparator condition, AbstractBlockBuilder<?,?> then) {
        return ifThen(condition, then.build());
    }
    // endregion

    // region forLoop
    /**
     * dodaj pętle iterującą po zmiennej iterator od 0 do to
     * @param iterator nazwa zmiennej, która będzie iteratorem pętli
     * @param to wyrażenie, które określa, do jakiej wartości ma iterować pętla
     * @param body instrukcja, która ma zostać wykonana w każdej iteracji pętli
     */
    public U forLoop(char iterator, Expression to, @NotNull Instruction body) {
        return add(new ForLoop(iterator, to, body));
    }

    /**
     * dodaj pętle iterującą po zmiennej iterator od 0 do to
     * @param iterator nazwa zmiennej, która będzie iteratorem pętli
     * @param to wyrażenie, które określa, do jakiej wartości ma iterować pętla
     * @param body blok, który ma zostać wykonany w każdej iteracji pętli
     */
    public U forLoop(char iterator, Expression to, AbstractBlockBuilder<?,?> body) {
        return forLoop(iterator, to, body.build());
    }

    /**
     * dodaj pętle iterującą po zmiennej iterator od 0 do to
     * @param iterator nazwa zmiennej, która będzie iteratorem pętli
     * @param to wyrażenie, które określa, do jakiej wartości ma iterować pętla
     * @param body instrukcja, która ma zostać wykonana w każdej iteracji pętli
     */
    public U forLoop(@NotNull Variable iterator, Expression to, @NotNull Instruction body) {
        return forLoop(iterator.name, to, body);
    }

    /**
     * dodaj pętle iterującą po zmiennej iterator od 0 do to
     * @param iterator nazwa zmiennej, która będzie iteratorem pętli
     * @param to wyrażenie, które określa, do jakiej wartości ma iterować pętla
     * @param body blok, który ma zostać wykonany w każdej iteracji pętli
     */
    public U forLoop(@NotNull Variable iterator, Expression to, AbstractBlockBuilder<?,?> body) {
        return forLoop(iterator.name, to, body);
    }
    // endregion

    // region invoke
    /**
     * dodaj inwokację procedury
     * @param name nazwa procedury
     * @param args argumenty procedury
     */
    public U invoke(@NotNull String name, @NotNull Map<Character, Expression> args) {
        return add(new ProcedureInvocation(name, args));
    }
    // endregion
}
