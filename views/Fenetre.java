package views;

import java.io.IOException;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import Files.*;
import client.PeerManager;
import client.PeerDiscovery; // Assure-toi d'avoir créé cette classe
import server.ServerListener;

public class Fenetre extends JFrame {

    private JButton btnRefresh, btnDownload, btnUpload, btnStartServer;
    private JTextField txtPortLocal;
    private JList<String> listFiles;
    private DefaultListModel<String> listModel;

    public Fenetre() {
        setTitle("Client P2P Autonome (Zéro Conf)");
        setSize(550, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Panneau Nord : Configuration Locale ---
        JPanel panelNorth = new JPanel(new GridLayout(2, 2, 5, 5));
        panelNorth.setBorder(BorderFactory.createTitledBorder("Mon Serveur"));

        panelNorth.add(new JLabel(" Mon Port d'écoute :"));
        txtPortLocal = new JTextField("5000");
        panelNorth.add(txtPortLocal);

        btnStartServer = new JButton("Démarrer et Découvrir les voisins");
        panelNorth.add(new JLabel(" Statut : Hors ligne"));
        panelNorth.add(btnStartServer);

        // --- Zone Centrale : Liste des fichiers ---
        listModel = new DefaultListModel<>();
        listFiles = new JList<>(listModel);
        listFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(listFiles);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Fichiers détectés sur le réseau local"));

        // --- Panneau Sud : Actions ---
        JPanel panelSouth = new JPanel(new FlowLayout());
        btnRefresh = new JButton("Actualiser le réseau");
        btnDownload = new JButton("Télécharger");
        btnUpload = new JButton("Partager un fichier");

        panelSouth.add(btnRefresh);
        panelSouth.add(btnDownload);
        panelSouth.add(btnUpload);

        add(panelNorth, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelSouth, BorderLayout.SOUTH);

        // --- LOGIQUE DES BOUTONS ---

        // 0. Démarrer le serveur + Découverte UDP
        btnStartServer.addActionListener(e -> {
            try {
                int port = Integer.parseInt(txtPortLocal.getText());

                // Lancement du serveur TCP (pour les transferts)
                new ServerListener(port).start();

                // Lancement de l'écoute UDP (pour la découverte auto)
                new PeerDiscovery(port).start();

                btnStartServer.setEnabled(false);
                txtPortLocal.setEditable(false);
                btnStartServer.setText("Serveur Actif (Port " + port + ")");

                // On annonce immédiatement notre présence aux autres
                PeerDiscovery.broadcastPresence(port);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        });

        // 1. Actualiser le réseau (Crier "Y'a quelqu'un ?" et demander les fichiers)
        // 1. Actualiser le réseau
        btnRefresh.addActionListener(e -> {
            try {
                int monPort = Integer.parseInt(txtPortLocal.getText());
                String monIpLocale = java.net.InetAddress.getLocalHost().getHostAddress();

                PeerDiscovery.broadcastPresence(monPort);

                // On vide la liste UNE SEULE FOIS ici au début du clic
                listModel.clear();

                List<String> voisins = PeerManager.getPeers();

                for (String info : voisins) {
                    String[] parts = info.split(":");
                    String ipVoisin = parts[0];
                    int portVoisin = Integer.parseInt(parts[1]);

                    if (ipVoisin.equals(monIpLocale) && portVoisin == monPort) {
                        continue;
                    }

                    // On lance la tâche pour chaque voisin
                    new ListFilesTask(ipVoisin, portVoisin, monPort, listModel).execute();
                }
            } catch (java.net.UnknownHostException ex) {
                ex.printStackTrace();
            }
        });
        // 2. Télécharger
        btnDownload.addActionListener(e -> {
            String selectedFile = listFiles.getSelectedValue();
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un fichier dans la liste !");
                return;
            }

            // Le PeerManager contient maintenant des chaînes "IP:Port" (ex:
            // "192.168.1.15:5000")
            List<String> sources = PeerManager.getPeers();

            if (sources.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune source (peer) disponible.");
                return;
            }

            // MISE À JOUR : On ne passe plus "5000" en dur,
            // car le port est déjà dans les chaînes de la liste 'sources'
            new DownloadFile(sources, selectedFile).execute();
        });

        // 3. Déployer
        btnUpload.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    FileManager.uploadToSharedFolder(chooser.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Fichier prêt !");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }
}