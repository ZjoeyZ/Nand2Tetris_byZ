package type;

/**
 * AST节点的类型。
 */
public enum NTokenType implements Type {
    Class,

    classVarDec,
    subroutineDec,
    parameterList,
    subroutineBody,

    varDec,


    expression,
    term,
    expressionList,

    className,
    subroutineName,
    varName,


    doStatement,
    letStatement,
    ifStatement,
    whileStatement,
    returnStatement,


    op,
    unaryOp,
    keywordConstant,
}