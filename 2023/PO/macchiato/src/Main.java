import macchiato.Debugger;
import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.MainBlock;
import macchiato.parser.Parser;
import macchiato.parser.ParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws MacchiatoException, ParserException, IOException {
        if (args.length == 0) {
            System.err.println("Usage: macchiato [<source file>|stdin] [debug]");
            System.exit(1);
        }

        // Wczytaj plik źródłowy
        String sourcePath = args[0];
        if (Objects.equals(args[0], "stdin")) {
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