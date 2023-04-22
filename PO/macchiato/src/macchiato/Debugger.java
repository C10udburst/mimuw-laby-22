package macchiato;

import macchiato.instructions.Instruction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Debugger {
    // region dane

    private boolean debugging = true;
    private int steps = 0;

    // endregion dane

    public Debugger() {

    }

    public void beforeExecute(Instruction instruction) {
        if (shouldBreak()) {
            printDebug(instruction.toString());
            handleUserInput(instruction);
        }
        steps--;
    }

    public static void debug(Instruction start) {
        var debugger = new Debugger();
        try {
            start.debugExecute(debugger);
        } catch (Exception e) {
            debugger.handleError(e);
        }
        debugger.exit(0);
    }


    public void stopDebugging() {
        debugging = false;
    }

    public boolean shouldBreak() {
        return debugging && steps == 0;
    }

    public void addSteps(int steps) {
        this.steps += steps;
    }

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
                handleUserInput(currentInstruction);
        }
    }

    public void printDebug(String s) {
        System.out.println(s);
    }

    private void exit(int status) {
        System.exit(status);
    }

    public void handleError(Exception e) {
        System.out.println(e.getMessage());
        exit(1);
    }
}
