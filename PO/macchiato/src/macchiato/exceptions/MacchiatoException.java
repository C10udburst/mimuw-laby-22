package macchiato.exceptions;

public class MacchiatoException extends Exception {
    public MacchiatoException(String s) {
        super(s);
    }

    public MacchiatoException() {
        super("There was an error in the macchiato program.");
    }
}
