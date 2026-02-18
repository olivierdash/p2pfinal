package client;

import java.net.*;

public class PeerDiscovery extends Thread {
    private static final int DISCOVERY_PORT = 8888;
    private int myTcpPort;

    public PeerDiscovery(int myTcpPort) {
        this.myTcpPort = myTcpPort;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        // Initialisation de l'IP locale une seule fois pour les performances
        String monIp;
        try {
            monIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            monIp = "127.0.0.1";
        }

        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            socket.setBroadcast(true);
            byte[] buffer = new byte[1024];

            System.out.println("Découverte automatique activée sur port " + DISCOVERY_PORT);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String senderIp = packet.getAddress().getHostAddress();
                String message = new String(packet.getData(), 0, packet.getLength());

                // On extrait le port contenu dans le message (ex: "DISCOVER_PEERS:5000")
                String[] parts = message.split(":");
                if (parts.length < 2) continue;
                
                int senderTcpPort = Integer.parseInt(parts[1]);

                // --- CORRECTION : FILTRAGE SOI-MÊME ---
                // On s'ignore si c'est notre IP ET notre port d'écoute TCP
                if (senderIp.equals(monIp) && senderTcpPort == myTcpPort) {
                    continue;
                }

                if (message.startsWith("DISCOVER_PEERS")) {
                    // Quelqu'un nous cherche : on l'ajoute
                    PeerManager.addPeer(senderIp, senderTcpPort);

                    // Et on lui répond qu'on est là
                    String response = "I_AM_HERE:" + myTcpPort;
                    byte[] resData = response.getBytes();
                    DatagramPacket resPacket = new DatagramPacket(
                            resData, resData.length, packet.getAddress(), packet.getPort());
                    socket.send(resPacket);
                    
                } else if (message.startsWith("I_AM_HERE")) {
                    // C'est une réponse à notre broadcast : on ajoute le voisin
                    PeerManager.addPeer(senderIp, senderTcpPort);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur Discovery : " + e.getMessage());
        }
    }

    public static void broadcastPresence(int myPort) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            String message = "DISCOVER_PEERS:" + myPort;
            byte[] data = message.getBytes();

            // Envoi à l'adresse de broadcast universelle
            DatagramPacket packet = new DatagramPacket(
                    data, data.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}