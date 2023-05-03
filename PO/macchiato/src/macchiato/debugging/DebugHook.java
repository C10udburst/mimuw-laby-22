package macchiato.debugging;

import macchiato.instructions.Instruction;

public interface DebugHook {
    /**
     * Wywoływana przed wykonaniem instrukcji.
     * @param instruction instrukcja, która miała zostać wykonana jako następna
     */
    void beforeExecute(Instruction instruction);
}
