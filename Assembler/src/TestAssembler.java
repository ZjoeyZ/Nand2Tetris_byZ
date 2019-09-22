import java.io.IOException;

public class TestAssembler {
    public static void main(String[] args) {
        Assembler assembler = new Assembler();
        try{
            assembler.compile("C:\\Users\\流川枫\\Downloads\\nand2tetris\\projects\\06\\pong\\PongL.asm");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
