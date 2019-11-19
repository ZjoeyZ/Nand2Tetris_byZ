package type;

/**
 * Token的类型，词法单位
 */
public enum TokenType implements Type{
    Plus,   // +
    Minus,  // -
    MUL,   // *
    DIV,  // /
    Tilde,  // ~
    Dot,    // .
    Comma,  // ,
    GE,     // >=
    GT,     // >
    EQ,     // ==
    NEQ,    // !=
    LE,     // <=
    LT,     // <
    And,    // &
    Or,     // |
    SemiColon, // ;
    LeftParen, // (
    RightParen,// )
    LeftCurly, // {
    RightCurly, // }
    LeftBracket,     // [
    RightBracket,     // ]

    Assignment,// =

    If,
    Else,
    While,

    Class,
    Method,
    Constructor,
    Function,

    Static,
    Field,

    Return,
    This,

    Var,
    Let,
    Do,
    Void,

    Int,
    Boolean,
    Char,
    Null,
    True,
    False,

    Identifier,     //标识符

    IntLiteral,     //整型字面量
    StringLiteral   //字符串字面量
}