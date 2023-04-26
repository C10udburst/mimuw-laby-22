package macchiato;

import macchiato.instructions.Instruction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Debugger {

    // region dane
    protected enum LogLevel {

        INFO("\u001B[0m"),
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
    public void beforeExecute(Instruction instruction) {
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
    public static void debug(Instruction start) {
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
     * Dodaje kroki do wykonania zanim debugger wstrzyma program.
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
    private void handleUserInput(Instruction currentInstruction, boolean finished) {
        String[] input = getUserInput();
        while (input == null || input.length == 0 || input[0].length() < 1)
            input = getUserInput();
        switch (input[0].codePointAt(0)) {
            case 'c': // (c)ontinue
                if (finished)
                    printConsole( "Program już się zakończył.", LogLevel.WARN);
                else
                    stopDebugging();
                break;
            case 's': // (s)tep
                if (finished)
                    printConsole( "Program już się zakończył.", LogLevel.WARN);
                else
                    try {
                        addSteps(Integer.parseInt(input[1]));
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        printConsole("Niepoprawna liczba kroków", LogLevel.ERROR);
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
                        printConsole("Nie znaleziono rodzica o podanej głębokości: "+input[1], LogLevel.WARN);
                    else
                        printConsole(requested.dumpVars(), LogLevel.INFO);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    printConsole("Niepoprawna głębokość", LogLevel.ERROR);
                    handleUserInput(currentInstruction, finished);
                }
                break;
            default:
                printConsole("Nie rozpoznano komendy.", LogLevel.ERROR);
                handleUserInput(currentInstruction, finished); // nie rozpoznano komendy, pobieramy ponownie
        }
        if (finished)
            handleUserInput(currentInstruction, true);
    }

    /**
     * Wywoływana po zakończeniu programu. Wyświetla komunikat o sukcesie lub błędzie.
     * @param mainBlock główny blok programu.
     */
    protected void onFinish(Instruction mainBlock) {
        printConsole("Program zakończył się " + (didFail ? "błędem." : "powodzeniem."), (didFail ? LogLevel.WARN : LogLevel.INFO));
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
     * Wyświetla treść wyjątku i zamyka program.
     * @param e wyjątek.
     */
    public void handleError(Exception e) {
        printConsole(e.getMessage(), LogLevel.ERROR);
        didFail = true;
    }
}
