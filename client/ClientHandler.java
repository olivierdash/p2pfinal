package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import Files.FileManager; 

public class ClientHandler implements Runnable {
    private Socket client;

    public ClientHandler(Socket s){
        this.client = s;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream())) {
            String commandes = in.readUTF();
            if (commandes.equals("LIST")) {
                ArrayList<String> fichiers = FileManager.listFiles();
                out.writeInt(fichiers.size());
                for (String f : fichiers) {
                    out.writeUTF(f);
                }
            } else if (commandes.startsWith("GET:")) {
                String filename = commandes.substring(4);
                FileManager.sendFile(filename, out);
            }
        } catch (IOException e) {
            System.err.println("Erreur connexion entrante : " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException e) {}
        }
    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }
}