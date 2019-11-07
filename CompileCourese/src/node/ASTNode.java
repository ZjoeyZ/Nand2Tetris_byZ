package node;

import type.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * AST的节点。
 */
public class ASTNode {
    ASTNode parent = null;
    public List<ASTNode> children = new ArrayList<ASTNode>();

    public String getText() {
        return null;
    }

    public Type getType() {
        return null;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public ASTNode getParent() {
        return parent;
    }

    public void addChild(ASTNode token) {
    }
}