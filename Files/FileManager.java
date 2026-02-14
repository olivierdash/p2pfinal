package Files;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
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

    public static void receiveFile(Socket socket) throws Exception {
        DataInputStream di = new DataInputStream(socket.getInputStream());
        String filename = di.readUTF();
        try (FileOutputStream fos = new FileOutputStream("recu_"+filename)) {
            byte[] buffer = new byte[4096];
            int bytesread;

            while ((bytesread = di.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesread);
            }
            System.out.println("Fichier recu avec succes");
        } 
    }
}
