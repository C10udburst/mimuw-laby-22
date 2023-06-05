import macchiato.builder.BlockBuilder;
import macchiato.builder.ProcedureBuilder;
import macchiato.builder.ProgramBuilder;
import macchiato.debugging.Debugger;
import macchiato.exceptions.MacchiatoException;
import macchiato.instructions.*;
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
        Debugger.debug(ProgramBuilder.create()
                .declareVariable('a', 0)
                .declareVariable('b', 1)
                .declareVariable('c', 2)
                .declareProcedure("a", ProcedureBuilder.create()
                        .buildProcedure('a', 'b')
                )
                .declareProcedure("b", ProcedureBuilder.create()
                        .buildProcedure('b', 'c')
                )
                .declareProcedure("c", ProcedureBuilder.create()
                        .buildProcedure('c', 'a')
                )
                .add(BlockBuilder.create()
                        .declareProcedure("z", ProcedureBuilder.create()
                                .buildProcedure('a', 'b')
                        )
                        .declareProcedure("a", ProcedureBuilder.create()
                                .buildProcedure('z', 'x')
                        )
                )
                .build()
        );
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
