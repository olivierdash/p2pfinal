package Files;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;

public class FileManager {
    public static final String FOLDER = "partage/";
    public static final String FOLDER_RECUS = "recus/";
    public static final int CHUNK_SIZE = 512 * 1024;

    public static void init() {
        new File(FOLDER).mkdir();
        new File(FOLDER_RECUS).mkdir();
    }

    // Cherche le fichier dans 'partage' OU 'recus'
    public static File findFile(String filename) {
        File f1 = new File(FOLDER + filename);
        if (f1.exists()) return f1;
        File f2 = new File(FOLDER_RECUS + filename);
        if (f2.exists()) return f2;
        return null;
    }

    public static long getFileSize(String filename) {
        File f = findFile(filename);
        return (f != null && f.isFile()) ? f.length() : 0;
    }

    public static void sendFileChunk(String filename, int chunkIndex, OutputStream out) throws IOException {
        File fichier = findFile(filename);
        if (fichier == null) return;

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

    public static void receiveFileChunk(String filename, int chunkIndex, InputStream in, int length)
            throws IOException {
        // On écrit toujours dans "recus/"
        File target = new File(FOLDER_RECUS + filename);
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

    public static String getFileChecksum(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int nread;
            while ((nread = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, nread);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static ArrayList<String> listFiles() {
        Set<String> allFiles = new HashSet<>();
        File d1 = new File(FOLDER);
        File d2 = new File(FOLDER_RECUS);
        
        if (d1.list() != null) Collections.addAll(allFiles, d1.list());
        if (d2.list() != null) Collections.addAll(allFiles, d2.list());
        
        return new ArrayList<>(allFiles);
    }

    public static void uploadToSharedFolder(File sourceFile) throws IOException {
        File destination = new File(FOLDER + sourceFile.getName());
        Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}