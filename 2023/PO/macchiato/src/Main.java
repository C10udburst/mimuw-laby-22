import macchiato.Debugger;
import macchiato.Declaration;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Constant;
import macchiato.instructions.Block;
import macchiato.instructions.MainBlock;
import macchiato.parser.Parser;
import macchiato.parser.ParserException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws MacchiatoException, ParserException, IOException {
        if (args.length == 0) {
            System.err.println("Usage: java Main <file>.mac [debug]");
            System.exit(1);
        }
        String source = Files.readString(new File(args[0]).toPath());
        Parser parser = new Parser(source);
        MainBlock mainBlock = parser.parse();
        if (args.length > 1 && args[1].equals("debug")) {
            Debugger.debug(mainBlock);
        } else {
            mainBlock.execute();
        }
    }
}