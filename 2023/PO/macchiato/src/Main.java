import macchiato.builder.BlockBuilder;
import macchiato.builder.ProgramBuilder;
import macchiato.debugging.Debugger;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Add;
import macchiato.expressions.Constant;
import macchiato.expressions.Subtract;
import macchiato.expressions.Variable;
import macchiato.instructions.*;
import macchiato.parser.Parser;
import macchiato.parser.ParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

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
        Debugger.debug(ProgramBuilder
                .create()
                .declareVariable('x', Constant.of(101))
                .declareVariable('y', Constant.of(1))
                .declareProcedure("out", List.of('a'), new BlockBuilder()
                        .assign('a', Add.of(Variable.named('a'), Variable.named('x')))
                        .print('a')
                )
                .assign('x', Subtract.of(Variable.named('x'), Variable.named('y')))
                .invoke("out", Map.of('a', Variable.named('x'))) // x = 100, print(100+100)
                .invoke("out", Map.of('a', Constant.of(100))) // print(100+100)
                .add(new BlockBuilder()
                        .declareVariable('x', Constant.of(10))
                        .invoke("out", Map.of('a', Constant.of(100))) // print(100+10)
                )
                .build());
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
