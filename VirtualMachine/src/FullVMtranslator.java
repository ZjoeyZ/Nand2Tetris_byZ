import java.io.*;
import java.util.Arrays;

public class FullVMtranslator extends VMtranslator{

    VMtranslator.Type commandType(String token) {
        if (Arrays.asList(arithmetic).contains(token)) {
            return VMtranslator.Type.C_ARITHMETIC;
        } else if (token.equals("push")) {
            return VMtranslator.Type.C_PUSH;
        } else if (token.equals("pop")) {
            return VMtranslator.Type.C_POP;
        } else if (token.equals("function")) {
            return VMtranslator.Type.C_FUCTION;
        } else if (token.equals("return")) {
            return VMtranslator.Type.C_RETURN;
        } else if (token.equals("call")) {
            return VMtranslator.Type.C_CALL;
        } else if (token.equals("goto")) {
            return VMtranslator.Type.C_GOTO;
        } else if (token.equals("if-goto")) {
            return VMtranslator.Type.C_IF;
        } else {
            return VMtranslator.Type.C_LABLE;
        }

    }

    void parse() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filePath));

        int indexOfDot = filePath.lastIndexOf(".");
        BufferedWriter out = new BufferedWriter(new FileWriter(filePath.substring(0, indexOfDot) + ".asm"));

        String command;
        Object[] args = new Object[3];
        while ((command = in.readLine()) != null) {
            // 代码去空行
            command = command.replace("\n", "");
            if (command.startsWith("//")) {
                continue;
            } // 跳过整行注释
            if (command.contains("//")) {
                int index = command.indexOf("/");
                command = command.substring(0, index);
            } // 去掉代码后方注释

            if (command.equals("")) {
                continue;
            } // 跳过空行

            // 代码分词
            String[] tokens = command.split("\\s+");

            // 代码类型
            VMtranslator.Type type = commandType(tokens[0]);

            if (type == VMtranslator.Type.C_ARITHMETIC) {
                // add or sub ...
                args[0] = tokens[0];
                writeArithmetic(out, (String) args[0]);
            } else if (type == VMtranslator.Type.C_PUSH || type == VMtranslator.Type.C_POP) {
                args[0] = type;
                args[1] = tokens[1];
                args[2] = Integer.parseInt(tokens[2]);
                writePushPop(out, (VMtranslator.Type) args[0], (String) args[1], (int) args[2]);
            } else if (type == VMtranslator.Type.C_LABLE) {
                args[0] = type;
                args[1] = tokens[1];
                writeLabel(out, (String) args[1]);
            } else if (type == VMtranslator.Type.C_GOTO) {
                args[0] = type;
                args[1] = tokens[1];
                writeGoto(out, (String) args[1]);
            } else if (type == VMtranslator.Type.C_IF) {
                args[0] = type;
                args[1] = tokens[1];
                writeIf(out, (String) args[1]);
            } else if (type == VMtranslator.Type.C_FUCTION) {
                args[0] = type;
                args[1] = tokens[1];
                writeFunction(out, (String) args[1]);
            } else if (type == VMtranslator.Type.C_CALL) {
                args[0] = type;
                args[1] = tokens[1];
                args[2] = Integer.parseInt(tokens[2]);
                writeCall(out, (String) args[1], (int) args[2]);
            } else if (type == VMtranslator.Type.C_RETURN) {
                args[0] = type;
                writeReturn(out);
            }
            System.out.println("the tokens are " + args[0] + " " + args[1] + " " + args[2]);
        }
        in.close();
        out.close();
    }

    FullVMtranslator(String filePath) {
        super(filePath);
    }

    private void writeReturn(BufferedWriter out) {
    }

    private void writeCall(BufferedWriter out, String arg, int arg1) {
    }

    private void writeFunction(BufferedWriter out, String arg) {
    }

    private void writeIf(BufferedWriter out, String arg) throws IOException {
        String commands = null;
        // pop stack head value to D, decrease the sp, if it is not 0, jmp
        commands = "@SP\r\n"
                + "AM=M-1\r\n"
                + "D=M\r\n"
                + "@" + arg + "\r\n"
                + "D;JGT\r\n";
        out.write(commands);

    }

    private void writeGoto(BufferedWriter out, String arg) throws IOException {
        String commands = null;
        // jmp
        commands = "@" + arg + "\r\n"
                + "0;jmp\r\n";
        out.write(commands);
    }

    private void writeLabel(BufferedWriter out, String arg) throws IOException {
        out.write("(" + arg + ")\r\n");
    }
}
