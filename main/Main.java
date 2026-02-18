package main;

import Files.FileManager;
import server.ServerListener;
import views.Fenetre;

public class Main {
    public static void main(String[] args) {
        // 1. Initialisation du dossier "partage" 
        FileManager.init();

        // 2. Démarrage du serveur P2P (Port 5000 par défaut)
        int myPort = 5000;
        ServerListener server = new ServerListener(myPort);
        server.start(); 

        // 3. Lancement de l'interface graphique [cite: 2]
        javax.swing.SwingUtilities.invokeLater(() -> {
            new Fenetre();
        });
        
        System.out.println("Client P2P prêt et en écoute sur le port " + myPort);
    }
}