package macchiato.parser;

import macchiato.Declaration;
import macchiato.comparators.*;
import macchiato.comparators.Comparator;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.*;
import macchiato.instructions.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Klasa parsująca kod źródłowy na instrukcje.
 */
public class Parser {
    private static final Pattern CONDITION_PATTERN = Pattern.compile("\\( ?(?<exp1>[a-z0-9+\\-/%*]+(?: [a-z0-9+\\-/%*])*) ?\\) ?(?<sgn>=|<>|>|<|>=|<=) ?\\( ?(?<exp2>[a-z0-9+\\-/%*]+(?: [a-z0-9+\\-/%*])*) ?\\)");
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile("(?<name>[a-z]) ?= ?\\( ?(?<exp>[a-z0-9+\\-/%*]+(?: [a-z0-9+\\-/%*])*) ?\\)");
    private static final Pattern FOR_PATTERN = Pattern.compile("(?<name>[a-z]) ?= ?0\\.\\.?\\( ?(?<exp>[a-z0-9+\\-/%*]+(?: [a-z0-9+\\-/%*])*) ?\\)");

    private final List<String> lines;

    public Parser(String source) {
        source = source.replaceAll("[:;]", "\n"); // Traktuj każdy znak : i ; jako nową linię
        source = source.replaceAll("[ \t]+", " "); // Zastąp wszystkie białe znaki pojedynczym spacją

        // Usuń puste linie i białe znaki na początku i końcu linii oraz komentarze
        lines = source.lines().filter(s -> !s.isBlank()).map(String::trim).filter(s -> !s.startsWith("#")).collect(Collectors.toList());
    }

    public MainBlock parse() throws ParserException {
        return parseMainBlock();
    }

    /**
     * Parsuje dowolny blok instrukcji.
     * @return Blok instrukcji
     * @throws ParserException
     */
    private Instruction parseAny() throws ParserException {
        if (lines.isEmpty()) throw new ParserException("Unexpected end of file");
        String line = lines.get(0);
        lines.remove(0);
        switch (line) {
            case "if":
                return parseIf();
            case "set":
                return parseAssignment();
            case "block":
                return parseBlock();
            case "for":
                return parseFor();
            case "print":
                return parsePrint();
            default:
                throw new ParserLineException(line);
        }
    }

    private IfStatement parseIf() throws ParserException {
        if (lines.isEmpty()) throw new ParserException("Unexpected end of file");
        String line = lines.get(0);
        lines.remove(0);
        Matcher condition = CONDITION_PATTERN.matcher(line);
        if (!condition.matches()) throw new ParserLineException(line);
        Expression exp1 = parseExpression(condition.group("exp1"));
        Expression exp2 = parseExpression(condition.group("exp2"));
        Comparator comparator = switch (condition.group("sgn")) {
            case "=" -> new Equals(exp1, exp2);
            case "<>" -> new NotEquals(exp1, exp2);
            case ">" -> new GreaterThan(exp1, exp2);
            case "<" -> new LessThan(exp1, exp2);
            case ">=" -> new GreaterEqual(exp1, exp2);
            case "<=" -> new LessEqual(exp1, exp2);
            default -> throw new ParserLineException(line);
        };
        Instruction ifTrue = parseAny();
        if (!lines.isEmpty() && lines.get(0).equals("else")) {
            lines.remove(0);
            Instruction ifFalse = parseAny();
            return new IfStatement(comparator, ifTrue, ifFalse);
        } else {
            return new IfStatement(comparator, ifTrue, null);
        }
    }

    private ForLoop parseFor() throws ParserException {
        if (lines.isEmpty()) throw new ParserException("Unexpected end of file");
        String line = lines.get(0);
        lines.remove(0);
        Matcher forLoop = FOR_PATTERN.matcher(line);
        if (!forLoop.matches()) throw new ParserLineException(line);
        char name = forLoop.group("name").charAt(0);
        Expression exp = parseExpression(forLoop.group("exp"));
        Instruction instruction = parseAny();
        return new ForLoop(name, exp, instruction);
    }

    private Block parseBlock() throws ParserException {
        List<Declaration> declarations = new LinkedList<>();
        List<Instruction> instructions = new LinkedList<>();

        boolean finishedDeclarations = false;
        while (!lines.isEmpty() && !lines.get(0).equals("}")) {
            if (!finishedDeclarations) {
                String line = lines.get(0);
                lines.remove(0);
                if (line.equals("do {")) {
                    finishedDeclarations = true;
                } else {
                    Matcher declaration = ASSIGNMENT_PATTERN.matcher(line);
                    if (!declaration.matches()) throw new ParserLineException(line);
                    char name = declaration.group("name").charAt(0);
                    Expression exp = parseExpression(declaration.group("exp"));
                    declarations.add(new Declaration(name, exp));
                }
            } else {
                instructions.add(parseAny());
            }
        }
        lines.remove(0);
        return new Block(declarations, instructions);
    }

    private MainBlock parseMainBlock() throws ParserException {
        List<Declaration> declarations = new LinkedList<>();
        List<Instruction> instructions = new LinkedList<>();

        boolean finishedDeclarations = false;
        while (!lines.isEmpty()) {
            if (!finishedDeclarations) {
                String line = lines.get(0);
                lines.remove(0);
                if (line.equals("do")) {
                    finishedDeclarations = true;
                } else {
                    Matcher declaration = ASSIGNMENT_PATTERN.matcher(line);
                    if (!declaration.matches()) throw new ParserLineException(line);
                    char name = declaration.group("name").charAt(0);
                    Expression exp = parseExpression(declaration.group("exp"));
                    declarations.add(new Declaration(name, exp));
                }
            } else {
                instructions.add(parseAny());
            }
        }
        return new MainBlock(declarations, instructions);
    }

    private Assignment parseAssignment() throws ParserException {
        if (lines.isEmpty()) throw new ParserException("Unexpected end of file");
        String line = lines.get(0);
        lines.remove(0);
        Matcher assignment = ASSIGNMENT_PATTERN.matcher(line);
        if (!assignment.matches()) throw new ParserLineException(line);
        char name = assignment.group("name").charAt(0);
        Expression exp = parseExpression(assignment.group("exp"));
        return new Assignment(name, exp);
    }

    private PrintStdOut parsePrint() throws ParserException {
        if (lines.isEmpty()) throw new ParserException("Unexpected end of file");
        String line = lines.get(0);
        lines.remove(0);
        return new PrintStdOut(line.charAt(0));
    }

    private Expression parseExpression(String line) throws ParserException {
        Deque<Expression> stack = new LinkedList<>();
        Deque<Operator> operators = new LinkedList<>();
        for (String token: line.split(" ")) {
            if (token.matches("[a-z]")) {
                stack.push(new Variable(token.charAt(0)));
            } else if (token.matches("[0-9]+")) {
                try {
                    stack.push(new Constant(Integer.parseInt(token)));
                } catch (NumberFormatException e) {
                    throw new ParserLineException(line);
                }
            } else if (token.matches("[+\\-/%*]")) {
                if (stack.size() < 2) throw new ParserLineException(line);
                Expression right = stack.pop();
                Expression left = stack.pop();
                switch (token) {
                    case "+" -> stack.push(new Add(left, right));
                    case "-" -> stack.push(new Subtract(left, right));
                    case "*" -> stack.push(new Multiply(left, right));
                    case "/" -> stack.push(new Divide(left, right));
                    case "%" -> stack.push(new Modulo(left, right));
                    default -> throw new ParserLineException(line);
                }
            } else {
                throw new ParserLineException(line);
            }
        }
        if (stack.size() != 1) throw new ParserLineException(line);
        return stack.pop();
    }
}
