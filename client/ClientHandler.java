package client;

import java.io.*;
import java.net.Socket;
import Files.FileManager;

public class ClientHandler implements Runnable {
    private Socket client;

    public ClientHandler(Socket s) {
        this.client = s;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(client.getInputStream());
                DataOutputStream out = new DataOutputStream(client.getOutputStream())) {

            String command = in.readUTF();

            if (command.equals("LIST")) {
                var files = FileManager.listFiles();
                out.writeInt(files.size());
                for (String f : files)
                    out.writeUTF(f);
            } else if (command.startsWith("SIZE:")) {
                String filename = command.substring(5);
                out.writeLong(FileManager.getFileSize(filename));
            } else if (command.startsWith("CHUNK:")) {
                // Format: CHUNK:filename:index
                String[] parts = command.split(":");
                String filename = parts[1];
                int index = Integer.parseInt(parts[2]);
                FileManager.sendFileChunk(filename, index, out);
            } else if (command.startsWith("HASH:")) {
                String filename = command.substring(5);
                try {
                    File f = new File("partage/" + filename);
                    out.writeUTF(FileManager.getFileChecksum(f));
                } catch (Exception e) {
                    out.writeUTF("ERROR");
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur P2P: " + e.getMessage());
        }
    }
}