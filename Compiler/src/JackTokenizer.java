import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;


public class JackTokenizer {
    private BufferedReader br;
    private ArrayList<String> files = new ArrayList<>();
    private int fileIdx = 0;

    public JackTokenizer(String path) {
        File file = new File(path);

        if (file.isFile()) {
            files.add(path);
        } else {
            File[] tempList = file.listFiles();

            assert tempList != null;

            for (File aTempList : tempList) {
                if (aTempList.toString().endsWith(".jack")) {
                    System.out.println("获取文件：" + aTempList.toString());
                    files.add(aTempList.toString());
                }
            }
        }
    }

    private boolean open() {
        try {
            if (br == null && fileIdx != files.size()) {
                br = new BufferedReader(new FileReader(files.get(fileIdx)));
                fileIdx += 1;
                return true;
            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void close() {
        try {
            if (br != null)
                br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String nextLine() throws IOException {
        // 输入流
        if (br == null) {
            if (!open()) {
                return null;
            }
        }

        // 读取
        String line;
        while (true) {
            line = br.readLine();
            if (line == null) {
                close();
                br = null;
                return nextLine();
            }
            if (line.startsWith("/*")) {
                continue;
            }
            if (line.startsWith("/**")) {
                continue;
            }
            if (line.endsWith("*/")) {
                continue;
            }
            //surround ';' '(' ')' '.' with space
            line = line.replaceAll("\\(", " ( ");
            line = line.replaceAll("\\)", " ) ");
            line = line.replaceAll("\\.", " . ");
            line = line.replaceAll("\\;", " ; ");

            line = line.replaceAll("//.*", "").trim();
            if (line.length() == 0)
                continue;

            return line;
        }
    }

    private ListIterator<String> getTokens(String line) {
        String[] splitTokens = line.split("\\s+");
        ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(splitTokens));
        return tokens.listIterator();
    }

    private String tokenType() {
        String token = null;
        return token;
    }

    private String getXmlUnit() {
        String unit = null;
        return unit;
    }

    public static void main(String[] args) {
        JackTokenizer jk = new JackTokenizer("C:\\Users\\流川枫\\Downloads\\nand2tetris\\projects\\10\\ExpressionLessSquare");
        try {
            while (true) {
                String line = jk.nextLine();
                if (line == null) {
                    break;
                }

                ListIterator<String> tokens = jk.getTokens(line);
                while (tokens.hasNext()) {
//                    String type = jk.tokenType();
//                    String xmlUnit = jk.getXmlUnit();
                    System.out.println(tokens.next());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
