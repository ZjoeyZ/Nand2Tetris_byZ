import node.Token;

import java.util.List;

/**
 * 一个简单的Token流。是把一个Tokens列表进行了封装。
 */
public class TokenReader{
    public List<Token> tokens = null;
    public int pos = 0;
    public TokenReader(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * 返回Token流中下一个Token，并从流中取出。 如果流已经为空，返回null;
     */
    public Token read() {
        if (pos < tokens.size()) {
            return tokens.get(pos++);
        }
        return null;
    }

    /**
     * 返回Token流中下一个Token，但不从流中取出。 如果流已经为空，返回null;
     */
    public Token peek() {
        if (pos < tokens.size()) {
            return tokens.get(pos);
        }
        return null;
    }

    /**
     * Token流回退一步。恢复原来的Token。
     */
    public void unread() {
        if (pos > 0) {
            pos--;
        }
    }

    /**
     * 获取Token流当前的读取位置。
     * @return
     */
    public int getPosition() {
        return pos;
    }

    /**
     * 设置Token流当前的读取位置
     * @param position
     */
    public void setPosition(int position) {
        if (position >=0 && position < tokens.size()){
            pos = position;
        }
    }

}
