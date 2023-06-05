package macchiato.instructions;

import macchiato.Variables;
import macchiato.debugging.DebugHook;
import macchiato.exceptions.InvalidVariableNameException;
import macchiato.exceptions.MacchiatoException;
import macchiato.exceptions.UndeclaredProcedureException;
import macchiato.exceptions.UndeclaredVariableException;
import macchiato.instructions.procedures.Procedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Instruction {
    // region dane
    @Nullable protected Instruction parent;
    @Nullable protected Variables vars;
    // endregion dane

    // region techniczne
    /**
     * Tworzy instrukcję.
     */
    public Instruction(boolean hasVariables) {
        vars = hasVariables ? new Variables(this) : null;
    }

    // wymuszenie implementacji metody toString() w klasach dziedziczących
    public abstract String toString();

    /**
     * @return Krótka nazwa instrukcji, używana przy wypisywaniu np. listy instrukcji w bloku.
     */
    public String getShortName() {
        return this.getClass().getSimpleName();
    }
    // endregion techniczne

    // region operacje na zmiennych
    /**
     * Wypisuje wszystkie zmienne w obecnym wartościowaniu
     * @return reprezentacja tekstowa wszystkich zmiennych.
     */
    public String dumpVars() {
        StringBuilder sb = new StringBuilder();
        for(char name = 'a'; name <= 'z'; name++) {
            try {
                int value = getVariable(name);
                sb.append(name).append(": ").append(value).append(", ");
            } catch (InvalidVariableNameException | UndeclaredVariableException e) { /* nie ma takiej zmiennej */ }
        }
        if (sb.length() > 0) // usuń ostatni przecinek
            sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    /**
     * Zwraca wartość zmiennej o podanej nazwie, szuka w tym bloku i w blokach nadrzędnych.
     * @param name nazwa szukanej zmiennej
     * @return wartość zmiennej
     * @throws InvalidVariableNameException jeśli nazwa zmiennej jest niepoprawna
     * @throws UndeclaredVariableException jeśli zmienna nie została zadeklarowana
     */
    public int getVariable(char name) throws InvalidVariableNameException, UndeclaredVariableException {
        try {
            if (vars == null) // nie ma zmiennych w tym bloku
                throw new UndeclaredVariableException(name, this);
            return vars.get(name);
        } catch (UndeclaredVariableException e) {
            if (parent == null) // nie ma nadrzędnego bloku, więc zmienna nie została zadeklarowana
                throw e;
            return parent.getVariable(name); // szukaj w nadrzędnym bloku
        }
    }

    /**
     * Ustawia wartość zmiennej o podanej nazwie, szuka w tym bloku i w blokach nadrzędnych.
     * @param name nazwa zmiennej
     * @param value wartość zmiennej
     * @throws InvalidVariableNameException jeśli nazwa zmiennej jest niepoprawna
     * @throws UndeclaredVariableException jeśli zmienna nie została zadeklarowana
     */
    public void setVariable(char name, int value) throws InvalidVariableNameException, UndeclaredVariableException {
        try {
            if (vars == null) // nie ma zmiennych w tym bloku
                throw new UndeclaredVariableException(name, this);
            vars.set(name, value);
        } catch (UndeclaredVariableException e) {
            if (parent == null) // nie ma nadrzędnego bloku, więc zmienna nie została zadeklarowana
                throw e;
            parent.setVariable(name, value); // szukaj w nadrzędnym bloku
        }
    }
    // endregion operacje na zmiennych

    // region operacje na instrukcjach
    /**
     * Wykonuje instrukcję.
     * @throws MacchiatoException jeśli wystąpi błąd podczas wykonywania instrukcji
     */
    public void execute() throws MacchiatoException {
        if (vars != null)
            vars.reset();
    }

    /**
     * Wykonuje instrukcję w trybie debugowania.
     * @param debugger obiekt, który ma zostać powiadomiony o wykonaniu instrukcji
     * @throws MacchiatoException jeśli wystąpi błąd podczas wykonywania instrukcji
     */
    public void debugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        if (vars != null)
            vars.reset();
    }

    /**
     * Zwraca nadrzędną instrukcję o podanej głębokości.
     * @param depth głębokość, 0 oznacza obecną instrukcję
     * @return nadrzędna instrukcja o podanej głębokości
     */
    public @Nullable Instruction getParent(int depth) {
        if (depth == 0) return this;
        if (parent == null)  return null;
        return parent.getParent(depth - 1);
    }

    /**
     * Zwraca procedure o podanej nazwie, szuka w tym bloku i w blokach nadrzędnych.
     * @param name nazwa szukanej procedury
     * @return procedura o podanej nazwie lub null, jeśli nie znaleziono
     */
    public @NotNull Procedure getProcedure(String name) throws UndeclaredProcedureException {
        if (parent == null)
                throw new UndeclaredProcedureException(this, name);
        return parent.getProcedure(name);
    }

    /**
     * Zwraca zbiór nazw zadeklarowanych procedur w tym bloku i w blokach nadrzędnych.
     * @return zbiór nazw zadeklarowanych procedur
     */
    public @NotNull HashSet<String> declaredProcedures() {
        if (parent == null)
            return new HashSet<>();
        return parent.declaredProcedures();
    }

    // endregion operacje na instrukcjach
}
