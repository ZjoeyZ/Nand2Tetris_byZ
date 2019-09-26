import java.io.*;
import java.util.Arrays;


public class VMtranslator {
    String filePath;
    String[] arithmetic = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};
    long count = 0;//全局变量用于生成eq、gt、lt的@标签，使得每组标签的名字不一样

    enum Type {
        C_ARITHMETIC,
        C_CALL,
        C_FUCTION,
        C_GOTO,
        C_IF,
        C_LABLE,
        C_POP,
        C_PUSH,
        C_RETURN,
    }

    VMtranslator(String filePath) {
        this.filePath = filePath;
    }

    Type commandType(String token) {
        if (Arrays.asList(arithmetic).contains(token)) {
            return Type.C_ARITHMETIC;
        } else if (token.equals("push")) {
            return Type.C_PUSH;
        } else if (token.equals("pop")) {
            return Type.C_POP;
        } else if (token.equals("function")) {
            return Type.C_FUCTION;
        } else if (token.equals("return")) {
            return Type.C_RETURN;
        } else if (token.equals("call")) {
            return Type.C_CALL;
        } else if (token.equals("goto")) {
            return Type.C_GOTO;
        } else if (token.equals("if-goto")) {
            return Type.C_IF;
        } else {
            return Type.C_LABLE;
        }

    }

    void writeArithmetic(BufferedWriter out, String operater) throws IOException {
        String commands = null;
        switch (operater) {
            case "add":
                commands = "//add \r\n"
                        + "@SP\r\nAM=M-1\r\nD=M\r\nA=A-1\r\n"
                        + "M=M+D\r\n";
                break;
            case "sub":
                commands = "//sub \r\n"
                        + "@SP\r\nAM=M-1\r\nD=M\r\nA=A-1\r\n"
                        + "M=M-D\r\n";
                break;
            case "neg":
                commands = "@SP\r\nA=M-1\r\nM=-M";
                break;
            case "and":
                commands = "@SP\r\nAM=M-1\r\nD=M\r\nA=A-1\r\n"
                        + "M=M&D\r\n";
                break;
            case "or":
                commands = "@SP\r\nAM=M-1\r\nD=M\r\nA=A-1\r\n"
                        + "M=M|D\r\n";
                break;
            case "not":
                commands = "@SP\r\nA=M-1\r\nM=!M";
                break;
            case "eq":
                count++;
                commands = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "D=M-D\n" +
                        "@EQ.true." + count + "\n" +
                        "D;JEQ\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=0\n" +
                        "@EQ.after." + count + "\n" +
                        "0;JMP\n" +
                        "(EQ.true." + count + ")\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=-1\n" +
                        "(EQ.after." + count + ")\n";
                break;
            case "gt":
                count++;
                commands = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "D=M-D\n" +
                        "@GT.true." + count + "\n" +
                        "D;JGT\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=0\n" +
                        "@GT.after." + count + "\n" +
                        "0;JMP\n" +
                        "(GT.true." + count + ")\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=-1\n" +
                        "(GT.after." + count + ")\n";
                break;
            case "lt":
                count++;
                commands = "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "D=M-D\n" +
                        "@LT.true." + count + "\n" +
                        "D;JLT\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=0\n" +
                        "@LT.after." + count + "\n" +
                        "0;JMP\n" +
                        "(LT.true." + count + ")\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=-1\n" +
                        "(LT.after." + count + ")\n";
                break;
        }
        assert commands != null;
        out.write(commands);

    }

    void writePushPop(BufferedWriter out, Type arg, String arg1, int arg2) throws IOException {
        String commands = null;

        if (arg == Type.C_POP) {
            switch (arg1) {
                case "argument":
                    commands = "//pop argument " + arg2 + "\r\n"
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@ARG\r\n"
                            + "A=M\r\n"
                            + "D=A+D\r\n"
                            // D = [ARG] + arg2
                            // 使用中介@R13 存储pop目标地址
                            + "@R13\r\n"
                            + "M=D\r\n"
                            // D get stack head value,SP decrease
                            + "@SP\r\n"
                            + "AM=M-1\r\n"
                            + "D=M\r\n"
                            // put stack value into pop 目标地址
                            + "@R13\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n";
                    break;
                case "local":
                    commands = "//pop local " + arg2 + "\r\n"
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@LCL\r\n"
                            + "A=M\r\n"
                            + "D=A+D\r\n"
                            // 使用中介@R13 存储pop目标地址
                            + "@R13\r\n"
                            + "M=D\r\n"
                            // D get stack head value,SP decrease
                            + "@SP\r\n"
                            + "AM=M-1\r\n"
                            + "D=M\r\n"
                            // put stack value into pop 目标地址
                            + "@R13\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n";
                    break;
                case "this":
                    commands = "//pop this " + arg2 + "\r\n"
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@THIS\r\n"
                            + "A=M\r\n"
                            + "D=A+D\r\n"
                            // 使用中介@R13 存储pop目标地址
                            + "@R13\r\n"
                            + "M=D\r\n"
                            // D get stack head value,SP decrease
                            + "@SP\r\n"
                            + "AM=M-1\r\n"
                            + "D=M\r\n"
                            // put stack value into pop 目标地址
                            + "@R13\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n";
                    break;
                case "that":
                    commands = "//pop that " + arg2 + "\r\n"
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@THAT\r\n"
                            + "A=M\r\n"
                            + "D=A+D\r\n"
                            // 使用中介@R13 存储pop目标地址
                            + "@R13\r\n"
                            + "M=D\r\n"
                            // D get stack head value,SP decrease
                            + "@SP\r\n"
                            + "AM=M-1\r\n"
                            + "D=M\r\n"
                            // put stack value into pop 目标地址
                            + "@R13\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n";
                    break;
                case "temp":
                    commands = "//pop temp " + arg2 + "\r\n"
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@5\r\n"
                            + "D=A+D\r\n"
                            // 使用中介@R13 存储pop目标地址
                            + "@R13\r\n"
                            + "M=D\r\n"
                            // D get stack head value,SP decrease
                            + "@SP\r\n"
                            + "AM=M-1\r\n"
                            + "D=M\r\n"
                            // put stack value into pop 目标地址
                            + "@R13\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n";
                    break;
                case "pointer":
                    commands = "//pop pointer " + arg2 + "\r\n"
                            // get pointer address
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@3\r\n"
                            + "D=A+D\r\n"
                            // 使用中介@R13 存储pop目标地址
                            + "@R13\r\n"
                            + "M=D\r\n"
                            // D get stack head value,SP decrease
                            + "@SP\r\n"
                            + "AM=M-1\r\n"
                            + "D=M\r\n"
                            // put stack value into pop 目标地址
                            + "@R13\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n";
                    break;
                case "static":
                    commands = "//pop static " + arg2 + "\r\n"
                            // get pointer address
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@16\r\n"
                            + "D=A+D\r\n"
                            // 使用中介@R13 存储pop目标地址
                            + "@R13\r\n"
                            + "M=D\r\n"
                            // D get stack head value,SP decrease
                            + "@SP\r\n"
                            + "AM=M-1\r\n"
                            + "D=M\r\n"
                            // put stack value into pop 目标地址
                            + "@R13\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n";
                    break;
            }
        } else if (arg == Type.C_PUSH) {
            switch (arg1) {
                case "constant":
                    commands = "//push constant " + arg2 + "\r\n"
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@SP\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n"
                            + "@SP\r\n"
                            + "M=M+1\r\n";
                    break;
                case "argument":
                    commands = "//push argument " + arg2 + "\r\n"
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@ARG\r\n"
                            + "A=M\r\n"
                            + "A=A+D\r\n" // A = [ARG] + arg2
                            + "D=M\r\n" // D = [[ARG] + arg2]
                            // put D into stack
                            + "@SP\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n"
                            // SP increase
                            + "@SP\r\n"
                            + "M=M+1\r\n";
                    break;
                case "local":
                    commands = "//push local " + arg2 + "\r\n"
                            // D = [base + arg2]
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@LCL\r\n"
                            + "A=M\r\n"
                            + "A=A+D\r\n"
                            + "D=M\r\n"
                            // put D into stack
                            + "@SP\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n"
                            // SP increase
                            + "@SP\r\n"
                            + "M=M+1\r\n";
                    break;
                case "this":
                    commands = "//push this " + arg2 + "\r\n"
                            // D = [base + arg2]
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@THIS\r\n"
                            + "A=M\r\n"
                            + "A=A+D\r\n"
                            + "D=M\r\n"
                            // put D into stack
                            + "@SP\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n"
                            // SP increase
                            + "@SP\r\n"
                            + "M=M+1\r\n";
                    break;
                case "that":
                    commands = "//push that " + arg2 + "\r\n"
                            // D = [[base] + arg2]
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@THAT\r\n"
                            + "A=M\r\n"
                            + "A=A+D\r\n"
                            + "D=M\r\n"
                            // put D into stack
                            + "@SP\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n"
                            // SP increase
                            + "@SP\r\n"
                            + "M=M+1\r\n";
                    break;
                case "temp":
                    commands = "//push temp " + arg2 + "\r\n"
                            // D = [base + arg2]
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@5\r\n"
                            + "A=A+D\r\n"
                            + "D=M\r\n"
                            // put D into stack
                            + "@SP\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n"
                            // SP increase
                            + "@SP\r\n"
                            + "M=M+1\r\n";
                    break;
                case "pointer":
                    commands = "//push poiner " + arg2 + "\r\n"
                            // get pointer address
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@3\r\n"
                            + "A=A+D\r\n"
                            + "D=M\r\n"
                            // put D into stack
                            + "@SP\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n"
                            // SP increase
                            + "@SP\r\n"
                            + "M=M+1\r\n";
                    break;
                case "static":
                    commands = "//push static " + arg2 + "\r\n"
                            // get pointer address
                            + "@" + arg2 + "\r\n"
                            + "D=A\r\n"
                            + "@16\r\n"
                            + "A=A+D\r\n"
                            + "D=M\r\n"
                            // put D into stack
                            + "@SP\r\n"
                            + "A=M\r\n"
                            + "M=D\r\n"
                            // SP increase
                            + "@SP\r\n"
                            + "M=M+1\r\n";
                    break;
            }
        }
        assert commands != null;
        out.write(commands);
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
            Type type = commandType(tokens[0]);

            if (type == Type.C_ARITHMETIC) {
                // add or sub ...
                args[0] = tokens[0];
                writeArithmetic(out, (String) args[0]);
            } else if (type == Type.C_PUSH || type == Type.C_POP) {
                args[0] = type;
                args[1] = tokens[1];
                args[2] = Integer.parseInt(tokens[2]);
                writePushPop(out, (Type) args[0], (String) args[1], (int) args[2]);
            } else {
                break;
            }
            System.out.println("the tokens are " + args[0] + " " + args[1] + " " + args[2]);
        }
        in.close();
        out.close();
    }
}

