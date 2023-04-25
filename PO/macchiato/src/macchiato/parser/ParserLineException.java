package macchiato.parser;

public class ParserLineException extends ParserException {
    private final String sourceLine;
    private final int lineNumber;

    public ParserLineException(String line, int lineNumber) {
        super("Parser error on line " + lineNumber + ": " + line);
        this.sourceLine = line;
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getSourceLine() {
        return sourceLine;
    }
}
