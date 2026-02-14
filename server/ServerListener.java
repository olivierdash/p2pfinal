package server;

import java.net.ServerSocket;
import java.net.Socket;

import client.ClientHandler;

public class ServerListener extends Thread {
    private int port;

    public ServerListener(int port) {
        this.port = port;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        try (ServerSocket serveur = new ServerSocket()) {
            // TODO Auto-generated method stub
            System.out.println("Serveur demare sur le port : " + port);
            while (true) {
                // Attend une connection
                Socket client = serveur.accept();
                // Donne la gestion a un noveau thread
                new Thread(new ClientHandler(client)).start();
            }
        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
