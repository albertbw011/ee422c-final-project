import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ClientThread implements Runnable, Observer {
    private final Socket socket;
    private final Server server;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private final List<Item> auctionItemList;

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
            server.updateAuctionItemList();
            while (!socket.isClosed()) {
                if (server.update) {
                    for (Item item : auctionItemList) {
                        Message newM = new Message("updateItemTime", item);
                        sendToClient(newM);
                    }
                    server.update = false;
                }
                Message receivedMessage = (Message) inputStream.readUnshared();
                if (receivedMessage.command.equals("close")) {
                    try {
                        disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    server.processRequest(receivedMessage);
                }
            }
        } catch (ClassNotFoundException | IOException e ) {
            e.printStackTrace();
        }
    }

    public synchronized void sendToClient(Message m) {
        try {
            if (!m.command.equals("updateItemTime"))
                System.out.println("Sending to client: " + m);
            this.outputStream.reset();
            this.outputStream.writeUnshared(m);
            this.outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        this.sendToClient((Message) arg);
    }

    private void disconnect() throws IOException {
        this.socket.close();
        server.removeObserver(this);
    }
}
