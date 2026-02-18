package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import Files.FileManager;
import client.PeerManager;

public class CommandProcessor {

    public void execute(String command, DataOutputStream out) throws IOException {
        if (command.startsWith("LIST_ME:")) {
            String[] parts = command.split(":");
            if (parts.length == 3) {
                String ipVoisin = parts[1];
                int portVoisin = Integer.parseInt(parts[2]);
                PeerManager.addPeer(ipVoisin, portVoisin);
                handleList(out);
            }
        } else if (command.startsWith("SIZE:")) {
            handleSize(command.substring(5), out);
        } else if (command.startsWith("HASH:")) {
            handleHash(command.substring(5), out);
        } else if (command.startsWith("CHUNK:")) {
            handleChunk(command, out);
        }
    }

    private void handleList(DataOutputStream out) throws IOException {
        ArrayList<String> files = FileManager.listFiles();
        out.writeInt(files.size());
        for (String file : files) {
            out.writeUTF(file);
        }
    }

    private void handleSize(String filename, DataOutputStream out) throws IOException {
        // FileManager.getFileSize cherche maintenant dans les deux dossiers
        out.writeLong(FileManager.getFileSize(filename));
    }

    private void handleHash(String filename, DataOutputStream out) throws IOException {
        try {
            // CORRECTION : On utilise findFile pour chercher dans partage/ et recus/
            File f = FileManager.findFile(filename);
            if (f != null && f.exists()) {
                out.writeUTF(FileManager.getFileChecksum(f));
            } else {
                out.writeUTF("ERROR");
            }
        } catch (Exception e) {
            out.writeUTF("ERROR");
        }
    }

    private void handleChunk(String command, DataOutputStream out) throws IOException {
        String[] parts = command.split(":");
        if (parts.length == 3) {
            String filename = parts[1];
            int chunkIndex = Integer.parseInt(parts[2]);
            // sendFileChunk utilise aussi findFile en interne (si tu as pris ma version)
            FileManager.sendFileChunk(filename, chunkIndex, out);
        }
    }
}