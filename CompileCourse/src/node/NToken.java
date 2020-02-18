package node;

import type.GrammarType;

import java.util.ArrayList;
import java.util.List;

/**
 * 非终结符结点，语法单位
 */
public class NToken extends ASTNode {
    public List<ASTNode> children = new ArrayList<ASTNode>();

    public GrammarType type = null;
    String text = null;

    public NToken(GrammarType type, String text) {
        this.type = type;
        this.text = text;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public GrammarType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public void addChild(ASTNode child) {
        children.add(child);
        child.parent = this;
    }

}