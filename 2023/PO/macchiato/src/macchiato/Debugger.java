package macchiato;

import macchiato.instructions.Instruction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Debugger {
    // region dane

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";

    private boolean debugging = true;
    private int steps = 0;

    // endregion dane

    public Debugger() {

    }

    /**
     * Wywoływana przed wykonaniem instrukcji. Jeśli debugger jest w trybie debugowania, wyświetla informacje o instrukcji i czeka na komendę użytkownika.
     * @param instruction instrukcja, która zostanie wykonana.
     */
    public void beforeExecute(Instruction instruction) {
        if (shouldBreak()) {
            printDebug(instruction.toString());
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
        System.out.println("> ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            return br.readLine().split(" ");
        } catch (Exception e) {
            handleError(e);
            return null;
        }
    }

    /**
     * Obsługuje komendy użytkownika. Jeśli komenda nie jest rozpoznana, ponawia pobieranie danych.
     * @param currentInstruction
     */
    private void handleUserInput(Instruction currentInstruction) {
        String[] input = getUserInput();
        if (input == null || input.length == 0)
            return;
        switch (input[0].codePointAt(0)) {
            case 'c':
                stopDebugging();
                break;
            case 's':
                try {
                    addSteps(Integer.parseInt(input[1]));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    handleUserInput(currentInstruction);
                }
                break;
            case 'e':
                exit(0);
                break;
            case 'd':
                try {
                    printDebug(currentInstruction.getParent(Integer.parseInt(input[1])).dumpVars());
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    handleUserInput(currentInstruction);
                }
                break;
            default:
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
     * @param status
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
        e.printStackTrace();
        exit(1);
    }
}
