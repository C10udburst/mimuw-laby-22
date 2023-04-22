import macchiato.Debugger;
import macchiato.Declaration;
import macchiato.exceptions.MacchiatoException;
import macchiato.expressions.Constant;
import macchiato.instructions.Block;
import macchiato.instructions.MainBlock;

import java.util.Arrays;
import java.util.Collections;

public class Main {
    public static void main(String[] args) throws MacchiatoException {
        var main = new MainBlock(Collections.emptyList(), Arrays.asList(new Block(Arrays.asList(
                new Declaration('a', Constant.fromInt(1))
        ), Collections.emptyList())));
        main.execute();
        System.out.println(main);
    }
}