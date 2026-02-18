package server;

import java.io.*;
import java.net.Socket;

public class ClientSession implements Runnable {

    private Socket socket;

    public ClientSession(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        handleClient();
    }

    private void handleClient() {
        try (
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {

            String command = readCommand(in);
            processCommand(command, out);

        } catch (Exception e) {
            System.err.println("Erreur client : " + e.getMessage());
        } finally {
            closeSocket();
        }
    }

    private String readCommand(DataInputStream in) throws IOException {
        return in.readUTF();
    }

    private void processCommand(String command, DataOutputStream out) throws IOException {
        CommandProcessor processor = new CommandProcessor();
        processor.execute(command, out);
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
