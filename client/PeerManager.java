package client;

import java.util.ArrayList;
import java.util.List;

public class PeerManager {
    private static final List<String> activePeers = new ArrayList<>();

    public static void addPeer(String ip, int port) {
        String peer = ip + ":" + port;
        if (!activePeers.contains(peer)) {
            activePeers.add(peer);
        }
    }

    public static List<String> getPeers() {
        return new ArrayList<>(activePeers); // Retourne une copie pour la sécurité
    }
}