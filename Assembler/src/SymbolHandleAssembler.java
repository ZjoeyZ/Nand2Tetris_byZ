import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SymbolHandleAssembler {
    private final int A_COMMAND = 0;
    private final int C_COMMAND = 1;
    private final int L_COMMAND = 2;
    private HashMap<String, String> symbolTable = new HashMap<String, String>(){
        {
            put("SP", "0");
            put("LCL", "1");
            put("ARG", "2");
            put("THIS", "3");
            put("THAT", "4");
            put("R0", "0");
            put("R1", "1");
            put("R2", "2");
            put("R3", "3");
            put("R4", "4");
            put("R5", "5");
            put("R6", "6");
            put("R7", "7");
            put("R8", "8");
            put("R9", "9");
            put("R10", "10");
            put("R11", "11");
            put("R12", "12");
            put("R13", "13");
            put("R14", "14");
            put("R15", "15");
            put("SCREEN", "16384");
            put("KBD", "24576");
        }
    };
    private final HashMap<String, String> destToBin = new HashMap<String, String>(){
        {
            put("","000");
            put("M","001");
            put("D","010");
            put("MD","011");
            put("A","100");
            put("AM","101");
            put("AD","110");
            put("ADM","111");
        }
    };
    private final HashMap<String, String> compToBin = new HashMap<String, String>(){
        {
            put("0", "0101010");
            put("1", "0111111");
            put("-1", "0111010");
            put("D", "0001100");
            put("A", "0110000");
            put("!D", "0001101");
            put("!A", "0110001");
            put("-D", "0001111");
            put("-A", "0110011");
            put("D+1", "0011111");
            put("A+1", "0110111");
            put("D-1", "0001110");
            put("A-1", "0110010");
            put("D+A", "0000010");
            put("D-A", "0010011");
            put("A-D", "0000111");
            put("D&A", "0000000");
            put("D|A", "0010101");
            put("M", "1110000");
            put("!M", "1110001");
            put("-M", "1110011");
            put("M+1", "1110111");
            put("M-1", "1110010");
            put("D+M", "1000010");
            put("D-M", "1010011");
            put("M-D", "1000111");
            put("D&M", "1000000");
            put("D|M", "1010101");

        }
    };
    private final HashMap<String, String> jumpToBin = new HashMap<String, String>(){
        {
            put("", "000");
            put("JGT", "001");
            put("JEQ", "010");
            put("JGE", "011");
            put("JLT", "100");
            put("JNE", "101");
            put("JLE", "110");
            put("JMP", "111");
        }
    };

    private int commandType(String command) {
        int type;
        if (command.startsWith("@")) {
            String regex = ".*[a-zA-Z]+.*";
            Matcher m = Pattern.compile(regex).matcher(command);
            if (m.matches() || command.contains("_") ||
                    command.contains("*") || command.contains("$") || command.contains(":")) {
                type = L_COMMAND;
                // 如果含有字母或者特殊字符说明是说明是L类型
            } else {
                type = A_COMMAND;
            }
        } else {
            type = C_COMMAND;
        }
        return type;
    }

    private int getNumber(String command) {
        return Integer.parseInt(command);
    }

    private String fillWithZero(String bin) {
        int len = bin.length();
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= (16 - len); i++) {
            sb.append("0");
        }
        sb.append(bin);
        return sb.toString();
    }

    private String[] participle(String command) {
        // dest_comp_jump = [dest comp jump]
        String[] dest_comp_jump = new String[3];
        int indexSemi = command.indexOf(";");
        int indexEq = command.indexOf("=");

        if (command.contains(";")) {
            dest_comp_jump[2] = command.substring(indexSemi+1);
            if (command.contains("=")) {
                dest_comp_jump[0] = command.substring(0, indexEq);
                dest_comp_jump[1] = command.substring(indexEq + 1, indexSemi);
            } else {
                dest_comp_jump[0] = "";
                dest_comp_jump[1] = command.substring(0, indexSemi);
            }
        } else {
            dest_comp_jump[2] = "";
            dest_comp_jump[0] = command.substring(0, indexEq);
            dest_comp_jump[1] = command.substring(indexEq + 1);
        }
        return dest_comp_jump;
    }

    private void firstScan(String filePath) throws IOException {
        // 准备.asm文件的读取
        BufferedReader in = new BufferedReader(new FileReader(filePath));

        String command;
        long romAddress = -1;
        while ((command = in.readLine()) != null) {
            // 代码去空行、去空格
            command = command.replace(" ", "");
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
            }



            if (command.contains("(") || command.contains(")")) {
                String key = command.substring(command.indexOf("(") + 1, command.indexOf(")"));
                String value = String.valueOf(romAddress + 1);
                symbolTable.put(key, value);
                // System.out.println("add new entry " + key + " : "+ value);
                continue;
            }

            // 这条command的 ROM 地址
            romAddress = romAddress + 1;
        }
        in.close();
    }

    private void secondScan(String filePath) throws IOException {
        // 准备.asm文件的读取
        BufferedReader in = new BufferedReader(new FileReader(filePath));

        // 准备.hack文件的写入。注：输出文件 和输入文件在一个文件夹：
        int indexOfDot = filePath.lastIndexOf(".");
        BufferedWriter out = new BufferedWriter(new FileWriter(filePath.substring(0, indexOfDot) + ".hack"));

        // 逐行读取汇编代码
        String command;
        long varRamAddress = 15;
        while ((command = in.readLine()) != null) {
            // 代码去空行、去空格
            command = command.replace(" ", "");
            command = command.replace("\n", "");
            if (command.startsWith("//")) {
                continue;
            } // 跳过整行注释
            if (command.contains("//")) {
                int index = command.indexOf("/");
                command = command.substring(0, index);
            } // 去掉代码后方注释

            if (command.contains("(")) {
                continue;
            }
            if (command.equals("")) {
                continue;
            }

            // System.out.println(command);
            // 获得汇编代码是哪种类型的指令
            int type = commandType(command);

            // 汇编代码转机器代码
            String bin;
            if (type == A_COMMAND) {
                bin = Integer.toBinaryString(getNumber(command.substring(1)));
                bin = fillWithZero(bin);
                //System.out.println("the bin of A_Command is " + bin);
            } else if (type == C_COMMAND) {
                // dest_comp_jump = [dest comp jump]
                // Parser 模块和 code 模块 功能在这里相交
                String[] dest_comp_jump = participle(command);
                // System.out.println(Arrays.toString(dest_comp_jump));
                String dest = destToBin.get(dest_comp_jump[0]);
                String comp = compToBin.get(dest_comp_jump[1]);
                String jump = jumpToBin.get(dest_comp_jump[2]);
                bin = "111" +  comp + dest + jump;
                // System.out.println("the bin of C_command is " + bin);
            } else {
                String key = command.substring(1);
                String value = symbolTable.get(key);
                if (value == null) {
                    // 变量符号
                    varRamAddress++;
                    value = String.valueOf(varRamAddress);
                    symbolTable.put(key, value);
                    // System.out.println("add new variable entry " + key + " : "+ value);
                }
                bin = Integer.toBinaryString(getNumber(value));
                bin = fillWithZero(bin);
            }

            //把 一行机器代码bin 写入文件中
            out.write(bin + "\r\n");
        }
        out.flush();
        out.close();
        in.close();
    }

    void assemble(String filePath) throws IOException{
        firstScan(filePath);
        secondScan(filePath);
    }
}
