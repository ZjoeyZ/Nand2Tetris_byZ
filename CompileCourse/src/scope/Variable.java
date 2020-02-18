package scope;

public class Variable {
    //符号的名称
    protected String name;

    //所属作用域
    protected SymbolTable enclosingSymbolTable;

    //变量类型
    protected String type;

    // 所属内存段
    protected String kind;

    // 内存段中第几位
    protected int number;

    public String getType() {
        return type;
    }

    public String getKind() {
        return kind;
    }

    public int getNumber() {
        return number;
    }

    protected Variable(String name, String type, String kind, int number, SymbolTable enclosingSymbolTable) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.number = number;
        this.enclosingSymbolTable = enclosingSymbolTable;
    }

    public String getName(){
        return name;
    }

    public SymbolTable getEnclosingSymbolTable(){
        return enclosingSymbolTable;
    }

    @Override
    public String toString(){
        return "Variable " + name + " type: "+ type + " kind: " + kind + " # " + number;
    }

}