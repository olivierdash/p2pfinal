package Files;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class FileManager {
    private static final String FOLDER = "partage/";

    public static void init() {
        File dir = new File(FOLDER);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public static void sendFile(String filename, OutputStream out) throws IOException {
        File fichier = new File(FOLDER + filename);
        if (!fichier.exists()) {
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

    public static void uploadToSharedFolder(File sourceFile) throws IOException {
        // On définit la destination : dossier de partage + nom du fichier d'origine
        File destination = new File(FOLDER + sourceFile.getName());

        // On copie le fichier physiquement sur le disque
        // StandardCopyOption.REPLACE_EXISTING permet d'écraser si le fichier existe
        // déjà
        Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Fichier ajouté au partage : " + sourceFile.getName());
    }

    public static ArrayList<String> listFiles() {
        File dossier = new File(FOLDER);
        String[] files = dossier.list();
        return files != null ? new ArrayList<>(Arrays.asList(files)) : new ArrayList<>();
    }

    public static void receiveFile(String filename, InputStream in) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(FOLDER + "recu_" + filename)) {
            byte[] buffer = new byte[4096];
            int count;
            // Attention: bloque jusqu'à la fermeture du socket par l'émetteur
            while ((count = in.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
        }
    }
}
