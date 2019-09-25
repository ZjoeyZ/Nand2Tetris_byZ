import java.io.IOException;

public class TestVM {
    public static void main(String[] args) {
        Parser parser1 = new Parser("C:\\Users\\流川枫\\Downloads\\nand2tetris\\projects\\07\\MemoryAccess\\StaticTest\\StaticTest.vm");
        try {
            parser1.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
