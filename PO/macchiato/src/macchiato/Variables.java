package macchiato;

import macchiato.exceptions.InvalidVariableNameException;
import macchiato.exceptions.UndeclaredVariableException;
import macchiato.exceptions.VariableRedeclarationException;
import org.jetbrains.annotations.NotNull;

public class Variables {

    // region dane

    @NotNull
    private Integer[] vars;

    public Variables() {
        this.vars = new Integer[26];
    }

    // endregion dane


    /**
     * Wypisuje wszystkie zmienne wraz z ich wartościami.
     * @return reprezentacja tekstowa zmiennych
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (char i = 'a'; i <= 'z'; i++) {
            if (vars[i - 'a'] != null) {
                if (!sb.isEmpty())
                    sb.append(", ");
                sb.append(i).append(": ").append(vars[i - 'a']);
            }
        }
        return sb.toString();
    }

    /**
     * Zwraca wartość zmiennej o podanej nazwie.
     * @param i nazwa zmiennej
     * @return wartość zmiennej o nazwie i
     * @throws UndeclaredVariableException jeśli zmienna nie została zadeklarowana w danym kontekście
     * @throws InvalidVariableNameException jeśli nazwa zmiennej jest niepoprawna
     */
    public int get(char i) throws UndeclaredVariableException, InvalidVariableNameException {
        if (vars[findIndex(i)] == null)
            throw new UndeclaredVariableException(i);
        return vars[findIndex(i)];
    }

    /**
     * Ustawia wartość zmiennej o podanej nazwie.
     * @param i nazwa zmiennej, której wartość ma zostać ustawiona
     * @param value wartość, na którą ma zostać ustawiona zmienna
     * @throws UndeclaredVariableException jeśli zmienna nie została zadeklarowana w danym kontekście
     * @throws InvalidVariableNameException jeśli nazwa zmiennej jest niepoprawna
     */
    public void set(char i, int value) throws UndeclaredVariableException, InvalidVariableNameException {
        if (vars[findIndex(i)] == null)
            throw new UndeclaredVariableException(i);
        vars[findIndex(i)] = value;
    }

    /**
     * Deklaruje zmienną o podanej nazwie i wartości, jeśli nie została zadeklarowana wcześniej.
     * @param i nazwa zmiennej
     * @param value wartość zmiennej
     * @throws InvalidVariableNameException jeśli nazwa zmiennej jest niepoprawna
     * @throws VariableRedeclarationException jeśli zmienna została zadeklarowana wcześniej
     */
    public void declare(char i, int value) throws InvalidVariableNameException, VariableRedeclarationException {
        if (vars[findIndex(i)] != null)
            throw new VariableRedeclarationException(i);
        vars[findIndex(i)] = value;
    }

    /**
     * Usuwa wszystkie zmienne, resetując stan.
     */
    public void reset() {
        vars = new Integer[26];
    }

    /**
     * Zwraca indeks zmiennej o podanej nazwie.
     * @param i nazwa zmiennej
     * @return indeks zmiennej o nazwie i
     * @throws InvalidVariableNameException jeśli nazwa zmiennej jest niepoprawna
     */
    private int findIndex(int i) throws InvalidVariableNameException {
        if (i < 'a' || i > 'z')
            throw new InvalidVariableNameException((char) i);
        return i - 'a';
    }
}
