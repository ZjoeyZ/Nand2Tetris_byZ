package node;

import type.NTokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 非终结符结点，语法单位
 */
public class NToken extends ASTNode {
    public List<ASTNode> children = new ArrayList<ASTNode>();

    public NTokenType type = null;
    String text = null;

    public NToken(NTokenType type, String text) {
        this.type = type;
        this.text = text;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public NTokenType getType() {
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