package Files;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class ListFilesTask extends SwingWorker<ArrayList<String>, Void> {
    private String ipCible;
    private int portCible;
    private int monPortLocal; // Nouveau : pour s'identifier auprès du voisin
    private DefaultListModel<String> model;

    public ListFilesTask(String ipCible, int portCible, int monPortLocal, DefaultListModel<String> model) {
        this.ipCible = ipCible;
        this.portCible = portCible;
        this.monPortLocal = monPortLocal;
        this.model = model;
    }

    @Override
    protected ArrayList<String> doInBackground() throws Exception {
        try (Socket s = new Socket(ipCible, portCible);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(s.getInputStream())) {

            // On récupère notre propre IP locale sur le réseau
            String monIp = s.getLocalAddress().getHostAddress();

            // On envoie une commande spéciale : "LIST_ME:IP:PORT"
            // Cela dit au voisin : "Donne moi ta liste, et au passage, voici mon adresse"
            out.writeUTF("LIST_ME:" + monIp + ":" + monPortLocal);

            int count = in.readInt();
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                list.add(in.readUTF());
            }
            return list;
        }
    }

    @Override
    protected void done() {
        try {
            ArrayList<String> files = get();
            for (String f : files) {
                model.addElement(f);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "L'ami est injoignable : " + e.getMessage());
        }
    }
}