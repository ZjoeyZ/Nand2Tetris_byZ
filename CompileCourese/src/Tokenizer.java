import node.Token;
import type.TokenType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * 分词类
 */
public class Tokenizer {
    protected String inputPath;
    protected String outputPath;
    protected char[] chars;
    protected int charsLen;
    protected int pointer = 0;
    protected PrintWriter writer;

    protected HashMap<String, TokenType> map = new HashMap<String, TokenType>() {{
        put("class", TokenType.Class);
        put("method", TokenType.Method);
        put("constructor", TokenType.Constructor);
        put("function", TokenType.Function);
        put("int", TokenType.Int);
        put("boolean", TokenType.Boolean);
        put("char", TokenType.Char);
        put("void", TokenType.Void);
        put("var", TokenType.Var);
        put("static", TokenType.Static);
        put("field", TokenType.Field);
        put("let", TokenType.Let);
        put("do", TokenType.Do);
        put("if", TokenType.If);
        put("else", TokenType.Else);
        put("while", TokenType.While);
        put("return", TokenType.Return);
        put("true", TokenType.True);
        put("false", TokenType.False);
        put("null", TokenType.Null);
        put("this", TokenType.This);
        put("(", TokenType.LeftParen);
        put(")", TokenType.RightParen);
        put("[", TokenType.LeftBracket);
        put("]", TokenType.RightBracket);
        put("{", TokenType.LeftCurly);
        put("}", TokenType.RightCurly);
        put(",", TokenType.Comma);
        put(";", TokenType.SemiColon);
        put("=", TokenType.Assignment);
        put(".", TokenType.Dot);
        put("+", TokenType.Plus);
        put("-", TokenType.Minus);
        put("*", TokenType.MUL);
        put("/", TokenType.DIV);
        put("&", TokenType.And);
        put("|", TokenType.Or);
        put("~", TokenType.Tilde);
        put("<", TokenType.LT);
        put(">", TokenType.GT);
        put(">=", TokenType.LE);
        put("<=", TokenType.LE);
        put("!=", TokenType.NEQ);
        put("==", TokenType.EQ);
    }};
    protected final TokenType Identifier = TokenType.Identifier;
    protected final TokenType IntegerConstant = TokenType.IntLiteral;
    protected final TokenType StringConstant = TokenType.StringLiteral;

    /**
     * 程序入口
     * 第一个参数：输入文件路径
     * 第二个参数：输出文件路径（默认在 CompileCoureses 项目下）
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length >= 2) {
            String inputPath = args[0];
            String outputPath = args[1];
            Tokenizer tokenizer = new Tokenizer(inputPath, outputPath);
            TokenReader reader =  tokenizer.tokenize();
            for (Token token : reader.tokens) {
                tokenizer.writer.write(token.toString());
                System.out.println(token);
            }
        } else {
            String inputPath = args[0];
            Tokenizer tokenizer = new Tokenizer(inputPath);
            TokenReader reader =  tokenizer.tokenize();
            for (Token token : reader.tokens) {
                tokenizer.writer.write(token.toString());
                System.out.println(token);
            }
        }

    }

    public Tokenizer(String in, String out) throws FileNotFoundException {
        inputPath = in;
        outputPath = out;
        setChars();
        setWriter();
    }

    public Tokenizer(String in) throws FileNotFoundException {
        inputPath = in;
        outputPath = "out.txt";
        setChars();
        setWriter();
    }

    /**
     * 打开文件，接收字符串，字符串转 字符数组（全局变量）
     *
     * @throws FileNotFoundException
     */
    protected void setChars() throws FileNotFoundException {
        File file = new File(inputPath);
        assert (file.exists()) : "文件不存在";
        // 创建 Scanner 读取全部字符
        BufferedInputStream bis = (new BufferedInputStream(new FileInputStream(file)));
        Scanner scanner = new Scanner(new BufferedInputStream(bis), "UTF-8");
        String inputText = scanner.useDelimiter(Pattern.compile("\\A")).next();
        chars = inputText.toCharArray();
        charsLen = chars.length;
    }

    /**
     * 打开输出文件，生成输出流（全局变量）
     */
    protected void setWriter() {
        try {
            OutputStream os = new FileOutputStream(outputPath);
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            writer = new PrintWriter(osw, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected TokenType KeywordOrIdentifier(String lexeme) {
        if (map.containsKey(lexeme)) {
            return map.get(lexeme);
        }
        return Identifier;
    }

    protected boolean isLetter(char currentChar) {
        if (currentChar >= 'a' && currentChar <= 'z') {
            return true;
        }
        return currentChar >= 'A' && currentChar <= 'Z';
    }

    protected boolean isDigit(char currentChar) {
        return currentChar >= '0' && currentChar <= '9';
    }

    protected Token getToken() {
        // lexemeSB 用于收集char构造 lexeme
        StringBuilder lexemeSB = new StringBuilder();

        // 跳过空格
        while (chars[pointer] == ' ' || chars[pointer] == '\n'
                || chars[pointer] == '\r' || chars[pointer] == '\t') {
            pointer++;
            if (pointer >= charsLen) {
                return null;
            }
        }

        // 生成标识符，或者关键字的Token
        if (isLetter(chars[pointer]) || chars[pointer] == '_') {
            while (isLetter(chars[pointer]) || isDigit(chars[pointer])
                    || chars[pointer] == '_') {
                lexemeSB.append(chars[pointer]);
                pointer++;
            }
            return new Token(lexemeSB.toString(), KeywordOrIdentifier(lexemeSB.toString()));
        }

        // 生成整数常量的Token
        if (isDigit(chars[pointer])) {
            while (isDigit(chars[pointer])) {
                lexemeSB.append(chars[pointer]);
                pointer++;
            }
            return new Token(lexemeSB.toString(), IntegerConstant);
        }

        // 生成字符串的Token
        if (chars[pointer] == '\"') {
            lexemeSB.append(chars[pointer]);
            pointer++;
            while (chars[pointer] != '\"') {
                lexemeSB.append(chars[pointer]);
                pointer++;
            }
            lexemeSB.append(chars[pointer]);
            pointer++;
            return new Token(lexemeSB.toString(), StringConstant);
        }

        // 跳过注释 //, /* */ 或者 生成 / 的Token
        if (chars[pointer] == '/') {
            // 下一个字符
            pointer++;

            // 不是注释，只是普通的整除符号
            if (chars[pointer] != '/' && chars[pointer] != '*') {
                return new Token("/", map.get("/"));
            }

            // 是单行注释
            if (chars[pointer] == '/') {
                while (chars[pointer] != '\n') {
                    pointer++;
                }
                pointer++;
                return null;
            }

            // 是多行注释
            if (chars[pointer] == '*') {
                pointer++;
                while (!(chars[pointer] == '*' && chars[pointer + 1] == '/')) {
                    pointer++;
                }
                pointer++;
                pointer++;
            }
            return null;
        }

        if (chars[pointer] == '>') {
            if (chars[pointer + 1] == '=') {
                pointer += 2;
                return new Token(">=", map.get(">="));
            }
            pointer++;
            return new Token(">", map.get(">"));
        }

        if (chars[pointer] == '<') {
            if (chars[pointer + 1] == '=') {
                pointer += 2;
                return new Token("<=", map.get("<="));
            }
            pointer++;
            return new Token("<", map.get("<"));
        }

        if (chars[pointer] == '=') {
            if (chars[pointer + 1] == '=') {
                pointer += 2;
                return new Token("==", map.get("=="));
            }
            pointer++;
            return new Token("=", map.get("="));
        }

        if (chars[pointer] == '!') {
            if (chars[pointer + 1] == '=') {
                pointer += 2;
                return new Token("!=", map.get("!="));
            }
            pointer++;
            return new Token("!", map.get("!"));
        }

        // 处理其他单个字符
        String lexeme = chars[pointer] + "";
        pointer++;
        return new Token(lexeme, map.get(lexeme));
    }

    /**
     * 返回一个 解析输入流，生成 tokens，包装成 TokenReader 返回
     * 想·@return TokenReader
     */
    public TokenReader tokenize() {
        ArrayList<Token> tokens = new ArrayList<>();
        while(pointer < charsLen) {
            Token token = getToken();
            if (token != null) {
                tokens.add(token);
            }
        }
        System.out.println(tokens);
        return new TokenReader(tokens);
    }



}
