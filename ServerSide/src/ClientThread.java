import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class ClientThread implements Runnable, Observer {
    private Socket socket;
    private Server server;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private boolean updateItems;
    private Set<Item> auctionItemList;

    public ClientThread(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.updateItems = true;
        this.auctionItemList = server.getAuctionItemList();
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }
    public void run() {
        try {
            System.out.println("ClientThread running");
            for (Item item : auctionItemList) {
                Message send = new Message("addItem", item);
                sendToClient(send);
            }
            while (!socket.isClosed()) {
                Message receivedMessage = (Message) inputStream.readObject();
                server.processRequest(receivedMessage);
            }

        } catch (ClassNotFoundException | IOException e) {
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
        this.sendToClient((String) arg);
    }
}
