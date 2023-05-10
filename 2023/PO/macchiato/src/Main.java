import macchiato.debugging.Debugger;
import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.MainBlock;
import macchiato.parser.Parser;
import macchiato.parser.ParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws ParserException, MacchiatoException, IOException {
        if (args.length == 0)
            defaultBehaviour();
        else
            withArgs(args);

    }

    /**
     * Domyślne zachowanie programu, gdy nie podano argumentów, wykonuje kod z polecenia.
     */
    private static void defaultBehaviour() {
        String source = """
                    n = (30)
                do
                    for: k = 0..(n 1 -)
                        block
                            p = (1)
                            k = (k 2 +)
                        do {
                            for: i = 0..(k 2 -)
                                block
                                    i = (i 2 +)
                                do {
                                    if: (k i %) = (0)
                                        set: p = (0)
                                }
                            if: (p) = (1)
                                print: k
                        }
                """.stripIndent().trim();
        // Parsuj
        Parser parser = new Parser(source);
        MainBlock mainBlock = null;
        try {
            mainBlock = parser.parse();
        } catch (ParserException | MacchiatoException ex) {
            assert true; // nie powinno się zdarzyć, bo source jest poprawny
        }
        assert mainBlock != null;
        Debugger.debug(mainBlock);
    }

    /**
     * Wykonuje kod z pliku, którego ścieżka jest podana jako argument, lub pyta o ścieżkę, jeśli podano "ask".
     */
    private static void withArgs(String[] args) throws MacchiatoException, ParserException, IOException {
        // Wczytaj plik źródłowy
        String sourcePath = args[0];
        if (args[0].equals("ask")) {
            System.out.print("File path: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            sourcePath = br.readLine();
        }
        String source = Files.readString(new File(sourcePath).toPath());

        // Parsuj
        Parser parser = new Parser(source);
        MainBlock mainBlock = parser.parse();

        // Wykonaj, ewentualnie wykonaj z debugowaniem
        if (args.length > 1 && args[1].equals("debug")) {
            Debugger.debug(mainBlock);
        } else {
            mainBlock.execute();
        }
    }
}