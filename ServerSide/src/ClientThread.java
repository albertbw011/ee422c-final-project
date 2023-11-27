import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ClientThread implements Runnable, Observer {
    private final Socket socket;
    private final Server server;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private List<Item> auctionItemList;

    public ClientThread(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.auctionItemList = server.getAuctionItemList();
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
        server.update = true;
    }
    public void run() {
        try {
            System.out.println("ClientThread running");
            for (Item item : auctionItemList) {
                Message send = new Message("addItem", item);
                sendToClient(send);
            }
            while (!socket.isClosed()) {
                if (server.update) {
                    for (Item item : auctionItemList) {
                        Message newM = new Message("updateItemTime", item);
                        sendToClient(newM);
                    }
                    server.update = false;
                }
                Message receivedMessage = (Message) inputStream.readObject();
                server.processRequest(receivedMessage);
            }
        } catch (ClassNotFoundException | IOException e ) {
            e.printStackTrace();
        }
    }

    public void sendToClient(Object o) {
        try {
            System.out.println("Sending to client: " + o);
            this.outputStream.reset();
            this.outputStream.writeObject(o);
            this.outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        this.sendToClient(arg);
    }
}
