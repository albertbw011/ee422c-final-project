import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import org.bson.Document;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class Server extends Observable {
    private static ServerSocket serverSocket;
    private static List<ClientThread> clients;
    private static List<Item> auctionItemList;
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static ConnectionString connectionString;
    private static MongoCollection<Document> items;
    protected boolean update;
    public static void main(String[] args) {
        new Server().startServer();
    }

    private void startServer() {
        try {
            auctionItemList = new ArrayList<>();
            setUpDB();
            retrieveAuctionItems();
            ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
            ex.scheduleAtFixedRate(() -> {
                for (Item i : auctionItemList) {
                    i.decrementTimeRemaining();
                    updateDocument("timeRemaining", i, String.valueOf(i.getTimeRemaining()));
                    Message sendMessage = new Message("updateItemTime", i, i.getTimeRemaining());
                    setItemInList(i);
                    this.setChanged();
                    this.notifyObservers(sendMessage);
                }
                System.out.println("updated database");
                update = true;
            }, 1, 1, TimeUnit.SECONDS);
            setUpNetworking();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpNetworking() throws Exception {
        serverSocket = new ServerSocket(4444);
        acceptClients();
    }

    private void setUpDB() {
        connectionString = new ConnectionString("mongodb+srv://albertbw011:Albert447@auction.gmnwonc.mongodb.net/?retryWrites=true&w=majority");
        mongoClient = MongoClients.create(connectionString);
            try {
                // Send a ping to confirm a successful connection
                database = mongoClient.getDatabase("AuctionItems");
                items = database.getCollection("Items");
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException e) {
                e.printStackTrace();
            }
    }

    private void retrieveAuctionItems() {
        for (Document auctionItem : items.find()) {
            Item item = new Item(auctionItem.getString("name"), auctionItem.getString("description"),
                    Double.parseDouble(auctionItem.getString("startingBid")), Double.parseDouble(auctionItem.getString("buyNowPrice")),
                    Integer.parseInt(auctionItem.getString("timeRemaining")));
            auctionItemList.add(item);
        }
        update = false;
    }

    private void acceptClients() {
        clients = new ArrayList<>();
        try {
            while(true) {
                    Socket clientSocket = serverSocket.accept();
                    ClientThread client = new ClientThread(this, clientSocket);
                    System.out.println("Connecting to..." + clientSocket);
                    Thread thread = new Thread(client);
                    this.addObserver(client);
                    thread.start();
                    clients.add(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void processRequest(Message request) {
        String command = request.command;
        Item auctionItem = request.auctionItem;
        List<Item.BidInstance> bidHistory = auctionItem.bidHistory;
        Message sendMessage;
        switch (command) {
            case "buyNow":
                auctionItem.timeRemaining = 0;
                sendMessage = new Message("itemPurchased", auctionItem, auctionItem.buyNowPrice);
                updateDocument("sold", auctionItem, String.valueOf(true));
                updateDocumentBidHistory(auctionItem, bidHistory.get(bidHistory.size()-1));
                break;
            case "updateBid":
                sendMessage = new Message("updateItemBid", auctionItem, auctionItem.currentBid);
                updateDocument("currentBid", auctionItem, String.valueOf(auctionItem.currentBid));
                updateDocumentBidHistory(auctionItem, bidHistory.get(bidHistory.size()-1));
                break;
            case "itemEnded":
                if (!auctionItem.bidHistory.isEmpty()) {
                    auctionItem.buyer = auctionItem.bidHistory.get(auctionItem.bidHistory.size() - 1).bidder;
                    auctionItem.soldPrice = auctionItem.bidHistory.get(auctionItem.bidHistory.size() - 1).bidPrice;
                }
                sendMessage = new Message("itemEnded", auctionItem);
                break;
            default:
                sendMessage = null;
        }
        setItemInList(auctionItem);
        this.setChanged();
        this.notifyObservers(sendMessage);
    }

    public void updateDocument(String field, Item item, String updatedValue) {
        items.updateOne(eq("name", item.name), set(field, updatedValue));
    }

    public void updateDocumentBidHistory(Item item, Item.BidInstance bidInstance) {
        Document doc = new Document()
                .append("bidder", bidInstance.bidder)
                .append("bidPrice", bidInstance.bidPrice)
                .append("timeRemaining", bidInstance.timeRemaining)
                .append("purchased", bidInstance.purchased);
        items.updateOne(eq("name", item.name), push("bidHistory", doc));
    }

    private List<Item.BidInstance> retrieveItemBidHistory() {
        List<Item.BidInstance> bidInstanceList = new ArrayList<>();
        for (Document auctionItem : items.find())
            bidInstanceList.add(Item.BidInstance.fromDocument(auctionItem));
        return bidInstanceList;
    }

    private void setItemInList(Item item) {
        for (int i = 0; i < auctionItemList.size(); i++) {
            Item listItem = auctionItemList.get(i);
            if (listItem.name.equals(item.name)) {
                auctionItemList.set(i, item);
                break;
            }
        }
    }

    public List<Item> getAuctionItemList() {
        return auctionItemList;
    }
}
