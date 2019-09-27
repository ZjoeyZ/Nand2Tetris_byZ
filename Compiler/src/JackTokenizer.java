import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;


public class JackTokenizer {
    private BufferedReader br;
    private ArrayList<String> files = new ArrayList<>();

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


}
