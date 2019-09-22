import java.io.IOException;

public class TestAssembler {
    public static void main(String[] args) throws IOException {
        Assembler assembler = new Assembler();
        // assembler.compile("C:\\Users\\流川枫\\Downloads\\nand2tetris\\projects\\06\\max\\MaxL.asm");
        assembler.compile("C:\\Users\\流川枫\\Downloads\\nand2tetris\\projects\\06\\pong\\PongL.asm");
    }
}
