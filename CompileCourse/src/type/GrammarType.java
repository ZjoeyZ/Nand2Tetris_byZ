package type;

/**
 * 语法单位。
 * 没有使用所有的语法单位，比如 Class 下一级有结点 className， className 下一节点 identifier
 * 我直接让 Class 的下一级有节点 identifier
 */
public enum GrammarType implements Type {
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