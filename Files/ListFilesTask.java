package Files;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class ListFilesTask extends SwingWorker<ArrayList<String>, Void> {
    private String ip;
    private int port;
    private DefaultListModel<String> model;

    public ListFilesTask(String ip, int port, DefaultListModel<String> model) {
        this.ip = ip;
        this.port = port;
        this.model = model;
    }

    @Override
    protected ArrayList<String> doInBackground() throws Exception {
        try (Socket s = new Socket(ip, port);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(s.getInputStream())) {

            out.writeUTF("LIST");
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
            model.clear();
            for (String f : files) {
                model.addElement(f);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Impossible de lister les fichiers : " + e.getMessage());
        }
    }
}
