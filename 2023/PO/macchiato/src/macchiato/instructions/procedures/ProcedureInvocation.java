package macchiato.instructions.procedures;

import macchiato.debugging.DebugHook;
import macchiato.exceptions.IllegalArgumentsException;
import macchiato.exceptions.MacchiatoException;
import macchiato.exceptions.MissingArgumentException;
import macchiato.expressions.Expression;
import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ProcedureInvocation extends Instruction {
    @NotNull
    private final String name;

    @NotNull
    private final Map<Character, Expression> arguments;

    public ProcedureInvocation(@NotNull String name, @NotNull Map<Character, Expression> arguments) {
        super();
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Invoke ").append(name).append(" with:");
        for (Map.Entry<Character, Expression> entry : arguments.entrySet()) {
            builder.append("\n\t").append(entry.getKey()).append(" := ").append(entry.getValue());
        }
        return builder.toString();
    }

    @Override
    public String getShortName() {
        return "exec";
    }

    @Override
    protected void internalExecute() throws MacchiatoException {
        Procedure procedure = getProcedure(name);
        procedure.execute(this);
    }

    @Override
    protected void internalDebugExecute(@NotNull DebugHook debugger) throws MacchiatoException {
        debugger.beforeExecute(this);
        Procedure procedure = getProcedure(name);
        procedure.debugExecute(this, debugger);
    }

    /**
     * Zwraca wartość argumentu o podanym kluczu
     * @param argument klucz argumentu
     * @return wartość argumentu
     * @throws MacchiatoException jeśli argument nie istnieje
     */
    protected int evaluateArgument(char argument) throws MacchiatoException {
        Expression expression = arguments.get(argument);
        if (expression == null) {
            throw new MissingArgumentException(argument, this);
        }
        return expression.evaluate(this);
    }

    /**
     * Sprawdza, czy podano odpowiednią liczbę argumentów
     * @param expectedCount oczekiwana liczba argumentów
     * @throws MacchiatoException jeśli liczba argumentów jest nieprawidłowa
     */
    protected void checkArguments(int expectedCount) throws MacchiatoException {
        if (arguments.size() != expectedCount) {
            throw new IllegalArgumentsException(this);
        }
    }
}
