package Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class FileManager {
    private static final String FOLDER = "partage/";

    public static void init(){
        File dir = new File(FOLDER);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public static void sendFile(String filename, OutputStream out) throws IOException {
        File fichier = new File(FOLDER + filename);
        if (! fichier.exists()) {
            return;
        }
        try (FileInputStream f = new FileInputStream(fichier)) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = f.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
        }
        out.flush();
    }

    public static ArrayList<String> listFiles(){
        File dossier = new File(FOLDER);
        String[] files = dossier.list();
        return files != null ? new ArrayList<>(Arrays.asList(files)) : new ArrayList<>();
    }
}
