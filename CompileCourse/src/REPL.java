import node.ASTNode;
import node.Token;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class REPL {
    /**
     * 实现一个简单的REPL
     * 运行后在命令行中 输入 源代码
     * 输出 词法单位，语法树，
     * 比如：运行程序后，可以直接把 in.txt 或者 测试文件夹 里面的源代码拷贝到命令行，可以观察结果
     */
    public static void main(String[] args) {
        int bracketNum = 0;
        System.out.println("parser to generate AST tree !");

        Tokenizer tokenizer = new Tokenizer();
        Parser parser = new Parser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        StringBuilder scriptText = new StringBuilder();
        System.out.println("Please input a Class source code");
        System.out.print("\n>");   //提示符

        while (true) {
            try {
                String line = reader.readLine().trim();
                if (line.equals("exit();")) {
                    System.out.println("good bye!");
                    break;
                }
                scriptText.append(line).append("\n");
                if (line.contains("{")) {
                    bracketNum++;
                }
                if (line.contains("}")) {
                    bracketNum--;
                    if (bracketNum == 0) {
                        TokenReader tokens = tokenizer.tokenize(scriptText.toString());
                        System.out.println("tokens are:");
                        for (Token token: tokens.tokens) {
                            System.out.println(token);
                        }
                        ASTNode tree = parser.parse(tokens);
                        System.out.println("the ast is ");
                        parser.dumpAST(tree, "");

                        System.out.print("\n>");   //提示符

                        scriptText = new StringBuilder();
                    }
                }

            } catch (Exception e) {
                // e.printStackTrace();

                System.out.println(e.getLocalizedMessage());
                System.out.print("\n>");   //提示符
                scriptText = new StringBuilder();
            }
        }
    }

}
