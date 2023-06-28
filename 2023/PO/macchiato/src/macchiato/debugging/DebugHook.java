package macchiato.debugging;

import macchiato.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

public interface DebugHook {
    /**
     * Wywoływana przed wykonaniem instrukcji.
     *
     * @param instruction instrukcja, która miała zostać wykonana jako następna
     */
    void beforeExecute(@NotNull Instruction instruction);
}
