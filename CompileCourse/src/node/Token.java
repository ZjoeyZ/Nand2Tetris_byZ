package node;

import type.TokenType;

/**
 *  终结符叶子结点，词法单位
 */
public class Token extends ASTNode {

    // 文本值
    private String lexeme;

    //Token类型、种别
    private TokenType type = null;

    // 在源程序中的第几行
    private int line;

    // 第 line 行中的第几个
    private int number;

    public Token(String lexeme, TokenType type, int line, int number) {
        this.lexeme = lexeme;
        this.type = type;
        this.line = line;
        this.number = number;
    }


    public Token(String lexeme, TokenType type) {
        this.lexeme = lexeme;
        this.type  = type;
    }

    @Override
    public String getText() {
        return lexeme;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    public String getPosition() {
        return "'" + lexeme + "' at line:" + line + " number:" + number;
    }

    @Override
    public String toString() {
        return "(" + lexeme + "," + type + ")" + "\nline:" + line + "\tnumber:" + number;
    }

}
