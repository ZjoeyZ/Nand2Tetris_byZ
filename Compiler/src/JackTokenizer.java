import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


public class JackTokenizer {
    private BufferedReader br;
    private ArrayList<String> files = new ArrayList<>();
    private int fileIdx = 0;

    private HashMap<String, String> keyWordsAndSymbols = new HashMap<String, String>() {{
        put("class", "keyword");
        put("method", "keyword");
        put("constructor", "keyword");
        put("function", "keyword");
        put("int", "keyword");
        put("boolean", "keyword");
        put("char", "keyword");
        put("void", "keyword");
        put("var", "keyword");
        put("static", "keyword");
        put("field", "keyword");
        put("let", "keyword");
        put("do", "keyword");
        put("if", "keyword");
        put("else", "keyword");
        put("while", "keyword");
        put("return", "keyword");
        put("true", "keyword");
        put("false", "keyword");
        put("null", "keyword");
        put("this", "keyword");
        put("(", "symbol");
        put(")", "symbol");
        put("[", "symbol");
        put("]", "symbol");
        put("{", "symbol");
        put("}", "symbol");
        put(",", "symbol");
        put(";", "symbol");
        put("=", "symbol");
        put(".", "symbol");
        put("+", "symbol");
        put("-", "symbol");
        put("*", "symbol");
        put("/", "symbol");
        put("&", "symbol");
        put("|", "symbol");
        put("~", "symbol");
        put("<", "symbol");
        put(">", "symbol");
    }};

    public JackTokenizer(String path) {
        File file = new File(path);

        if (file.isFile()) {
            files.add(path);
        } else {
            File[] tempList = file.listFiles();

            assert tempList != null;

            for (File aTempList : tempList) {
                if (aTempList.toString().endsWith(".jack")) {
                    System.out.println("获取文件：" + aTempList.toString());
                    files.add(aTempList.toString());
                }
            }
        }
    }

    private boolean open() {
        try {
            if (br == null && fileIdx != files.size()) {
                br = new BufferedReader(new FileReader(files.get(fileIdx)));
                fileIdx += 1;
                return true;
            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void close() {
        try {
            if (br != null)
                br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String nextLine() throws IOException {
        // 输入流
        if (br == null) {
            if (!open()) {
                return null;
            }
        }

        // 读取
        String line;
        while (true) {
            line = br.readLine();
            if (line == null) {
                close();
                br = null;
                return nextLine();
            }
            if (line.startsWith("/*")) {
                continue;
            }
            if (line.startsWith("/**")) {
                continue;
            }
            if (line.endsWith("*/")) {
                continue;
            }
            //surround ';' '(' ')' '.' [] -with space
            // ignore < > * + | &
            line = line.replaceAll("\\(", " ( ");
            line = line.replaceAll("\\)", " ) ");
            line = line.replaceAll("\\.", " . ");
            line = line.replaceAll(";", " ; ");
            line = line.replaceAll("\\[", " [ ");
            line = line.replaceAll("]", " ] ");
            line = line.replaceAll("-", " - ");
            line = line.replaceAll("~", " ~ ");
            line = line.replaceAll(",", " , ");

            line = line.replaceAll("//.*", "").trim();
            if (line.length() == 0)
                continue;

            return line;
        }
    }

    public static boolean isNumeric(String str) {
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;//异常 说明包含非数字。
        }
        return true;
    }

    private ListIterator<String> getTokens(String line) {
        // 如果有一个字符串，要先单独取出字符串
        // 并且放入合适的位置, 留一个 ",然后用字符串 replace 它
        String subString = null;
        if (line.contains("\"")) {
            int index1 = line.indexOf("\"");
            int index2 = line.lastIndexOf("\"");
            subString = line.substring(index1, index2 + 1);
            line = line.substring(0, index1) + line.substring(index2);
            line = line.replace("\"", " \" ");
        }

        String[] String = line.split("\\s+");
        ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(String));

        if (subString != null) {
            tokens.set(tokens.indexOf("\""), subString);
        }

        return tokens.listIterator();
    }

    private String tokenType(String token) {
        if (keyWordsAndSymbols.containsKey(token)) {
            return keyWordsAndSymbols.get(token);
        }
        if (isNumeric(token)) {
            return "integerConstant";
        }
        if (token.contains("\"")) {
            return "StringConstant";
        } else {
            return "identifier";
        }
    }

    private String getXmlUnit(String type, String token) {
        switch (type) {
            case "symbol":
                if (token.equals("<")) {
                    token = "&lt";
                }
                if (token.equals(">")) {
                    token = "&gt";
                }
                if (token.equals("&")) {
                    token =  "&amp";
                }
                return "<symbol> " + token + " </symbol>";
            case "keyword":
                return "<keyword> " + token + " </keyword>";
            case "identifier":
                return "<identifier> " + token + " </identifier>";
            case "integerConstant":
                return "<integerConstant> " + token + " </integerConstant>";
            case "StringConstant":
                return "<stringConstant> " + token + " </stringConstant>";
        }
        System.out.println(type + ":" + token);
        return "error";
    }

    public static void main(String[] args) {
        JackTokenizer jk = new JackTokenizer("C:\\Users\\流川枫\\Downloads\\nand2tetris\\projects\\10\\ExpressionLessSquare\\Main.jack");

        try {
            System.out.println("<tokens>");

            while (true) {
                String line = jk.nextLine();
                if (line == null) {
                    break;
                }

                ListIterator<String> tokens = jk.getTokens(line);
                while (tokens.hasNext()) {
                    String token = tokens.next();
                    String type = jk.tokenType(token);
                    String xmlUnit = jk.getXmlUnit(type, token);
                    System.out.println(xmlUnit);
                }
            }
            System.out.println("</tokens>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
