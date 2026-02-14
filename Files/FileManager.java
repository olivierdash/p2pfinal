package Files;

import java.io.File;
import java.util.*;

public class FileManager {
    private static final String FOLDER = "partage/";

    public static void init(){
        File dir = new File(FOLDER);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public static ArrayList<String> listFiles(){
        File dossier = new File(FOLDER);
        String[] files = dossier.list();
        return files != null ? new ArrayList<>(Arrays.asList(files)) : new ArrayList<>();
    }
}
