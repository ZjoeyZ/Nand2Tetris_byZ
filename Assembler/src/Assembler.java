import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Assembler {
    private final int A_COMMAND = 0;
    private final int C_COMMAND = 1;
    private final int L_COMMAND = 2;
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

    private String[] participle(String command) {
        // commands = [dest comp jump]
        String[] commands = new String[3];
        int indexSemi = command.indexOf(";");
        int indexEq = command.indexOf("=");

        if (command.contains(";")) {
            commands[2] = command.substring(indexSemi+1);
            if (command.contains("=")) {
                commands[0] = command.substring(0, indexEq);
                commands[1] = command.substring(indexEq + 1, indexSemi);
            } else {
                commands[0] = "";
                commands[1] = command.substring(0, indexSemi);
            }
        } else {
            commands[2] = "";
            commands[0] = command.substring(0, indexEq);
            commands[1] = command.substring(indexEq + 1);
        }
    return commands;
    }

    private String addZeroBin(String bin) {
        int len = bin.length();
        StringBuffer sb = new StringBuffer();

        for (int i = 1; i <= (16 - len); i++) {
            sb.append("0");
        }
        sb.append(bin);
        return sb.toString();
    }

    private int getNumber(String command) {
        return Integer.parseInt(command.substring(1));
    }

    public void compile(String filePath) throws IOException {

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert fileReader != null;

        BufferedReader bufferReader = new BufferedReader(fileReader);

        String command = null;

        File hackfile = new File("C:\\Users\\流川枫\\Downloads\\nand2tetris\\projects\\06\\add");
        hackfile.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter("Add.hack"));

        while ((command = bufferReader.readLine()) != null) {
            // 去注释、去空行、去空格
            command = command.replace(" ", "");
            command = command.replace("\n", "");
            if (command.startsWith("//")) {
                continue;
            } // 去掉注释
            if (command.contains("//")) {
                int index = command.indexOf("/");
                command = command.substring(0, index);
            } // 去掉代码后注释

            if (command.equals("")) {
                continue;
            }

            // System.out.println(command);

            int type = commandType(command);

            String bin = null;
            if (type == A_COMMAND) {
                bin = Integer.toBinaryString(getNumber(command));
                bin = addZeroBin(bin);
                System.out.println("the bin is " + bin);
            } else if (type == C_COMMAND) {
                String[] commands = participle(command);
                System.out.println(Arrays.toString(commands));
                String dest = destToBin.get(commands[0]);
                String comp = compToBin.get(commands[1]);
                String jump = jumpToBin.get(commands[2]);
                bin = "111" + dest + comp + jump;
                // System.out.println("the bin is " + bin);
            }

            //把 bin 写入 .hack 中的一行
            out.write(bin + "\r\n");
        }
        out.flush();
        out.close();
    }
}
