import java.io.*;
import java.util.Locale;

public class VMWriter {
    private static final String CHARSET_NAME = "UTF-8";

    private static final Locale LOCALE = Locale.CHINA;

    private PrintWriter out;

    /**
     * 构造一个 VMWriter，面向 一个输入流
     */
    public VMWriter(OutputStream os) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(os, CHARSET_NAME);
            out = new PrintWriter(osw, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  构造一个 VMWriter，面向 操作系统的标准输出
     */
    public VMWriter() {
        this(System.out);
    }

    /**
     * 构造一个 VMWriter，面向 文件输出
     *
     * @param  filename 文件名
     */
    public VMWriter(String filename) {
        try {
            OutputStream os = new FileOutputStream(filename);
            OutputStreamWriter osw = new OutputStreamWriter(os, CHARSET_NAME);
            out = new PrintWriter(osw, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePush(String segment, int index) {
        out.println("push " + segment + " " + index);
    }

    public void writePop(String segment, int index) {
        out.println("pop " + segment + " " + index);
    }

    public void writeArithmetic(String op) {
        out.println(op);
    }

    public void writeLabel(int labelNum) {
        out.println("label L" + labelNum);
    }

    public void writeIf(int labelNum) {
        out.println("if-goto L" + labelNum);
    }

    public void writeGoto(int labelNum) {
        out.println("goto L" + labelNum);
    }
    public void writeCall(String name, int nArgs) {
        out.println("call " + name + " " + nArgs);
    }

    public void writeFunction(String name, int nLocs) {
        out.println("function " + name + " " + nLocs);
    }

    public void writeReturn(){
        out.println("return");
    }

    public void writeComment(String comment) {
        out.println("// " + comment);
    }
    /**
     * 关闭输出流
     */
    public void close() {
        out.flush();
        out.close();
    }

}
