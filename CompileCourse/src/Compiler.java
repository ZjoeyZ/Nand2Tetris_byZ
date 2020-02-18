import node.Token;
import scope.SymbolTable;
import type.TokenType;

import static type.TokenType.*;


public class Compiler {
    private TokenReader tokens;
    private VMWriter writer;
    private SymbolTable outerTable = new SymbolTable();
    private SymbolTable innerTable = new SymbolTable(outerTable);

    private String className;
    private String subroutineName;
    private String subroutineType;
    private String returnType;
    private int labelNum = 0;

    // 9个虚拟内存段
    private static String segConstant = "constant";
    private static String segField = "field";
    private static String segStatic = "static";
    private static String segTemp = "temp";
    private static String segPointer = "pointer";
    private static String segThat = "that";
    private static String segThis = "this";
    private static String segArgument = "argument";
    private static String segLocal = "local";


    /**
     * 程序入口
     * 不从命令行读取参数，而是在 testFilePath 中手动设置输入文件的路径
     */
    public static void main(String[] args) {
        String inPath = "测试文件及结果/CompilerTest/Square/Main.jack";
        String outPath = String.format("%s.vm", inPath.substring(0, inPath.lastIndexOf('.')));
        try {
            // 生成分词器
            Tokenizer tokenizer = new Tokenizer(inPath);
            // 分词结果tokens
            TokenReader tokens = tokenizer.tokenize();
            // 生成 compiler
            Compiler compiler = new Compiler(tokens, outPath);
            // 开始编译
            compiler.compile();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Compiler(TokenReader tokens) {
        this.tokens = tokens;
        this.writer = new VMWriter();
    }

    public Compiler(TokenReader tokens, String filename) {
        this.tokens = tokens;
        this.writer = new VMWriter(filename);
    }


    /**
     * 顺便把编译结果写入输入流
     *
     * @return
     * @throws Exception
     */
    public void compile() throws Exception {
        // 如果第一个 token 值是 class 关键字，开始解析类
        if (tokens.peek().getType() == Class) {
            compileClass();
        } else {
            throw new Exception("\nExpected keyword 'class' at the beginning");
        }
        //writer.close();
    }

    // AST 根节点
    private void compileClass() throws Exception {
        // 第一个子节点 class 关键字
        tokens.read();

        // 第二个子节点 className 标识符
        Token token = tokens.read();

        if (token.getType() == Identifier) {
            className = token.getText();
        } else {
            throw new Exception("\nExpected className");
        }

        // 第三个子节点 { 关键字
        token = tokens.read();
        if (token.getType() != LeftCurly) {
            throw new Exception("\nExpected '{' after class name");
        }

        // 可能还有0个以上个classVarDec 子树
        token = tokens.peek();
        while (token.getType() == Static || token.getType() == Field) {
            compileClassVarDec();
            token = tokens.peek();
        }

        // 可能还有0个以上 subroutineDec 子树
        TokenType type = token.getType();
        while (type == Constructor || type == Function || type == Method) {
            compileSubroutineDec();

            token = tokens.peek();
            type = token.getType();
        }

        // 最后一个子节点 } 关键字
        token = tokens.read();
        if (token.getType() != RightCurly) {
            throw new Exception("\nExpected '}' at the final of class not " + token.getPosition());
        }

        System.out.println("log: compile class file success");
    }

    // AST 第二层
    private void compileClassVarDec() throws Exception {
        String name, type, kind;

        // 第一个子节点 filed 或者 static 关键字
        Token token = tokens.read();
        TokenType tokenType = token.getType();
        switch (tokenType) {
            case Field:
                kind = "field";
                break;
            case Static:
                kind = "static";
                break;
            default:
                throw new Exception("\nExpected field or static before " + token.getPosition());

        }

        // 第二个子节点 int char boolean 关键字 或者 类名 标识符
        token = tokens.read();
        tokenType = token.getType();
        if (tokenType == Identifier || tokenType == Int || tokenType == Char
                || tokenType == Boolean) {
            type = token.getText();
        } else {
            throw new Exception("\nExpected type before " + token.getPosition());
        }

        token = tokens.peek();
        if (token.getType() == LeftBracket) {
            // 是数组类型
            type += "[]";
            tokens.read();// [
            tokens.read();// ]
        }

        // 第三个子节点 varName 标识符
        token = tokens.read();
        tokenType = token.getType();
        if (tokenType == Identifier) {
            name = token.getText();
        } else {
            throw new Exception("\nExpected varName before " + token.getPosition());
        }

        // 向符号表中定义新的变量
        outerTable.Define(name, type, kind);

        // 读取 (, varName)*
        token = tokens.read();
        while (token.getType() != SemiColon) {
            // ,
            if (token.getType() != Comma) {
                throw new Exception("\nExpected , before " + token.getPosition());
            }

            // varName
            token = tokens.read();
            if (token.getType() == Identifier) {
                name = token.getText();
            } else {
                throw new Exception("\nExpected varName before " + token.getPosition());
            }

            outerTable.Define(name, type, kind);

            // 读取下一个字符 ‘，’ 或者 ‘；’
            token = tokens.read();
        }

    }

    // AST 第二层
    private void compileSubroutineDec() throws Exception {
        // 清空前一次残留的非本作用域内的变量
        innerTable = new SymbolTable(outerTable);

        // 第一个子节点 constructor 或者 function 或者 method 关键字
        Token token = tokens.read();
        TokenType type = token.getType();
        switch (type) {
            case Function:
                subroutineType = "function";
                break;
            case Method:
                subroutineType = "method";
                // 默认已有一个参数 对象引用
                innerTable.setArgNum(0);
                break;
            case Constructor:
                // 如果是则要增加 分配内存
                subroutineType = "constructor";
                break;
            default:
                throw new Exception("\nexpected subroutineType before " + token.getPosition());
        }

        // 第二个子节点 void 或者 type
        token = tokens.read();
        type = token.getType();
        if (type == Identifier || type == Int || type == Char
                || type == Boolean || type == Void) {
            returnType = token.getText();
        } else {
            throw new Exception("\nExpected return type or void before " + token.getPosition());
        }


        // 第二个子节点 subroutineName
        token = tokens.read();
        if (token.getType() == Identifier) {
            subroutineName = token.getText();
        } else {
            throw new Exception("\nExpected subroutineName before " + token.getPosition());
        }

        // 第四个子节点 '('
        token = tokens.read();
        if (token.getType() != LeftParen) {
            throw new Exception("\nExpected left parenthesis before " + token.getPosition());
        }

        // 可能的 第五个子节点 'parameterList'
        token = tokens.peek();
        if (token.getType() != RightParen) {
            compileParameterList();
        }

        // 第六个子节点 ')'
        token = tokens.read();
        if (token.getType() != RightParen) {
            throw new Exception("\nExpected right parenthesis, before " + token.getPosition());

        }


        compileSubroutineBody();

    }

    // AST 第三层
    private void compileParameterList() throws Exception {


        // 第一个子节点 int char boolean 关键字 或者 类名 标识符
        String type = tokens.read().getText();
        String argName;
        // 第三个子节点 varName 标识符
        Token token = tokens.read();
        TokenType tokenType = token.getType();
        if (tokenType == Identifier) {
            argName = token.getText();
        } else {
            throw new Exception("\nExpected argName before " + token.getPosition());
        }

        // 向符号表中定义新的变量
        innerTable.Define(argName, type, "argument");

        // 读取 (, type argName)*
        token = tokens.peek();
        while (token.getType() != RightParen) {
            // ,
            token = tokens.read();
            if (token.getType() != Comma) {
                throw new Exception("\nExpected , before " + token.getPosition());
            }
            // type
            token = tokens.read();
            tokenType = token.getType();
            if (tokenType == Identifier || tokenType == Int || tokenType == Char
                    || tokenType == Boolean) {
                type = token.getText();
            } else {
                throw new Exception("\nExpected type before " + token.getPosition());
            }

            // argName
            token = tokens.read();
            if (token.getType() == Identifier) {
                argName = token.getText();
            } else {
                throw new Exception("\nExpected varName before " + token.getPosition());
            }

            innerTable.Define(argName, type, "argument");
            token = tokens.peek();
        }


    }

    // AST第三层方法体
    private void compileSubroutineBody() throws Exception {
        // 第一个结点 {
        Token token = tokens.read();
        if (token.getType() != LeftCurly) {
            throw new Exception("\nSubroutineBody: Expected left curly, before " + token.getPosition());
        }

        // o个以上 varDec
        token = tokens.peek();
        while (token.getType() == Var) {
            compileVarDec();
            token = tokens.peek();
        }

        // 已知子程序名 和子程序局部变量的数量，开始写子程序的定义
        writer.writeFunction(className + "." + subroutineName, innerTable.varCount("local"));

        // 如果是构造函数最开始要先分配空间，初始化this
        if ("constructor".equals(subroutineType)) {
            writer.writeComment("分配内存 ->");
            writer.writePush(segConstant, outerTable.varCount("field"));
            writer.writeCall("Memory.alloc", 1);
            writer.writePop(segPointer, 0);
        }

        // 类方法要初始化堆指针 this，指向对象
        if ("method".equals(subroutineType)) {
            writer.writePush("argument", 0);
            writer.writePop("pointer", 0);
        }

        // 0个以上语句
        compileStatements();

        // 最后一个结点 }
        token = tokens.read();
        if (token.getType() != RightCurly) {
            throw new Exception("\nSubroutineBody: Expected right curly, before " + token.getPosition());
        }


    }

    // AST 第四层 方法体中的局部变量声明
    private void compileVarDec() throws Exception {
        // 第一个结点 Var
        String locName = tokens.read().getText();
        String locType;
        // 第二个节点 type
        Token token = tokens.read();
        TokenType type = token.getType();
        if (type == Identifier || type == Int || type == Char
                || type == Boolean) {
            locType = token.getText();
        } else {
            throw new Exception("\nVarDec: Expected type before " + token.getPosition());
        }

        // TODO jack 语言暂时不支持使用后面加 [] 的方式定义数组，而是使用 Array 类。
        token = tokens.peek();
        if (token.getType() == LeftBracket) {
            // 是数组类型
            locType += "[]";
            tokens.read();// [
            tokens.read();// ]
        }

        // 第三个结点 varName
        token = tokens.read();
        if (token.getType() == Identifier) {
            locName = token.getText();
        } else {
            throw new Exception("\nVarDec: Expected varName before " + token.getPosition());
        }

        innerTable.Define(locName, locType, segLocal);

        // (, varName)*
        token = tokens.read();
        while (token.getType() != SemiColon) {
            // ,
            if (token.getType() != Comma) {
                throw new Exception("\nExpected ; before " + token.getPosition());
            }

            // varName
            token = tokens.read();
            if (token.getType() == Identifier) {
                locName = token.getText();
            } else {
                throw new Exception("\nExpected varName before " + token.getPosition());
            }
            innerTable.Define(locName, locType, segLocal);
            // 末尾符号 ;
            token = tokens.read();
        }

    }

    private void compileStatements() throws Exception {
        Token token = tokens.peek();
        while (token.getType() != RightCurly) {
            switch (token.getType()) {
                case Let:
                    compileLetStatement();
                    break;
                case If:
                    compileIfStatement();
                    break;
                case While:
                    compileWhileStatement();
                    break;
                case Do:
                    compileDoStatement();
                    break;
                case Return:
                    compileReturnStatement();
                    break;
                default:
                    throw new Exception("\nif statement: Expected let, if, do, while or return before " + token.getPosition());
            }
            token = tokens.peek();
        }
    }

    // AST 第四层 方法体中的5种语句
    private void compileReturnStatement() throws Exception {
        writer.writeComment("return 语句 ->");
        // 第一个子节点 return 关键字
        Token token = tokens.read();

        if ("void".equals(returnType)) {
            writer.writePush(segConstant, 0);
            // 希望是最后一个子节点 ;
            token = tokens.peek();
            if (token.getType() != SemiColon) {
                System.out.println("log: Warning, return type should be void not " + token.getPosition());
            }
        }

        if (tokens.peek().getType() != SemiColon) {
            // 非 void 有第二个子节点 expression
            compileExpression();
        }
        writer.writeReturn();
        // 最后一个子节点 ;
        token = tokens.read();
        if (token.getType() != SemiColon) {
            throw new Exception("\nreturn statement error : expected ';'  before " + token.getPosition());
        }
    }

    private void compileDoStatement() throws Exception {
        writer.writeComment("do 语句 ->");

        // 第一个子节点 do 关键字
        tokens.read();

        String name;
        boolean methodCall = false;

        Token token = tokens.read();
        if (token.getType() == Identifier) {
            name = token.getText();
        } else {
            throw new Exception("\ndo statement error :  need a identifier before " + token.getPosition());
        }

        token = tokens.peek();
        // subroutineCall 剩余结点
        if (token.getType() == LeftParen || token.getType() == Dot) {

            // ( 则进入 (expressionList) 分支
            // 说明是实例自身的方法的直接调用
            if (token.getType() == LeftParen) {

                subroutineName = className + '.' + name;

                // (
                token = tokens.read();
                writer.writePush(segPointer, 0);
                int nArgs = 0;
                if (tokens.peek().getType() != RightParen) {
                    nArgs = compileExpressionList();
                }
                // 还有第一个参数 this 要算上
                nArgs += 1;

                // ) 结束
                token = tokens.read();
                if (token.getType() == RightParen) {
                    // 方法调用
                    writer.writeCall(subroutineName, nArgs);
                } else {
                    throw new Exception("\nsubroutineCall error : expect ')' before " + token.getPosition());
                }
            } else {
                // . 则进入 className|varName . subroutineName(expressionList) 分支
                // .
                tokens.read();

                token = tokens.read();
                if (token.getType() == Identifier) {
                    if (innerTable.contains(name)) {
                        // 如果是 varName， 则是 Method,第一个参数是this
                        methodCall = true;
                        subroutineName = innerTable.typeOf(name) + "." + token.getText();
                    } else {
                        // 如果之前是 className， 则是 Function
                        subroutineName = name + "." + token.getText();
                    }
                } else {
                    throw new Exception("\nsubroutineCall error : expect subroutineName before " + token.getPosition());
                }

                // (
                token = tokens.read();
                if (token.getType() != LeftParen) {
                    throw new Exception("\nsubroutineCall error : expect ( before " + token.getPosition());
                }

                int nArgs = 0;

                if (methodCall) {
                    String kind = innerTable.kindOf(name);
                    if ("field".equals(kind)) {
                        kind = segThis;
                    }
                    writer.writePush(kind, innerTable.indexOf(name));
                }
                // expressionList
                if (tokens.peek().getType() != RightParen) {
                    nArgs = compileExpressionList();
                }
                if (methodCall) {
                    nArgs += 1;
                }
                // ) 结束
                token = tokens.read();
                if (token.getType() == RightParen) {
                    writer.writeCall(subroutineName, nArgs);
                    // do 完之后的返回一没用有被使用所以pop，不然会发生堆栈溢出，stackOverflow
                } else {
                    throw new Exception("\nsubroutineCall error : expect ')' before " + token.getPosition());
                }
            }
        }

        writer.writePop(segTemp, 0);

        // 最后一个子节点 ;
        token = tokens.read();
        if (token.getType() != SemiColon) {
            throw new Exception("\nDo statement error : expected ; at final before " + token.getPosition());
        }
    }

    private void compileWhileStatement() throws Exception {
        writer.writeComment("while 语句 ->");

        // 第一个子节点 while 关键字
        tokens.read();

        int l1 = labelNum;
        labelNum += 1;
        int l2 = labelNum;
        labelNum += 1;

        writer.writeLabel(l1);

        // (
        Token token = tokens.read();
        if (token.getType() != LeftParen) {
            throw new Exception("\nwhile statement error : expected ( before " + token.getPosition());
        }
        compileExpression();
        // )
        token = tokens.read();
        if (token.getType() != RightParen) {
            throw new Exception("\nwhile statement error : expected ) before " + token.getPosition());
        }

        writer.writeArithmetic("not");
        writer.writeIf(l2);

        // {
        token = tokens.read();
        if (token.getType() != LeftCurly) {
            throw new Exception("\nwhile statement error : expected { before " + token.getPosition());
        }
        // 0个以上语句
        compileStatements();
        // }
        token = tokens.read();
        if (token.getType() != RightCurly) {
            throw new Exception("\nwhile statement error : expected } before " + token.getPosition());
        }

        writer.writeGoto(l1);
        writer.writeLabel(l2);
    }

    private void compileIfStatement() throws Exception {
        writer.writeComment("if 语句 ->");

        int l1 = labelNum;
        labelNum += 1;
        int l2 = labelNum;
        labelNum += 1;

        // 第一个子节点 if 关键字
        tokens.read();

        // (
        Token token = tokens.read();
        if (token.getType() != LeftParen) {
            throw new Exception("\nif statement error : expected ( before " + token.getPosition());
        }
        // condition
        compileExpression();
        // )
        token = tokens.read();
        if (token.getType() != RightParen) {
            throw new Exception("\nif statement error : expected ) before " + token.getPosition());
        }

        writer.writeArithmetic("not");
        writer.writeIf(l1);

        // {
        token = tokens.read();
        if (token.getType() != LeftCurly) {
            throw new Exception("\nif statement error : expected { before " + token.getPosition());
        }
        // 0个以上语句
        compileStatements();
        // }
        token = tokens.read();
        if (token.getType() != RightCurly) {
            throw new Exception("\nwhile statement error : expected } before " + token.getPosition());
        }

        writer.writeGoto(l2);
        writer.writeLabel(l1);

        // else {statement*}
        token = tokens.peek();
        if (token.getType() == Else) {
            // else
            tokens.read();

            // {
            token = tokens.read();
            if (token.getType() != LeftCurly) {
                throw new Exception("\nif statement error : expected { before " + token.getPosition());
            }
            // 0个以上语句
            compileStatements();
            // }
            token = tokens.read();
            if (token.getType() != RightCurly) {
                throw new Exception("\nwhile statement error : expected } before " + token.getPosition());
            }
        }

        writer.writeLabel(l2);
    }

    private void compileLetStatement() throws Exception {
        writer.writeComment("let 赋值语句 ->");

        // varName
        String varName;
        String kind;
        int index;

        // 是否是引用变量, 无用
        // boolean isRef = false;

        // 是否是数组
        boolean isArr = false;
        // 是否是 构造函数中的属性成员赋值
        boolean isField = false;

        // 第一个子节点 let 关键字
        tokens.read();

        // 第二个子节点 名
        Token token = tokens.read();
        if (token.getType() != Identifier) {
            throw new Exception("\nlet statement error : expected varName before " + token.getPosition());
        }

        varName = token.getText();
        kind = innerTable.kindOf(varName);
        index = innerTable.indexOf(varName);
        // 盘算是不是成员变量
        if ("field".equals(kind)) {
            isField = true;
        }

        token = tokens.peek();

        // 可能是 有 [ expression ]
        if (token.getType() == LeftBracket) {
            //判断语义是否正确
            if ("Array".equals(innerTable.typeOf(varName))) {
                isArr = true;
            } else {
                throw new Exception("\nlet statement error : expected an Array instance before " + token.getPosition());
            }

            writer.writeComment("生成数组元素指针暂存在栈里");
            if (isField) {
                // 对象成员是
                writer.writePush(segThis, index);
            } else {
                // 参数、局部、静态变量是 数组
                writer.writePush(kind, index);
            }

            // [
            tokens.read();
            // expression
            compileExpression();
            // ]
            token = tokens.read();
            if (token.getType() != RightBracket) {
                throw new Exception("\nlet statement error : expected ] before " + token.getPosition());
            }

            // 数组类型
            // 引用值进栈然后，+ expression 值
            writer.writeArithmetic("add");
            // that 指向这个元素, ！错误，that 可能被后面的expression操作重置
            // writer.writePop(segPointer, 1);
        }

        // = expression ;
        token = tokens.read();
        if (token.getType() != Assignment) {
            throw new Exception("\nlet statement error : expected = before " + token.getPosition());
        }
        compileExpression();
        token = tokens.read();
        if (token.getType() != SemiColon) {
            throw new Exception("\nlet statement error : expected ; before " + token.getPosition());
        }


        if (isArr) {
            // 数组 参数、变量、对象成员
            // 暂存计算结果
            writer.writePop(segTemp, 0);
            // 元素指针 that
            writer.writePop(segPointer, 1);
            writer.writePush(segTemp, 0);
            writer.writePop(segThat, 0);
        } else if (isField) {
            // 普通对象成员
            writer.writePop(segThis, index);
        } else {
            // 普通参数、变量
            writer.writePop(kind, index);
        }


    }

    // AST 第5层 语句中的表达式
    private void compileExpression() throws Exception {
        // 第一个结点一是一个 term
        compileTerm();


        // 0个以上的 op term
        Token token = tokens.peek();
        TokenType type = token.getType();
        while (type == Plus || type == Minus || type == MUL
                || type == DIV || type == And || type == Or
                || type == LT || type == GT || type == EQ
                || type == LE || type == GE || type == Assignment) {
            // 加入 ope 子节点
            token = tokens.read();
            type = token.getType();

            // 加入 term 子节点
            compileTerm();

            if (type == Plus) {
                writer.writeArithmetic("add");
            } else if (type == Minus) {
                writer.writeArithmetic("sub");
            } else if (type == MUL) {
                writer.writeCall("Math.multiply", 2);
            } else if (type == DIV) {
                writer.writeCall("Math.divide", 2);
            } else if (type == And) {
                writer.writeArithmetic("and");
            } else if (type == Or) {
                writer.writeArithmetic("or");
            } else if (type == LT) {
                writer.writeArithmetic("lt");
            } else if (type == GT) {
                writer.writeArithmetic("gt");
            } else if (type == EQ || type == Assignment) {
                writer.writeArithmetic("eq");
            } else {
                throw new Exception("暂不支持 <= 和 >=");
            }

            token = tokens.peek();
            type = token.getType();
        }

    }

    // AST 第六层 表达式中的项
    private void compileTerm() throws Exception {
        Token token = tokens.read();
        TokenType type = token.getType();


        // 只有一个子节点 常量
        if (type == False || type == Null) {
            writer.writePush(segConstant, 0);
            return;
        }
        if (type == True) {
            writer.writePush(segConstant, 1);
            writer.writeArithmetic("neg");
            return;
        }
        if (type == IntLiteral) {
            writer.writePush(segConstant, Integer.parseInt(token.getText()));
            return;
        }
        if (type == StringLiteral) {
            // 去掉两边 ""，size 为结束符 + 1
            String text = token.getText();
            text = text.substring(1, text.lastIndexOf('\"'));
            char[] chars = text.toCharArray();
            int size = text.length();
            writer.writePush(segConstant, size);
            writer.writeCall("String.new", 1);

            for (char c : chars) {
                writer.writePush(segConstant, c);
                writer.writeCall("String.appendChar", 2);
            }
            return;
        }

        if (type == This) {
            writer.writePush(segPointer, 0);
            return;
        }


        // 只有一个子节点 标识符 或者 、5-6个节点 subroutineCall 或者 三个结点 varName[expression]
        if (type == Identifier) {
            // varName | subroutineName | className
            // 遍历两个字符表找到 varName ，有返回单个变量值，没有说明是 subName 或者是 className
            String name = token.getText();


            // [ , ( , .
            token = tokens.peek();

            // [  expression]
            if (token.getType() == LeftBracket) {
                String kind = innerTable.kindOf(name);
                int index = innerTable.indexOf(name);
                boolean isField = "field".equals(kind);

                writer.writeComment("生成数组元素指针暂存在栈里");
                if (isField) {
                    // 对象成员是
                    writer.writePush(segThis, index);
                } else {
                    // 参数、局部、静态变量是 数组
                    writer.writePush(kind, index);
                }

                // [
                tokens.read();
                // expression
                compileExpression();
                // ]
                token = tokens.read();
                if (token.getType() != RightBracket) {
                    throw new Exception("\nlet statement error : expected ] before " + token.getPosition());
                }

                // 计算得到元素指针
                writer.writeArithmetic("add");
                // 元素值进栈
                writer.writePop(segPointer, 1);
                writer.writePush(segThat, 0);
                return;
            }

            // subroutineCall 剩余结点
            if (token.getType() == LeftParen || token.getType() == Dot) {

                // ( 则进入 (expressionList) 分支
                // 说明是实例自身的方法的直接调用
                if (token.getType() == LeftParen) {
                    subroutineName = className + '.' + name;
                    // (
                    token = tokens.read();
                    // 第一个参数 this
                    writer.writePush(segPointer, 0);
                    int nArgs = 1;
                    if (tokens.peek().getType() != RightParen) {
                        nArgs = compileExpressionList();
                    }
                    nArgs += 1;

                    // ) 结束
                    token = tokens.read();
                    if (token.getType() == RightParen) {
                        // 实例方法调用
                        writer.writeCall(subroutineName, nArgs);
                        return;
                    } else {
                        throw new Exception("\nsubroutineCall error : expect ')' before " + token.getPosition());
                    }
                }

                // . 则进入 className|varName . subroutineName(expressionList) 分支
                if (token.getType() == Dot) {
                    boolean methodCall = false;
                    // .
                    token = tokens.read();

                    token = tokens.read();
                    if (token.getType() == Identifier) {
                        if (innerTable.contains(name)) {
                            // 如果是 varName， 则是 Method
                            methodCall = true;
                            subroutineName = innerTable.typeOf(name) + "." + token.getText();
                        } else {
                            // 如果之前是 className， 则是 Functionp
                            subroutineName = name + "." + token.getText();
                        }
                    } else {
                        throw new Exception("\nsubroutineCall error : expect subroutineName before " + token.getPosition());
                    }

                    // (
                    token = tokens.read();
                    if (token.getType() != LeftParen) {
                        throw new Exception("\nsubroutineCall error : expect ( before " + token.getPosition());
                    }

                    int nArgs = 0;

                    if (methodCall) {
                        String kind = innerTable.kindOf(name);
                        if ("field".equals(kind)) {
                            kind = segThis;
                        }
                        writer.writePush(kind, innerTable.indexOf(name));
                    }
                    // expressionList
                    if (tokens.peek().getType() != RightParen) {
                        nArgs = compileExpressionList();
                    }
                    if (methodCall) {
                        nArgs += 1;
                    }

                    // ) 结束
                    token = tokens.read();
                    if (token.getType() == RightParen) {
                        writer.writeCall(subroutineName, nArgs);
                        return;
                    } else {
                        throw new Exception("\nsubroutineCall error : expect ')' before " + token.getPosition());
                    }
                }
            }
            // 看来只有一个 varName
            String kind = innerTable.kindOf(name);
            if ("field".equals(kind)) {
                kind = "this";
            };
            writer.writePush(kind, innerTable.indexOf(name));
            return;
        }

        // 有三个子节点 '(' expression ')'
        if (type == LeftParen) {
            // expression
            compileExpression();

            // )
            token = tokens.read();
            if (token.getType() != RightParen) {
                throw new Exception("\nterm error : expect ')' before " + token.getPosition());
            }
            return;
        }

        // 有两个个节点 unaryOP term
        if (type == Minus || type == Tilde) {

            // term
            compileTerm();

            // unaryOP
            if (type == Minus) {
                writer.writeArithmetic("neg");
            } else {
                writer.writeArithmetic("not");
            }
            return;
        }


        throw new Exception("\ncan't resolve term because of " + token.getPosition());
    }

    // 第七层 表达式中的项的方法调用中的表达式列表
    private int compileExpressionList() throws Exception {
        int nArgs = 1;
        // expression
        compileExpression();


        while (tokens.peek().getType() == Comma) {
            nArgs = nArgs + 1;
            tokens.read();
            compileExpression();
        }
        return nArgs;
    }

}

