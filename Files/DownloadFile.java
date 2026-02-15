package Files;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class DownloadFile extends SwingWorker<Void, Void> {
    private String ip;
    private int port;
    private String fileName;
    public DownloadFile(String ip, int port, String fileName) {
        this.ip = ip;
        this.port = port;
        this.fileName = fileName;
    }

    @Override
    protected Void doInBackground() throws Exception {
        try (Socket s = new Socket(ip, port);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(s.getInputStream());) {
            out.writeUTF("GET:" + fileName);
            FileManager.receiveFile(fileName, in);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    @Override
    protected void done() {
        try {
            get(); // Pour attraper les exceptions éventuelles
            JOptionPane.showMessageDialog(null, "Téléchargement de '" + fileName + "' terminé !");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur lors du téléchargement : " + e.getMessage());
        }
    }
}
