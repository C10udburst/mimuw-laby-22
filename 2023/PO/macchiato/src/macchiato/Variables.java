package macchiato;

import macchiato.exceptions.InvalidVariableNameException;
import macchiato.exceptions.UndeclaredVariableException;

public class Variables {

    // region dane

    Integer[] vars;

    public Variables() {
        this.vars = new Integer[26];
    }

    // endregion dane


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

    public boolean exists(char i) throws InvalidVariableNameException {
        return vars[findIndex(i)] != null;
    }

    public int get(char i) throws UndeclaredVariableException, InvalidVariableNameException {
        if (vars[findIndex(i)] == null)
            throw new UndeclaredVariableException(i);
        return vars[findIndex(i)];
    }

    public void set(char i, int value) throws UndeclaredVariableException, InvalidVariableNameException {
        if (vars[findIndex(i)] == null)
            throw new UndeclaredVariableException(i);
        vars[findIndex(i)] = value;
    }

    public void declare(char i, int value) throws InvalidVariableNameException {
        vars[findIndex(i)] = value;
    }

    private int findIndex(int i) throws InvalidVariableNameException {
        if (i < 'a' || i > 'z')
            throw new InvalidVariableNameException((char) i);
        return i - 'a';
    }
}
