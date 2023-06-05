package macchiato.instructions;

/**
 * Interfejs używany w {@link macchiato.builder.AbstractBlockBuilder} do tworzenia instrukcji.
 */
public interface InstructionLike {
    Instruction toInstruction();
}
