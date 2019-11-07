package node;

import type.TokenType;

public class Token extends ASTNode {

    // 文本值
    private String lexeme;

    //Token类型、种别
    private TokenType type = null;


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


    @Override
    public String toString() {
        return "(" + lexeme + "," + type + ")";
    }

}
