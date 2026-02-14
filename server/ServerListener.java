package server;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerListener extends Thread{
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
                //Attend une connection 
                Socket client = serveur.accept();
                // Donne la gestion a un noveau thread
                new Thread()
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
}
