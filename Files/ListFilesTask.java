package Files;

import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.SwingWorker;

public class ListFilesTask extends SwingWorker<ArrayList<String>, Void>  {
    private String ip;
    private int port;
    private DefaultListModel<String> model;

    public ListFilesTask(String ip, int port, DefaultListModel<String> model) {
        this.ip = ip;
        this.port = port;
        this.model = model;
    }
}
