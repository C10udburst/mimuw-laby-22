package macchiato;

import macchiato.instructions.Instruction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Debugger {
    // region dane

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private boolean debugging = true;
    private int steps = 0;

    // endregion dane

    protected Debugger() {

    }

    /**
     * Wywoływana przed wykonaniem instrukcji. Jeśli debugger jest w trybie debugowania, wyświetla informacje o instrukcji i czeka na komendę użytkownika.
     * @param instruction instrukcja, która zostanie wykonana.
     */
    public void beforeExecute(Instruction instruction) {
        if (shouldBreak()) printDebug(instruction.toString());
        while (shouldBreak()) {
            handleUserInput(instruction);
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
        debugger.exit(0);
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
    private String[] getUserInput() {
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
     */
    private void handleUserInput(Instruction currentInstruction) {
        String[] input = getUserInput();
        while (input == null || input.length == 0 || input[0].length() < 1)
            input = getUserInput();
        switch (input[0].codePointAt(0)) {
            case 'c': // (c)ontinue
                stopDebugging();
                break;
            case 's': // (s)tep
                try {
                    addSteps(Integer.parseInt(input[1]));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println(ANSI_RED+"Niepoprawna liczba kroków"+ANSI_RESET);
                    handleUserInput(currentInstruction);
                }
                break;
            case 'e': // (e)xit
                exit(0);
                break;
            case 'd': // (d)isplay
                try {
                    Instruction requested = currentInstruction.getParent(Integer.parseInt(input[1]));
                    if (requested == null)
                        System.out.println(ANSI_YELLOW+"Nie znaleziono rodzica o podanej głębokości: "+input[1]+ANSI_RESET);
                    else
                        System.out.println(requested.dumpVars());
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println(ANSI_RED+"Niepoprawna głębokość"+ANSI_RESET);
                    handleUserInput(currentInstruction);
                }
                break;
            default:
                System.err.println(ANSI_RED+"Nie rozpoznano komendy."+ANSI_RESET);
                handleUserInput(currentInstruction); // nie rozpoznano komendy, pobieramy ponownie
        }
    }

    /**
     * Wyświetla komunikat w trybie debugowania.
     * @param s komunikat
     */
    public void printDebug(String s) {
        System.out.println(s);
    }

    /**
     * Kończy pracę debuggera.
     */
    private void exit(int status) {
        System.exit(status);
    }

    /**
     * Wyświetla treść wyjątku i zamyka program.
     * @param e wyjątek.
     */
    public void handleError(Exception e) {
        System.err.println(ANSI_RED + e.getMessage() + ANSI_RESET);
        exit(1);
    }
}
