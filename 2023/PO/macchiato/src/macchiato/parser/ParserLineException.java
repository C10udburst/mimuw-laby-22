package macchiato.parser;

public class ParserLineException extends ParserException {
    public final String sourceLine;
    public final int lineNumber;

    public ParserLineException(String line, int lineNumber) {
        super("Parser error on line " + lineNumber + ": " + line);
        this.sourceLine = line;
        this.lineNumber = lineNumber;
    }
}
