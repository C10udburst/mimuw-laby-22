package macchiato.parser;

import macchiato.Declaration;
import macchiato.comparators.*;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.*;
import macchiato.instructions.*;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Klasa parsująca kod źródłowy.
 * Kod źródłowy jest podawany w konstruktorze jako String.
 * Po utworzeniu obiektu klasy Parser należy wywołać metodę parse().
 * Składnia:
 * <pre>
 * Dla uproszczenia pominięto tutaj znaki końca linii, ale w kodzie źródłowym muszą być one obecne,
 * wszędzie tam, gdzie pojawia się *Deklaracja lub *Instrukcja.
 * Znaki końca linii można zastąpić średnikami lub dwukropkami.
 *
 * Symbol startowy: GłównyBlok
 * GłównyBlok ::= *Deklaracja " do \n" *Instrukcja
 * NazwaZmiennej ::= [a-z]
 * Deklaracja ::= NazwaZmiennej " = " Wyrażenie
 * Instrukcja ::= Blok | PętlaFor | Warunek | Przypisanie | Wypisanie
 *
 * Blok ::= "block\n" *Deklaracja " do {\n" *Instrukcja "}"
 * PętlaFor ::= "for " NazwaZmiennej " = 0.." Wyrażenie "\n" Instrukcja
 * Warunek ::= "if\n " Wyrażenie Porównanie Wyrażenie "\n" Instrukcja ["else\n" Instrukcja]
 * Przypisanie ::= "set\n" NazwaZmiennej " = " Wyrażenie
 * Wypisanie ::= "print\n" NazwaZmiennej
 *
 * Porównanie ::= "=" | "<>" | ">" | "<" | ">=" | "<="
 *
 * Wyrażenia muszą być zapisane w ONP, oddzielone spacjami
 * Wyrażenie ::= "(" NazwaZmiennej | Liczba | Operator ")"
 * Liczba ::= [0-9]+
 * Operator ::= "+" | "-" | "*" | "/" | "%"
 * </pre>
 */
@Deprecated(since = "1.0.1")
public class Parser {
    // Wzorce wyrażeń regularnych
    private static final Pattern CONDITION_PATTERN = Pattern.compile("\\( ?(?<exp1>[a-z0-9+\\-/%*]+(?: [a-z0-9+\\-/%*])*) ?\\) ?(?<sgn>=|<>|>|<|>=|<=) ?\\( ?(?<exp2>[a-z0-9+\\-/%*]+(?: [a-z0-9+\\-/%*])*) ?\\)");
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile("(?<name>[a-z]) ?= ?\\( ?(?<exp>[a-z0-9+\\-/%*]+(?: [a-z0-9+\\-/%*])*) ?\\)");
    private static final Pattern FOR_PATTERN = Pattern.compile("(?<name>[a-z]) ?= ?0\\.\\.?\\( ?(?<exp>[a-z0-9+\\-/%*]+(?: [a-z0-9+\\-/%*])*) ?\\)");

    private final LinkedList<String> lines;
    private int lineNumber = 0;

    public Parser(String source) {
        source = source.replaceAll("[:;]", "\n"); // Traktuj każdy znak : i ; jako nową linię
        source = source.replaceAll("[ \t]+", " "); // Zastąp wszystkie białe znaki pojedynczym spacją

        // Usuń puste linie i białe znaki na początku i końcu linii oraz komentarze
        lines = source.lines()
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .filter(s -> !s.startsWith("#"))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Parsuje kod źródłowy na instrukcje.
     * @return Główny blok instrukcji
     * @throws ParserException W przypadku błędu parsowania
     */
    public MainBlock parse() throws ParserException, MacchiatoException {
        return parseMainBlock();
    }

    /**
     * Na podstawie pierwszej linii kodu wybiera odpowiednią instrukcję i ją parsuje.
     * @return Blok instrukcji
     */
    private Instruction parseAny() throws ParserException, MacchiatoException {
        String line = popLine();

        return switch (line) {
            case "if" -> parseIf();
            case "set" -> parseAssignment();
            case "block" -> parseBlock();
            case "for" -> parseFor();
            case "print" -> parsePrint();
            default -> throw new ParserLineException(line, lineNumber);
        };
    }

    /**
     * Parsuje instrukcję if.
     * @return Instrukcja if
     */
    private IfStatement parseIf() throws ParserException, MacchiatoException {
        String line = popLine();

        // Parsuj warunek <exp1> <=|<>|>|<|>=|<=> <exp2>
        Matcher condition = CONDITION_PATTERN.matcher(line);
        if (!condition.matches()) throw new ParserLineException(line, lineNumber);

        Expression exp1 = parseExpression(condition.group("exp1"));
        Expression exp2 = parseExpression(condition.group("exp2"));
        Comparator comparator = switch (condition.group("sgn")) {
            case "=" -> new Equals(exp1, exp2);
            case "<>" -> new NotEquals(exp1, exp2);
            case ">" -> new GreaterThan(exp1, exp2);
            case "<" -> new LessThan(exp1, exp2);
            case ">=" -> new GreaterEqual(exp1, exp2);
            case "<=" -> new LessEqual(exp1, exp2);
            default -> throw new ParserLineException(line, lineNumber);
        };

        // ifTrue nie może być puste
        Instruction ifTrue = parseAny();

        // else jest opcjonalne
        if (!lines.isEmpty() && lines.get(0).equals("else")) {
            lines.remove(0);
            lineNumber++;
            Instruction ifFalse = parseAny();
            return new IfStatement(comparator, ifTrue, ifFalse);
        } else {
            return new IfStatement(comparator, ifTrue, null);
        }
    }

    /**
     * Parsuje instrukcję for.
     * @return Instrukcja for
     */
    private ForLoop parseFor() throws ParserException, MacchiatoException {
        String line = popLine();

        // Parsuj pętlę <name> = 0..<exp>
        Matcher forLoop = FOR_PATTERN.matcher(line);
        if (!forLoop.matches()) throw new ParserLineException(line, lineNumber);

        char name = forLoop.group("name").charAt(0);
        Expression exp = parseExpression(forLoop.group("exp"));

        // instrukcja w pętli nie może być pusta
        Instruction instruction = parseAny();

        return new ForLoop(name, exp, instruction);
    }

    /**
     * Parsuje blok instrukcji.
     * @return Blok instrukcji
     */
    private Block parseBlock() throws ParserException, MacchiatoException {
        List<Declaration> declarations = new LinkedList<>();
        List<Instruction> instructions = new LinkedList<>();

        boolean finishedDeclarations = false;
        while (!lines.isEmpty() && !lines.get(0).equals("}")) { // Parsuj deklaracje i instrukcje do końca bloku, czyli do }
            if (!finishedDeclarations) {
                if (lines.get(0).equals("do {")) {
                    finishedDeclarations = true;
                    lines.remove(0);
                } else {
                    declarations.add(parseDeclaration());
                }
            } else {
                // Zakończyły się deklaracje, parsuj instrukcje
                instructions.add(parseAny());
            }
        }

        if (lines.isEmpty()) throw new ParserException("Unexpected end of file");

        // Usuń końcowy znak }
        lines.remove(0);
        lineNumber++;

        return new Block(declarations, instructions);
    }

    /**
     * Parsuje główny blok instrukcji.
     * @return Główny blok instrukcji
     */
    private MainBlock parseMainBlock() throws ParserException, MacchiatoException {
        List<Declaration> declarations = new LinkedList<>();
        List<Instruction> instructions = new LinkedList<>();

        boolean finishedDeclarations = false;
        while (!lines.isEmpty()) { // Parsuj deklaracje i instrukcje do końca pliku
            if (!finishedDeclarations) {
                if (lines.get(0).equals("do")) {
                    finishedDeclarations = true;
                    lines.remove(0);
                } else {
                     declarations.add(parseDeclaration());
                }
            } else {
                // Zakończyły się deklaracje, parsuj instrukcje
                instructions.add(parseAny());
            }
        }
        return new MainBlock(declarations, instructions);
    }

    /**
     * Parsuje deklarację zmiennej.
     * @return Deklaracja zmiennej
     * @throws ParserException Błąd parsowania
     */
    private Declaration parseDeclaration() throws ParserException {
        String line = popLine();

        // <name> = <exp>
        Matcher declaration = ASSIGNMENT_PATTERN.matcher(line);
        if (!declaration.matches()) throw new ParserLineException(line, lineNumber);

        char name = declaration.group("name").charAt(0);
        Expression exp = parseExpression(declaration.group("exp"));

        return new Declaration(name, exp);
    }

    /**
     * Parsuje instrukcję przypisania.
     * @return Instrukcja przypisania
     */
    private Assignment parseAssignment() throws ParserException {
        String line = popLine();

        // <name> = <exp>
        Matcher assignment = ASSIGNMENT_PATTERN.matcher(line);
        if (!assignment.matches()) throw new ParserLineException(line, lineNumber);

        char name = assignment.group("name").charAt(0);
        Expression exp = parseExpression(assignment.group("exp"));

        return new Assignment(name, exp);
    }

    /**
     * Parsuje instrukcję wypisania na standardowe wyjście.
     * @return Instrukcja wypisania na standardowe wyjście
     */
    private PrintStdOut parsePrint() throws ParserException {
        String line = popLine();
        return new PrintStdOut(line.charAt(0));
    }

    /**
     * Parsuje wyrażenie arytmetyczne zapisane w odwrotnej notacji polskiej.
     * @param line Wyrażenie arytmetyczne zapisane w odwrotnej notacji polskiej
     * @return Wyrażenie arytmetyczne
     */
    private Expression parseExpression(String line) throws ParserException {
        Deque<Expression> stack = new LinkedList<>();

        for (String token: line.split(" ")) { // tokeny oddzielone spacją
            if (token.matches("[a-z]")) { // zmienna
                stack.push(new Variable(token.charAt(0)));
            } else if (token.matches("[0-9]+")) { // stała
                try {
                    stack.push(new Constant(Integer.parseInt(token)));
                } catch (NumberFormatException e) {
                    throw new ParserLineException(line, lineNumber);
                }
            } else if (token.matches("[+\\-/%*]")) { // operator
                if (stack.size() < 2)
                    throw new ParserLineException(line, lineNumber); // za mało argumentów na stosie

                // jeśli operator, to wykonaj operację na dwóch argumentach ze stosu, kolejność ma znaczenie
                Expression right = stack.pop();
                Expression left = stack.pop();
                switch (token) {
                    case "+" -> stack.push(new Add(left, right));
                    case "-" -> stack.push(new Subtract(left, right));
                    case "*" -> stack.push(new Multiply(left, right));
                    case "/" -> stack.push(new Divide(left, right));
                    case "%" -> stack.push(new Modulo(left, right));
                    default -> throw new ParserLineException(line, lineNumber);
                }
            } else { // nieznany token
                throw new ParserLineException(line, lineNumber);
            }
        }
        if (stack.size() != 1) // na stosie powinien zostać tylko wynik
            throw new ParserLineException(line, lineNumber);
        return stack.pop();
    }

    /**
     * Usuwa pierwszą linię z listy linii i zwraca ją.
     * @return Pierwsza linia
     */
    private String popLine() throws ParserException {
        if (lines.isEmpty()) throw new ParserException("Unexpected end of file");
        lineNumber++;
        return lines.poll();
    }
}
