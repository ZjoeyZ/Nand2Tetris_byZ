import java.io.IOException;

public class TestAssembler {
    public static void main(String[] args) {
        SymbolHandleAssembler assembler = new SymbolHandleAssembler();
        try{
            assembler.assemble("C:\\Users\\流川枫\\Downloads\\nand2tetris\\projects\\06\\pong\\Pong.asm");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
