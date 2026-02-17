package Files;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;

public class FileManager {
    private static final String FOLDER = "partage/";
    // Taille d'un segment : 512 Ko pour permettre le partage par "bouts"
    public static final int CHUNK_SIZE = 512 * 1024; 

    public static void init() {
        File dir = new File(FOLDER);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    // Calcule l'empreinte numérique pour vérifier l'intégrité
    public static String getFileChecksum(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int nread;
            while ((nread = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, nread);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // Envoie un segment spécifique (chunkIndex) pour permettre le multi-source
    public static void sendFileChunk(String filename, int chunkIndex, OutputStream out) throws IOException {
        File fichier = new File(FOLDER + filename);
        if (!fichier.exists()) return;

        try (RandomAccessFile raf = new RandomAccessFile(fichier, "r")) {
            long pos = (long) chunkIndex * CHUNK_SIZE;
            if (pos >= raf.length()) return;

            raf.seek(pos);
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead = raf.read(buffer);
            
            if (bytesRead > 0) {
                out.write(buffer, 0, bytesRead);
            }
        }
        out.flush();
    }

    // Reçoit et écrit un segment à une position précise
    public static void receiveFileChunk(String filename, int chunkIndex, InputStream in, int length) throws IOException {
        File target = new File(FOLDER + "recu_" + filename);
        try (RandomAccessFile raf = new RandomAccessFile(target, "rw")) {
            raf.seek((long) chunkIndex * CHUNK_SIZE);
            byte[] buffer = new byte[length];
            int totalRead = 0;
            while (totalRead < length) {
                int read = in.read(buffer, totalRead, length - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
            raf.write(buffer, 0, totalRead);
        }
    }

    public static ArrayList<String> listFiles() {
        File dossier = new File(FOLDER);
        String[] files = dossier.list();
        return files != null ? new ArrayList<>(Arrays.asList(files)) : new ArrayList<>();
    }

    public static void uploadToSharedFolder(File sourceFile) throws IOException {
        File destination = new File(FOLDER + sourceFile.getName());
        Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}