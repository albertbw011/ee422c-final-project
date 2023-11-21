import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static int portNumber;
    private static ServerSocket serverSocket;
    public static List<ClientThread> clients;

    public static void main(String[] args) {
        new Server().startServer();
    }

    private void startServer() {
        portNumber = 4444;
        try {
            serverSocket = new ServerSocket(portNumber);
            acceptClients();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void acceptClients() {
        clients = new ArrayList<ClientThread>();
        while(true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientThread client = new ClientThread(clientSocket);
                Thread thread = new Thread(client);
                thread.start();
                clients.add(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
