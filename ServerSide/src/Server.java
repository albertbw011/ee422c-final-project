import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import com.mongodb.client.*;
import com.mongodb.*;
import org.bson.Document;

public class Server extends Observable {
    private static ServerSocket serverSocket;
    private static List<ClientThread> clients;
    private static Set<Item> auctionItemList;

    public static void main(String[] args) {
        new Server().startServer();
    }

    private void startServer() {
        try {
            retrieveAuctionItems();
            setUpNetworking();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpNetworking() throws Exception {
        serverSocket = new ServerSocket(4444);
        acceptClients();
    }

    private void retrieveAuctionItems() {
        auctionItemList = new HashSet<>();
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        MongoClient client = MongoClients.create(connectionString);
        MongoDatabase database = client.getDatabase("appdb");
        MongoCollection<Document> collection = database.getCollection("items");
        FindIterable<Document> itObj = collection.find();

        MongoCursor<Document> it = itObj.iterator();
        while (it.hasNext()) {
            Document auctionItem = it.next();
            Item item = new Item(auctionItem.getString("name"), auctionItem.getString("description"),
                    Double.parseDouble(auctionItem.getString("startingBid")), Double.parseDouble(auctionItem.getString("buyNowPrice")),
                    Integer.parseInt(auctionItem.getString("timeRemaining")));
            auctionItemList.add(item);
        }
        client.close();
    }

    private void acceptClients() {
        clients = new ArrayList<>();
        while(true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientThread client = new ClientThread(this, clientSocket);
                System.out.println("Connecting to..." + clientSocket);
                Thread thread = new Thread(client);
                this.addObserver(client);
                thread.start();
                clients.add(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void processRequest(Message request) {
        String command = request.command;
        Item auctionItem = request.auctionItem;
        Message sendMessage;
        switch (command) {
            case "buyNow":
                auctionItem.timeRemaining = 0;
                sendMessage = new Message("itemPurchased", auctionItem, auctionItem.buyNowPrice);
                for (ClientThread client : clients)
                    client.sendToClient(sendMessage);
                break;
            case "updateBid":
                sendMessage = new Message("updateItemBid", auctionItem, auctionItem.currentBid);
                for (ClientThread client : clients)
                    client.sendToClient(sendMessage);
                break;
        }
    }

    public Set<Item> getAuctionItemList() {
        return auctionItemList;
    }

    public List<ClientThread> getClients() {
        return clients;
    }
}
