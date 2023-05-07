package macchiato.debugging;

import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Debugger implements DebugHook {

    // region dane
    protected enum LogLevel {

        INFO("\u001B[0m"),
        SUCCESS("\u001B[32m"),
        WARN("\u001B[33m"),
        ERROR("\u001B[31m")
        ;

        public final String color;

        LogLevel(String s) {
            this.color = s;
        }
    }

    protected static final String ANSI_RESET = "\u001B[0m";

    private boolean debugging = true;
    private int steps = 0;

    private boolean didFail = false;
    // endregion dane

    protected Debugger() {

    }

    /**
     * Wywoływana przed wykonaniem instrukcji. Jeśli debugger jest w trybie debugowania, wyświetla informacje o instrukcji i czeka na komendę użytkownika.
     * @param instruction instrukcja, która zostanie wykonana.
     */
    @Override
    public void beforeExecute(@NotNull Instruction instruction) {
        if (shouldBreak())
            printConsole(instruction.toString(), LogLevel.INFO);
        while (shouldBreak()) {
            handleUserInput(instruction, false);
        } // kontynuujemy wykonywanie programu
        steps--;
    }

    /**
     * Uruchamia debugger na podanym kodzie. Wyświetla informacje o błędzie, jeśli wystąpił.
     * @param start początek kodu.
     */
    public static void debug(@NotNull Instruction start) {
        var debugger = new Debugger();
        try {
            start.debugExecute(debugger);
        } catch (Exception e) {
            debugger.handleError(e);
        }
        debugger.onFinish(start);
    }


    /**
     * Przerywa debugowanie.
     */
    public void stopDebugging() {
        debugging = false;
    }

    /**
     * Sprawdza, czy debugger powinien zatrzymać program.
     * @return true, jeśli debugger powinien zatrzymać program.
     */
    public boolean shouldBreak() {
        return debugging && steps == 0;
    }

    /**
     * Dodaje kroki do wykonania, zanim debugger wstrzyma program.
     * @param steps liczba kroków do wykonania.
     */
    public void addSteps(int steps) {
        this.steps += steps;
    }

    /**
     * Pobiera dane od użytkownika, argumenty oddzielone spacją.
     * @return dane od użytkownika.
     */
    protected String[] getUserInput() {
        System.out.print("> ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            return Arrays.stream(br.readLine().split(" ")).map(String::trim).toArray(String[]::new);
        } catch (Exception e) {
            handleError(e);
            return null;
        }
    }


    /**
     * Obsługuje komendy użytkownika. Jeśli komenda nie jest rozpoznana, ponawia pobieranie danych.
     * @param currentInstruction instrukcja, która będzie wykonana jako następna.
     * @param finished czy wykonywanie już się zakończyło
     */
    private void handleUserInput(@NotNull Instruction currentInstruction, boolean finished) {
        String[] input = getUserInput();
        while (input == null || input.length == 0 || input[0].length() < 1)
            input = getUserInput();
        switch (input[0].codePointAt(0)) {
            case 'c': // (c)ontinue
                if (finished)
                    printConsole( "Program has already finished execution.", LogLevel.WARN);
                else
                    stopDebugging();
                break;
            case 's': // (s)tep
                if (finished)
                    printConsole( "Program has already finished execution.", LogLevel.WARN);
                else
                    try {
                        int steps = Integer.parseInt(input[1]);
                        if (steps < 1) // nie można wykonać mniej niż jednego kroku
                            throw new NumberFormatException();
                        addSteps(steps);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        printConsole("Invalid number of steps.", LogLevel.ERROR);
                        handleUserInput(currentInstruction, false);
                    }
                break;
            case 'e': // (e)xit
                System.exit(didFail ? 1 : 0);
                break;
            case 'd': // (d)isplay
                try {
                    Instruction requested = currentInstruction.getParent(Integer.parseInt(input[1]));
                    if (requested == null)
                        printConsole("Parent instruction of depth " + input[1] + " could not be resolved.", LogLevel.WARN);
                    else
                        printConsole(requested.dumpVars(), LogLevel.INFO);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    printConsole("Invalid depth.", LogLevel.ERROR);
                    handleUserInput(currentInstruction, finished);
                }
                break;
            default:
                printConsole("Invalid command.", LogLevel.ERROR);
                handleUserInput(currentInstruction, finished); // nie rozpoznano komendy, pobieramy ponownie
        }
        if (finished) // jeśli wykonywanie się zakończyło, to nie przerywamy wczytywania komend, gdyż nie ma już co wykonywać
            handleUserInput(currentInstruction, true);
    }

    /**
     * Wywoływana po zakończeniu programu. Wyświetla komunikat o sukcesie lub błędzie.
     * @param mainBlock główny blok programu.
     */
    protected void onFinish(@NotNull Instruction mainBlock) {
        printConsole("Program finished " + (didFail ? "with an error." : "successfully."), (didFail ? LogLevel.WARN : LogLevel.SUCCESS));
        handleUserInput(mainBlock, true);
    }

    /**
     * Wyświetla komunikat w konsoli. Jeśli poziom logowania jest ERROR, wyświetla go na konsoli błędów.
     * @param message treść komunikatu.
     * @param logLevel poziom logowania.
     */
    protected void printConsole(String message, LogLevel logLevel) {
        System.out.println(logLevel.color + message + ANSI_RESET);
    }

    /**
     * Wyświetla komunikat o błędzie i ustawia flagę błędu.
     * @param e wyjątek.
     */
    public void handleError(Exception e) {
        if (e == null) return;
        printConsole(e.getMessage(), LogLevel.ERROR);
        didFail = true;
    }
}