package macchiato.instructions;

import macchiato.Debugger;
import macchiato.Variables;
import macchiato.exceptions.InvalidVariableNameException;
import macchiato.exceptions.MacchiatoException;
import macchiato.exceptions.UndeclaredVariableException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Instruction {
    // region dane
    @Nullable protected Instruction parent;
    @Nullable protected final Variables vars;
    // endregion dane

    // region techniczne
    /**
     * Tworzy instrukcję.
     */
    public Instruction(@Nullable Variables vars) {
        this.vars = vars;
    }

    public abstract String toString();
    // endregion techniczne

    // region operacje na zmiennych
    /**
     * Wypisuje wszystkie zmienne w tej instrukcji.
     * @return reprezentacja tekstowa wszystkich zmiennych.
     */
    public String dumpVars() {
        return vars != null ? vars.toString() : "";
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
                throw new UndeclaredVariableException(name);
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
                throw new UndeclaredVariableException(name);
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
    public abstract void execute() throws MacchiatoException;

    /**
     * Wykonuje instrukcję w trybie debugowania.
     * @param debugger debugger
     * @throws MacchiatoException jeśli wystąpi błąd podczas wykonywania instrukcji
     */
    public abstract void debugExecute(@NotNull Debugger debugger) throws MacchiatoException;

    /**
     * Zwraca nadrzędny blok o podanej głębokości.
     * @param depth głębokość, 0 oznacza obecny blok
     * @return nadrzędny blok o podanej głębokości
     */
    public @Nullable Instruction getParent(int depth) {
        if (depth == 0)  return this;
        if (parent == null)  return null;
        return parent.getParent(depth - 1);
    }
    // endregion operacje na instrukcjach
}
