package scope;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    // 这个SymbolTable 里面只有 Variable 成员
    private Map<String, Variable> symbols = new HashMap<>();
    protected SymbolTable enclosingSymbolTable;

    // 各种 kind 的变量index
    private int staticNum = -1;
    private int fieldNum = -1;
    private int argNum = -1;
    private int localNum = -1;

    public SymbolTable() {
    }

    public SymbolTable(SymbolTable enclosingSymbolTable) {
        this.enclosingSymbolTable = enclosingSymbolTable;
    }

    ;

    /**
     * 向 table 中添加 变量
     */
    public void Define(String name, String type, String kind) {
        int number = -1;
        switch (kind) {
            case "static":
                staticNum++;
                number = staticNum;
                break;
            case "field":
                fieldNum++;
                number = fieldNum;
                break;
            case "argument":
                argNum++;
                number = argNum;
                break;
            case "local":
                localNum++;
                number = localNum;
                break;
            default:
                try {
                    throw new Exception("variable kind not satisfied:" + kind);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        Variable v = new Variable(name, type, kind, number, this);
        symbols.put(name, v);
    }


    private Variable getVariable(String name) {
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        }
        // 找不到就去上一层找
        if (enclosingSymbolTable != null) {
            return enclosingSymbolTable.getVariable(name);
        }
        return null;
    }

    /**
     * 返回变量数量
     *
     * @param kind
     * @return
     * @throws Exception
     */
    public int varCount(String kind) throws Exception {
        switch (kind) {
            case "static":
                return staticNum + 1;
            case "field":
                return fieldNum + 1;
            case "argument":
                return argNum + 1;
            case "local":
                return localNum + 1;
        }
        throw new Exception("varCount() error, invalid type");
    }

    public String kindOf(String name) {
        Variable v = this.getVariable(name);
        assert v != null;
        return v.getKind();
    }

    public String typeOf(String name) {
        Variable v = this.getVariable(name);
        assert v != null;
        return v.getType();
    }

    public int indexOf(String name) {
        Variable v = this.getVariable(name);
        assert v != null;
        return v.getNumber();
    }

    public boolean contains(String name) {
        return this.getVariable(name) != null;
    }

    public void setArgNum(int argNum) {
        this.argNum = argNum;
    }
}