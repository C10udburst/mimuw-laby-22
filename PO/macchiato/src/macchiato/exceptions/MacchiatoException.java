package macchiato.exceptions;

import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

public class MacchiatoException extends Exception {

    @NotNull
    public final Instruction context;

    public MacchiatoException(String s, @NotNull Instruction context) {
        super(s);
        this.context = context;
    }

    public MacchiatoException(@NotNull Instruction context) {
        super("There was an error in the macchiato program.");
        this.context = context;
    }
}
