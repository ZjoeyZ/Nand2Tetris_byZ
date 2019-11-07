import jdk.nashorn.internal.runtime.arrays.ArrayLikeIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.cert.TrustAnchor;
import java.util.*;

import static sun.management.Agent.error;

public class JackParser {
    private BufferedReader br;
    private ArrayList<String> files = new ArrayList<>();
    private int fileIdx = 0;

    private String line = null;
    private ListIterator<String> tokens = new ArrayList<String>().listIterator();
    private String token = null;
    private String type = null;
    private String xmlUnit = null;
    private String blankSpace = "  ";

    private static HashMap<String, String> keyWords = new HashMap<String, String>() {{
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
    }};
    private static HashMap<String, String> symbols = new HashMap<String, String>() {{
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

    public JackParser(String path) {
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
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void close() {
        try {
            if (br != null) {
                br.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String nextLine() throws IOException {
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
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith("*")) {
                continue;
            }
            return line;
        }
    }


    public static boolean isNumeric(String str) {
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            //异常 说明包含非数字。
            return false;
        }
        return true;
    }

    public ListIterator<String> getTokens() {
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

        String[] strings = line.split("\\s+");
        ArrayList<String> tokens = new ArrayList<>(Arrays.asList(strings));

        if (subString != null) {
            tokens.set(tokens.indexOf("\""), subString);
        }

        return tokens.listIterator();
    }

    private boolean hasMoreTokens() throws IOException {
        if (tokens.hasNext()) {
            return true;
        } else {
            line = this.nextLine();
            if (line == null) {
                return false;
            } else {
                tokens = this.getTokens();
                return true;
            }
        }
    }

    public String tokenType(String token) {
        if (keyWords.containsKey(token)) {
            return "keyword";
        }
        if (symbols.containsKey(token)) {
            return "symbol";
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

    private void advance() throws IOException {
        if (hasMoreTokens()) {
            token = tokens.next();
            type = tokenType(token);
            updateXmlUnit();
        } else {
            throw new IllegalStateException("No more tokens");
        }
    }

    public void updateXmlUnit() {
        switch (type) {
            case "symbol":
                if ("<".equals(token)) {
                    token = "&lt;";
                }
                if (">".equals(token)) {
                    token = "&gt;";
                }
                if ("&".equals(token)) {
                    token = "&amp;";
                }
                xmlUnit = "<symbol> " + token + " </symbol>";
                break;
            case "keyword":
                xmlUnit = "<keyword> " + token + " </keyword>";
                break;
            case "identifier":
                xmlUnit = "<identifier> " + token + " </identifier>";
                break;
            case "integerConstant":
                xmlUnit = "<integerConstant> " + token + " </integerConstant>";
                break;
            case "StringConstant":
                xmlUnit = "<stringConstant> " + token.substring(1, token.length() - 1) + " </stringConstant>";
                break;
            default:
                System.out.println(type + ":" + token);
                xmlUnit = "error";
        }
    }

    public static void main(String[] args) {
        JackParser jk = new JackParser("C:\\Users\\流川枫\\Downloads\\nand2tetris\\projects\\10\\Square\\Square.jack");

        try {
            // BufferedWriter out = new BufferedWriter(new FileWriter("C:\\Users\\流川枫\\Downloads\\nand2tetris\\projects\\10\\myanwser\\");
            jk.compileClass();

        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    private void compileClass() throws IOException {
        System.out.println("<class>");

        advance();
        if (!"keyword".equals(type)) {
            error("class");
        }
        System.out.println(blankSpace + xmlUnit);

        advance();
        if (!"identifier".equals(type)) {
            error("className");
        }
        System.out.println(blankSpace + xmlUnit);

        advance();
        if (!"{".equals(token)) {
            error("require {");
        }
        System.out.println(blankSpace + xmlUnit);

        compileClassVarDec();
        compileSubroutine();

        advance();
        if (!"}".equals(token)) {
            error("require }");
        }
        System.out.println(blankSpace + xmlUnit);


        System.out.println("</class>");
    }

    private void compileClassVarDec() throws IOException {
        advance();
        if (!"static".equals(token) && !"field".equals(token)) {
            token = tokens.previous();
            return;
        }

        System.out.println(blankSpace + "<classVarDec>");
        blankSpace = blankSpace + "  ";

        while (!";".equals(token) ) {
            System.out.println(blankSpace + xmlUnit);
            advance();
        }

        System.out.println(blankSpace + xmlUnit);

        blankSpace = blankSpace.substring(2);
        System.out.println(blankSpace + "</classVarDec>");

        compileClassVarDec();
    }

    private void compileSubroutine() throws IOException {
        advance();
        if (!"constructor".equals(token) && !"method".equals(token) && "function".equals(token)) {
            tokens.previous();
            return;
        }

        System.out.println(blankSpace + "<subroutineDec>");

        blankSpace = blankSpace + "  ";
        System.out.println(blankSpace + xmlUnit);

        advance();
        System.out.println(blankSpace + xmlUnit);

        advance();
        System.out.println(blankSpace + xmlUnit);

        advance();
        System.out.println(blankSpace + xmlUnit);

        compileParameterList();

        advance();
        System.out.println(blankSpace + xmlUnit);


        blankSpace = blankSpace.substring(2);
        System.out.println(blankSpace + "</subroutineDec>");

        compileClassVarDec();

    }

    private void compileParameterList() throws IOException {
        advance();
        System.out.println(blankSpace + "<parameterList>");
        blankSpace = blankSpace + "  ";

        while (!")".equals(token)) {
            System.out.println(blankSpace + xmlUnit);
            advance();
        }
        tokens.previous();
        blankSpace = blankSpace.substring(2);

        System.out.println(blankSpace + "</parameterList>");

    }
}
