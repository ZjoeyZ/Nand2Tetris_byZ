import node.*;
import type.NTokenType;
import type.TokenType;


public class Parser {
    TokenReader tokens;

    public static void main(String args[]) throws Exception {
        String inputPath = "in.txt";

        Tokenizer tokenizer = new Tokenizer(inputPath);
        TokenReader tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);

        ASTNode tree = parser.parse();

        parser.dumpAST(tree, "");
    }

    void dumpAST(ASTNode node, String indent) {
        System.out.println(indent + node.getType() + " " + node.getText());
        for (ASTNode child : node.getChildren()) {
            dumpAST(child, indent + "\t");
        }
    }

    Parser(TokenReader tokens) {
        this.tokens = tokens;
    }

    /**
     * 返回一颗抽象语法树
     *
     * @return
     * @throws Exception
     */
    public ASTNode parse() throws Exception {
        // 如果第一个 token 值是 class 关键字，开始解析类
        if (tokens.peek().getType() == TokenType.Class) {
            ASTNode rootNode = new NToken(NTokenType.Class, "class");
            compileClass(rootNode);
            return rootNode;
        }
        throw new Exception("Expected keyword 'class' at the beginning");
    }

    // AST 根节点
    private void compileClass(ASTNode parent) throws Exception {
        // 第一个子节点 class 关键字
        parent.addChild(tokens.read());

        // 第二个子节点 className 标识符
        Token token = tokens.read();
        if (token.getType() == TokenType.Identifier) {
            parent.addChild(token);
        } else {
            throw new Exception("Expected className");
        }

        // 第三个子节点 { 关键字
        token = tokens.read();
        if (token.getType() == TokenType.LeftCurly) {
            parent.addChild(token);
        } else {
            throw new Exception("Expected '{' after class name");
        }

        // 可能还有0个以上个classVarDec 子树
        token = tokens.peek();
        while (token.getType() == TokenType.Static || token.getType() == TokenType.Field) {
            ASTNode child = new NToken(NTokenType.classVarDec, "classVarDec");
            parent.addChild(child);
            compileClassVarDec(child);
            token = tokens.peek();
        }

        // 可能还有0个以上 subroutineDec 子树
        TokenType type = token.getType();
        while (type == TokenType.Constructor || type == TokenType.Function || type == TokenType.Method) {
            ASTNode child = new NToken(NTokenType.subroutineDec, "subroutineDec");
            parent.addChild(child);
            compileSubroutineDec(child);

            token = tokens.peek();
            type = token.getType();
        }

        // 最后一个子节点 } 关键字
        token = tokens.read();
        if (token.getType() == TokenType.RightCurly) {
            parent.addChild(token);
        } else {
            throw new Exception("Expected '}' at final");
        }

        System.out.println("compile class file success");

    }

    // AST 第二层
    private void compileClassVarDec(ASTNode parent) throws Exception {
        // 第一个子节点 filed 或者 static 关键字
        parent.addChild(tokens.read());

        // 第二个子节点 int char boolean 关键字 或者 类名 标识符
        Token token = tokens.read();
        TokenType type = token.getType();
        if (type == TokenType.Identifier || type == TokenType.Int || type == TokenType.Char
                || type == TokenType.Boolean) {
            parent.addChild(token);
        } else {
            throw new Exception("Expected type not " + token.getText());
        }

        // 第三个子节点 varName 标识符
        token = tokens.read();
        type = token.getType();
        if (type == TokenType.Identifier) {
            parent.addChild(token);
        } else {
            throw new Exception("Expected varName not " + token.getText());
        }

        // 读取 (, varName)*
        token = tokens.read();
        while (token.getType() != TokenType.SemiColon) {
            // ,
            if (token.getType() == TokenType.Comma) {
                parent.addChild(token);
            } else {
                throw new Exception("Expected , not " + token.getText());
            }


            // varName
            token = tokens.read();
            if (token.getType() == TokenType.Identifier) {
                parent.addChild(token);
            } else {
                throw new Exception("Expected varName not " + token.getText());
            }

            token = tokens.read();
        }

        // 添加末尾符号 ;
        parent.addChild(token);

        System.out.println("compile classVarDec success");

    }

    // AST 第二层
    private void compileSubroutineDec(ASTNode parent) throws Exception {
        // 第一个子节点 constructor 或者 function 或者 method 关键字
        parent.addChild(tokens.read());

        // 第二个子节点 void 或者 type
        Token token = tokens.read();
        TokenType type = token.getType();
        if (type == TokenType.Identifier || type == TokenType.Int || type == TokenType.Char
                || type == TokenType.Boolean || type == TokenType.Void) {
            parent.addChild(token);
        } else {
            throw new Exception("Expected type or void not " + token.getText());
        }

        // 第二个子节点 subroutineName
        token = tokens.read();
        if (token.getType() == TokenType.Identifier) {
            parent.addChild(token);
        } else {
            throw new Exception("Expected subroutineName not " + token.getText());
        }

        // 第四个子节点 '('
        token = tokens.read();
        if (token.getType() == TokenType.LeftParen) {
            parent.addChild(token);
        } else {
            throw new Exception("Expected left parenthesis not " + token.getText());
        }

        // 可能的 第五个子节点 'parameterList'
        token = tokens.peek();
        if (token.getType() != TokenType.RightParen) {
            ASTNode paramChild = new NToken(NTokenType.parameterList, "parameterList");
            compileParameterList(paramChild);
            parent.addChild(paramChild);
        }

        // 第六个子节点 ')'
        token = tokens.read();
        if (token.getType() == TokenType.RightParen) {
            parent.addChild(token);
        } else {
            throw new Exception("Expected right parenthesis, not " + token.getText());
        }

        // 最后一个子节点 subroutineBody
        ASTNode child = new NToken(NTokenType.subroutineBody, "subroutineBody");
        compileSubroutineBody(child);
        parent.addChild(child);

        System.out.println("compile subroutineDec  success");

    }

    // AST 第三层
    private void compileParameterList(ASTNode paramParent) throws Exception {
        // 第一个子节点 int char boolean 关键字 或者 类名 标识符
        paramParent.addChild(tokens.read());

        // 第三个子节点 varName 标识符
        Token token = tokens.read();
        TokenType type = token.getType();
        if (type == TokenType.Identifier) {
            paramParent.addChild(token);
        } else {
            throw new Exception("Expected varName not " + token.getText());
        }

        // 读取 (, type varName)*
        token = tokens.peek();
        while (token.getType() != TokenType.RightParen) {
            // ,
            token = tokens.read();
            if (token.getType() == TokenType.Comma) {
                paramParent.addChild(token);
            } else {
                throw new Exception("Expected , not " + token.getText());
            }
            // type
            token = tokens.read();
            type = token.getType();
            if (type == TokenType.Identifier || type == TokenType.Int || type == TokenType.Char
                    || type == TokenType.Boolean) {
                paramParent.addChild(token);
            } else {
                throw new Exception("Expected type not " + token.getText());
            }

            // varName
            token = tokens.read();
            if (token.getType() == TokenType.Identifier) {
                paramParent.addChild(token);
            } else {
                throw new Exception("Expected varName not " + token.getText());
            }

            token = tokens.peek();
        }

        System.out.println("compile parameterList success");

    }

    // AST第三层方法体
    private void compileSubroutineBody(ASTNode parent) throws Exception {
        // 第一个结点 {
        Token token = tokens.read();
        if (token.getType() == TokenType.LeftCurly) {
            parent.addChild(token);
        } else {
            throw new Exception("SubroutineBody: Expected left curly, not " + token.getText());
        }

        // o个以上 varDec
        token = tokens.peek();
        while (token.getType() == TokenType.Var) {
            ASTNode child = new NToken(NTokenType.varDec, "varDec");
            compileVarDec(child);
            parent.addChild(child);
            token = tokens.peek();
        }

        // 0个以上语句
        token = tokens.peek();
        while (token.getType() != TokenType.RightCurly) {
            ASTNode child;
            switch (token.getType()) {
                case Let:
                    child = new NToken(NTokenType.letStatement, "letStatement");
                    compileLetStatement(child);
                    parent.addChild(child);
                    break;
                case If:
                    child = new NToken(NTokenType.ifStatement, "ifStatement");
                    compileIfStatement(child);
                    parent.addChild(child);
                    break;
                case While:
                    child = new NToken(NTokenType.whileStatement, "whileStatement");
                    compileWhileStatement(child);
                    parent.addChild(child);
                    break;
                case Do:
                    child = new NToken(NTokenType.doStatement, "doStatement");
                    compileDoStatement(child);
                    parent.addChild(child);
                    break;
                case Return:
                    child = new NToken(NTokenType.returnStatement, "returnStatement");
                    compileReturnStatement(child);
                    parent.addChild(child);
                    break;
                default:
                    throw new Exception("statement: Expected let, if, do, while or return not " + token);
            }
            token = tokens.peek();
        }

        // 最后一个结点 }
        token = tokens.read();
        if (token.getType() == TokenType.RightCurly) {
            parent.addChild(token);
        } else {
            throw new Exception("SubroutineBody: Expected right curly, not " + token.getText());
        }

        System.out.println("compile subroutineBody success");

    }

    // AST 第四层 方法体中的局部变量声明
    private void compileVarDec(ASTNode parent) throws Exception {
        // 第一个结点 Var
        parent.addChild(tokens.read());

        // 第二个节点 type
        Token token = tokens.read();
        TokenType type = token.getType();
        if (type == TokenType.Identifier || type == TokenType.Int || type == TokenType.Char
                || type == TokenType.Boolean) {
            parent.addChild(token);
        } else {
            throw new Exception("VarDec: Expected type not " + token.getText());
        }

        // 第三个结点 varName
        token = tokens.read();
        if (token.getType() == TokenType.Identifier) {
            parent.addChild(token);
        } else {
            throw new Exception("VarDec: Expected varName not " + token.getText());
        }

        // (, varName)*
        token = tokens.read();
        while (token.getType() != TokenType.SemiColon) {
            // ,
            if (token.getType() == TokenType.Comma) {
                parent.addChild(token);
            } else {
                throw new Exception("Expected , not " + token.getText());
            }


            // varName
            token = tokens.read();
            if (token.getType() == TokenType.Identifier) {
                parent.addChild(token);
            } else {
                throw new Exception("Expected varName not " + token.getText());
            }

            token = tokens.read();
        }

        // 添加末尾符号 ;
        parent.addChild(token);

    }

    // AST 第四层 方法体中的5种语句
    private void compileReturnStatement(ASTNode parent) throws Exception {
        // 第一个子节点 return 关键字
        parent.addChild(tokens.read());

        // 可能有第二个子节点 expression
        if (tokens.peek().getType() != TokenType.SemiColon) {
            ASTNode child = new NToken(NTokenType.expression, "expression");
            parent.addChild(child);
            compileExpression(child);
        }

        // 最后一个子节点 ;
        Token token = tokens.read();
        if (token.getType() == TokenType.SemiColon) {
            parent.addChild(token);
        } else {
            throw new Exception("return statement: expected ; at final not " + token);
        }
        System.out.println("compile return statement success");

    }

    private void compileDoStatement(ASTNode parent) throws Exception {
        // 第一个子节点 Do 关键字
        parent.addChild(tokens.read());

        Token token = tokens.read();
        if (token.getType() == TokenType.Identifier) {
            parent.addChild(token);
        } else {
            throw new Exception("do statement: subroutineCall need a identifer not " + token);
        }

        token = tokens.peek();
        // ( 则进入 expressionList) 分支
        if (token.getType() == TokenType.LeftParen) {
            // (
            token = tokens.read();
            parent.addChild(token);

            if (tokens.peek().getType() != TokenType.RightParen) {
                ASTNode child = new NToken(NTokenType.expressionList, "expressionList");
                parent.addChild(child);
                compileExpressionList(child);
            }

            // ) 结束
            token = tokens.read();
            if (token.getType() == TokenType.RightParen) {
                parent.addChild(token);
            } else {
                throw new Exception("subroutineCall: expect ')' not " + token);
            }
        }

        // . 则进入 className|varName . subroutineName(expressionList) 分支
        else if (token.getType() == TokenType.Dot) {
            // .
            token = tokens.read();
            parent.addChild(token);

            // subroutineName
            token = tokens.read();
            if (token.getType() == TokenType.Identifier) {
                parent.addChild(token);
            } else {
                throw new Exception("subroutineCall: expect subroutineName not " + token);
            }

            // (
            token = tokens.read();
            if (token.getType() == TokenType.LeftParen) {
                parent.addChild(token);
            } else {
                throw new Exception("subroutineCall: expect ( not " + token);
            }

            // expressionList
            if (tokens.peek().getType() != TokenType.RightParen) {
                ASTNode child = new NToken(NTokenType.expressionList, "expressionList");
                parent.addChild(child);
                compileExpressionList(child);
            }

            // ) 结束
            token = tokens.read();
            if (token.getType() == TokenType.RightParen) {
                parent.addChild(token);
            } else {
                throw new Exception("subroutineCall: expect ')' not " + token);
            }
        }

        // 最后一个子节点 ;
        token = tokens.read();
        if (token.getType() == TokenType.SemiColon) {
            parent.addChild(token);
        } else {
            throw new Exception("Do statement: expected ; at final not " + token);
        }

        System.out.println("compile do statement success");
    }

    private void compileWhileStatement(ASTNode parent) throws Exception {
        // 第一个子节点 while 关键字
        parent.addChild(tokens.read());

        // (
        Token token = tokens.read();
        if (token.getType() == TokenType.LeftParen) {
            parent.addChild(token);
        } else {
            throw new Exception("while statement: expected ( not " + token);
        }

        ASTNode child = new NToken(NTokenType.expression, "expression");
        parent.addChild(child);
        compileExpression(child);

        // )
        token = tokens.read();
        if (token.getType() == TokenType.RightParen) {
            parent.addChild(token);
        } else {
            throw new Exception("while statement: expected ) not " + token);
        }
        // (
        token = tokens.read();
        if (token.getType() == TokenType.LeftCurly) {
            parent.addChild(token);
        } else {
            throw new Exception("while statement: expected { not " + token);
        }

        // 0个以上语句
        token = tokens.peek();
        while (token.getType() != TokenType.RightCurly) {
            switch (token.getType()) {
                case Let:
                    child = new NToken(NTokenType.letStatement, "letStatement");
                    compileLetStatement(child);
                    parent.addChild(child);
                    break;
                case If:
                    child = new NToken(NTokenType.ifStatement, "ifStatement");
                    compileIfStatement(child);
                    parent.addChild(child);
                    break;
                case While:
                    child = new NToken(NTokenType.whileStatement, "whileStatement");
                    compileWhileStatement(child);
                    parent.addChild(child);
                    break;
                case Do:
                    child = new NToken(NTokenType.doStatement, "doStatement");
                    compileDoStatement(child);
                    parent.addChild(child);
                    break;
                case Return:
                    child = new NToken(NTokenType.returnStatement, "returnStatement");
                    compileReturnStatement(child);
                    parent.addChild(child);
                    break;
                default:
                    throw new Exception("statement: Expected let, if, do, while or return not " + token);
            }
            token = tokens.peek();
        }

        // }
        token = tokens.read();
        if (token.getType() == TokenType.RightCurly) {
            parent.addChild(token);
        } else {
            throw new Exception("while statement: expected } not " + token);
        }

        System.out.println("compile while statement finished");
    }

    private void compileIfStatement(ASTNode parent) throws Exception {
        // 第一个子节点 if 关键字
        parent.addChild(tokens.read());

        // (
        Token token = tokens.read();
        if (token.getType() == TokenType.LeftParen) {
            parent.addChild(token);
        } else {
            throw new Exception("if statement: expected ( not " + token);
        }

        ASTNode child = new NToken(NTokenType.expression, "expression");
        parent.addChild(child);
        compileExpression(child);

        // )
        token = tokens.read();
        if (token.getType() == TokenType.RightParen) {
            parent.addChild(token);
        } else {
            throw new Exception("if statement: expected ) not " + token);
        }
        // {
        token = tokens.read();
        if (token.getType() == TokenType.LeftCurly) {
            parent.addChild(token);
        } else {
            throw new Exception("if statement: expected { not " + token);
        }

        // 0个以上语句
        token = tokens.peek();
        while (token.getType() != TokenType.RightCurly) {
            switch (token.getType()) {
                case Let:
                    child = new NToken(NTokenType.letStatement, "letStatement");
                    compileLetStatement(child);
                    parent.addChild(child);
                    break;
                case If:
                    child = new NToken(NTokenType.ifStatement, "ifStatement");
                    compileIfStatement(child);
                    parent.addChild(child);
                    break;
                case While:
                    child = new NToken(NTokenType.whileStatement, "whileStatement");
                    compileWhileStatement(child);
                    parent.addChild(child);
                    break;
                case Do:
                    child = new NToken(NTokenType.doStatement, "doStatement");
                    compileDoStatement(child);
                    parent.addChild(child);
                    break;
                case Return:
                    child = new NToken(NTokenType.returnStatement, "returnStatement");
                    compileReturnStatement(child);
                    parent.addChild(child);
                    break;
                default:
                    throw new Exception("if statement: Expected let, if, do, while or return not " + token);
            }
            token = tokens.peek();
        }

        // }
        token = tokens.read();
        if (token.getType() == TokenType.RightCurly) {
            parent.addChild(token);
        } else {
            throw new Exception("while statement: expected } not " + token);
        }

        // else {statement*}
        token = tokens.peek();
        if(token.getType() == TokenType.Else){
            parent.addChild(tokens.read());

            // {
            token = tokens.read();
            if (token.getType() == TokenType.LeftCurly) {
                parent.addChild(token);
            } else {
                throw new Exception("if statement: expected { not " + token);
            }

            // 0个以上语句
            token = tokens.peek();
            while (token.getType() != TokenType.RightCurly) {
                switch (token.getType()) {
                    case Let:
                        child = new NToken(NTokenType.letStatement, "letStatement");
                        compileLetStatement(child);
                        parent.addChild(child);
                        break;
                    case If:
                        child = new NToken(NTokenType.ifStatement, "ifStatement");
                        compileIfStatement(child);
                        parent.addChild(child);
                        break;
                    case While:
                        child = new NToken(NTokenType.whileStatement, "whileStatement");
                        compileWhileStatement(child);
                        parent.addChild(child);
                        break;
                    case Do:
                        child = new NToken(NTokenType.doStatement, "doStatement");
                        compileDoStatement(child);
                        parent.addChild(child);
                        break;
                    case Return:
                        child = new NToken(NTokenType.returnStatement, "returnStatement");
                        compileReturnStatement(child);
                        parent.addChild(child);
                        break;
                    default:
                        throw new Exception("if statement: Expected let, if, do, while or return not " + token);
                }
                token = tokens.peek();
            }

            // }
            token = tokens.read();
            if (token.getType() == TokenType.RightCurly) {
                parent.addChild(token);
            } else {
                throw new Exception("while statement: expected } not " + token);
            }
        }
        System.out.println("compile if statement finished");
    }

    private void compileLetStatement(ASTNode parent) throws Exception{
        // 第一个子节点 let 关键字
        parent.addChild(tokens.read());

        // varName
        Token token = tokens.read();
        if (token.getType() == TokenType.Identifier) {
            parent.addChild(token);
        } else {
            throw new Exception("let statement: expected varName not " + token);
        }

        token = tokens.read();

        // 可能是 有 [ expression ]
        if (token.getType() == TokenType.LeftBracket) {
            // [
            parent.addChild(token);

            // expression
            ASTNode child = new NToken(NTokenType.expression, "expression");
            parent.addChild(child);
            compileExpression(child);

            // ]
            token = tokens.read();
            if (token.getType() == TokenType.RightBracket) {
                parent.addChild(token);
            } else {
                throw new Exception("let statement: expected ] not " + token);
            }
        }

        // =
        if (token.getType() == TokenType.Assignment) {
            parent.addChild(token);
        } else {
            throw new Exception("let statement: expected = not " + token);
        }

        // expression
        ASTNode child = new NToken(NTokenType.expression, "expression");
        parent.addChild(child);
        compileExpression(child);

        // ;
        token = tokens.read();
        if (token.getType() == TokenType.SemiColon) {
            parent.addChild(token);
        } else {
            throw new Exception("let statement: expected ; not " + token);
        }

        System.out.println("compile let statement success");
    }

    // AST 第5层 语句中的表达式
    private void compileExpression(ASTNode parent) throws Exception {
        // 第一个结点一是一个 term
        ASTNode child = new NToken(NTokenType.term, "term");
        compileTerm(child);
        parent.addChild(child);

        // 0个以上的 op term
        Token token = tokens.peek();
        TokenType type = token.getType();
        while (type == TokenType.Plus || type == TokenType.Minus || type == TokenType.MUL
                || type == TokenType.DIV || type == TokenType.And || type == TokenType.Or
                || type == TokenType.LT || type == TokenType.GT || type == TokenType.EQ
                || type == TokenType.LE || type == TokenType.GE) {
            // 加入 ope 子节点
            parent.addChild(tokens.read());

            // 加入 term 子节点
            child = new NToken(NTokenType.term, "term");
            compileTerm(child);
            parent.addChild(child);

            token = tokens.peek();
            type = token.getType();
        }

    }

    // AST 第六层 表达式中的项
    private void compileTerm(ASTNode parent) throws Exception {
        Token token = tokens.read();
        TokenType type = token.getType();


        // 只有一个子节点 常量
        if (type == TokenType.IntLiteral || type == TokenType.StringLiteral || type == TokenType.True
                || type == TokenType.False || type == TokenType.Null || type == TokenType.This) {
            parent.addChild(token);
            return;
        }

        // 只有一个子节点 标识符 或者 、5-6个节点 subroutineCall 或者 三个结点 varName[expression]
        if (type == TokenType.Identifier) {
            // varName | subroutineName | className
            parent.addChild(token);

            // [ , ( , .
            token = tokens.peek();

            // [  expression]
            if (token.getType() == TokenType.LeftBracket) {
                // [
                parent.addChild(tokens.read());

                // expression
                ASTNode child = new NToken(NTokenType.expression, "expression");
                compileExpression(child);
                parent.addChild(child);

                // ]
                token = tokens.read();
                if (token.getType() == TokenType.RightBracket) {
                    parent.addChild(token);
                } else {
                    throw new Exception("term: expect ']' not " + token.getText());
                }
                return;
            }

            // subroutineCall 剩余结点
            if (token.getType() == TokenType.LeftParen || token.getType() == TokenType.Dot) {

                // ( 则进入 (expressionList) 分支
                if (token.getType() == TokenType.LeftParen) {
                    // (
                    token = tokens.read();
                    parent.addChild(token);

                    if (tokens.peek().getType() != TokenType.RightParen) {
                        ASTNode child = new NToken(NTokenType.expressionList, "expressionList");
                        parent.addChild(child);
                        compileExpressionList(child);
                    }

                    // ) 结束
                    token = tokens.read();
                    if (token.getType() == TokenType.RightParen) {
                        parent.addChild(token);
                        return;
                    } else {
                        throw new Exception("subroutineCall: expect ')' not " + token);
                    }
                }

                // . 则进入 className|varName . subroutineName(expressionList) 分支
                if (token.getType() == TokenType.Dot) {
                    // .
                    token = tokens.read();
                    parent.addChild(token);

                    // subroutineName
                    token = tokens.read();
                    if (token.getType() == TokenType.Identifier) {
                        parent.addChild(token);
                    } else {
                        throw new Exception("subroutineCall: expect subroutineName not " + token);
                    }

                    // (
                    token = tokens.read();
                    if (token.getType() == TokenType.LeftParen) {
                        parent.addChild(token);
                    } else {
                        throw new Exception("subroutineCall: expect ( not " + token);
                    }

                    // expressionList
                    if (tokens.peek().getType() != TokenType.RightParen) {
                        ASTNode child = new NToken(NTokenType.expressionList, "expressionList");
                        parent.addChild(child);
                        compileExpressionList(child);
                    }

                    // ) 结束
                    token = tokens.read();
                    if (token.getType() == TokenType.RightParen) {
                        parent.addChild(token);
                        return;
                    } else {
                        throw new Exception("subroutineCall: expect ')' not " + token);
                    }
                }
            }
            // 看来只有一个 varName
            return;
        }

        // 有三个子节点 '(' expression ')'
        if (type == TokenType.LeftParen) {
            parent.addChild(token);

            // expression
            ASTNode child = new NToken(NTokenType.expression, "expression");
            compileExpression(child);
            parent.addChild(child);

            // )
            token = tokens.read();
            if (token.getType() == TokenType.RightParen) {
                parent.addChild(token);
            } else {
                throw new Exception("term: expect ')' not " + token.getText());
            }
            return;
        }

        // 有两个个节点 unaryOP term
        if (type == TokenType.Minus || type == TokenType.Tilde) {
            // unaryOP
            parent.addChild(token);

            // term
            ASTNode child = new NToken(NTokenType.term, "term");
            compileTerm(child);
            parent.addChild(child);
            return;
        }


        throw new Exception("can not resolve term because of " + token);
    }

    // 第七层 表达式中的项的方法调用中的表达式列表
    private void compileExpressionList(ASTNode parent) throws Exception {
        // expression
        ASTNode child = new NToken(NTokenType.expression, "expression");
        compileExpression(child);
        parent.addChild(child);

        while (tokens.peek().getType() == TokenType.Comma) {
            parent.addChild(tokens.read());

            child = new NToken(NTokenType.expression, "expression");
            compileExpression(child);
            parent.addChild(child);
        }
    }
}

