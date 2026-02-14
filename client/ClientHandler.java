package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

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
                List<String> fichiers = FileService.listerFiles();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }
}