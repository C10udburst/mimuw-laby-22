package macchiato.parser;

public class ParserLineException extends ParserException {
    public ParserLineException(String line) {
        super("Parser error on line: " + line);
    }
}
