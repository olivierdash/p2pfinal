package server;

import client.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListener extends Thread {

    private int port;

    public ServerListener(int port) {
        this.port = port;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        startServer();
    }

    private void startServer() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Serveur P2P actif sur le port : " + port);

            while (!Thread.currentThread().isInterrupted()) { // Plus propre que true
                try {
                    acceptClient(server);
                } catch (Exception e) {
                    // Si un client échoue, on l'affiche mais on continue d'écouter les autres
                    System.err.println("Erreur lors de l'acceptation d'un peer : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Échec critique du serveur : " + e.getMessage());
        }
    }

    private void acceptClient(ServerSocket server) throws Exception {
        Socket client = server.accept();
        new Thread(new ClientSession(client)).start();
    }
}
