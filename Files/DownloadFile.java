package Files;

import java.io.*;
import java.net.Socket;
import java.util.List;
import javax.swing.*;

public class DownloadFile extends SwingWorker<Void, Integer> {
    private List<String> peerInfos;
    private String fileName;

    public DownloadFile(List<String> peerInfos, String fileName) {
        this.peerInfos = peerInfos;
        this.fileName = fileName;
    }

    @Override
    protected Void doInBackground() throws Exception {
        String sourceIp = null;
        int sourcePort = 0;
        long totalSize = 0;
        String expectedHash = "";

        // 1. Trouver qui a le fichier
        for (String info : peerInfos) {
            String[] parts = info.split(":");
            try {
                long size = getRemoteFileSize(parts[0], Integer.parseInt(parts[1]), fileName);
                if (size > 0) {
                    sourceIp = parts[0];
                    sourcePort = Integer.parseInt(parts[1]);
                    totalSize = size;
                    expectedHash = getRemoteHash(sourceIp, sourcePort, fileName);
                    break;
                }
            } catch (Exception e) {
                continue;
            }
        }

        if (sourceIp == null) {
            throw new Exception("Le fichier n'a été trouvé chez aucun voisin actif.");
        }

        // 2. Calcul des segments
        int totalChunks = (int) Math.ceil((double) totalSize / FileManager.CHUNK_SIZE);

        for (int i = 0; i < totalChunks; i++) {
            boolean chunkReceived = false;

            try (Socket s = new Socket(sourceIp, sourcePort);
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());
                    DataInputStream in = new DataInputStream(s.getInputStream())) {

                out.writeUTF("CHUNK:" + fileName + ":" + i);

                // Calcul précis de la taille du segment à lire
                long remaining = totalSize - ((long) i * FileManager.CHUNK_SIZE);
                int expectedLen = (int) Math.min(FileManager.CHUNK_SIZE, remaining);

                FileManager.receiveFileChunk(fileName, i, in, expectedLen);
                chunkReceived = true;
            } catch (IOException e) {
                System.err.println("Erreur segment " + i + ": " + e.getMessage());
            }

            if (!chunkReceived)
                throw new Exception("Échec de récupération du segment " + i);

            publish((i * 100) / totalChunks);
        }

        // 3. Vérification finale (CORRECTION DES CHEMINS ICI)
        // On vérifie le fichier là où FileManager l'a écrit : dans "recus/"
        File finalFile = new File(FileManager.FOLDER_RECUS + fileName);

        if (!finalFile.exists()) {
            throw new Exception("Erreur système : Fichier non trouvé après téléchargement.");
        }

        String actualHash = FileManager.getFileChecksum(finalFile);
        if (!actualHash.equals(expectedHash)) {
            // Optionnel : finalFile.delete();
            throw new Exception("Intégrité échouée : le fichier reçu est corrompu (Hash mismatch).");
        }

        return null;
    }

    private long getRemoteFileSize(String ip, int port, String fileName) throws IOException {
        try (Socket s = new Socket(ip, port);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(s.getInputStream())) {
            s.setSoTimeout(2000); // Éviter de bloquer si le voisin est lent
            out.writeUTF("SIZE:" + fileName);
            return in.readLong();
        }
    }

    private String getRemoteHash(String ip, int port, String fileName) throws IOException {
        try (Socket s = new Socket(ip, port);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(s.getInputStream())) {
            s.setSoTimeout(2000);
            out.writeUTF("HASH:" + fileName);
            return in.readUTF();
        }
    }

    @Override
    protected void done() {
        try {
            get();
            JOptionPane.showMessageDialog(null, "Téléchargement réussi dans le dossier 'recus' !");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur : " + e.getCause().getMessage());
        }
    }
}