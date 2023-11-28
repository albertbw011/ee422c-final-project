import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import org.bson.Document;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class Server extends Observable {
    private ServerSocket serverSocket;
    private List<Item> auctionItemList;
    private Map<String, String> customerList;
    private MongoCollection<Document> items;
    private MongoCollection<Document> customers;
    protected boolean update;
    public static void main(String[] args) {
        new Server().startServer();
    }

    private void startServer() {
        try {
            auctionItemList = new ArrayList<>();
            customerList = new HashMap<>();
            setUpDB();
            retrieveAuctionItems();
            retrieveCustomerInfo();
            ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
            ex.scheduleAtFixedRate(() -> {
                this.updateAuctionItemList();
                for (Item i : auctionItemList) {
                    if (i.timeRemaining > -2) {
                        i.decrementTimeRemaining();
                        updateDocument("timeRemaining", i, i.getTimeRemaining());
                        Message sendMessage = new Message("updateItemTime", i);
                        this.setChanged();
                        this.notifyObservers(sendMessage);
                    }
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
        ConnectionString connectionString = new ConnectionString("mongodb+srv://albertbw011:Albert447@auction.gmnwonc.mongodb.net/?retryWrites=true&w=majority");
        MongoClient mongoClient = MongoClients.create(connectionString);
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("AuctionItems");
                items = database.getCollection("Items");
                customers = database.getCollection("Customers");
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException e) {
                e.printStackTrace();
            }
    }

    private void retrieveCustomerInfo() {
        for (Document customer : customers.find())
            customerList.put(customer.getString("username"), customer.getString("password"));
    }

    public void retrieveAuctionItems() {
        for (Document auctionItem : items.find()) {
            Item item = new Item(auctionItem.getString("name"), auctionItem.getString("description"),
                    auctionItem.getDouble("startingBid"), auctionItem.getDouble("buyNowPrice"),
                    auctionItem.getInteger("timeRemaining"), auctionItem.getDouble("currentBid"),
                    retrieveItemBidHistoryFromDB(auctionItem.getString("name")));
            auctionItemList.add(item);
        }
        update = false;
    }

    public void updateAuctionItemList() {
        for (Document auctionItem : items.find()) {
            Item newItem = new Item(auctionItem.getString("name"), auctionItem.getString("description"),
                    auctionItem.getDouble("startingBid"), auctionItem.getDouble("buyNowPrice"),
                    auctionItem.getInteger("timeRemaining"), auctionItem.getDouble("currentBid"),
                    retrieveItemBidHistoryFromDB(auctionItem.getString("name")));
            int initialIndex = returnIndex(newItem.name);
            auctionItemList.set(initialIndex, newItem);
        }
        update = false;
    }

    private int returnIndex(String itemName) {
        for (int i = 0; i < auctionItemList.size(); i++) {
            Item it = auctionItemList.get(i);
            if (it.name.equals(itemName))
                return i;
        }
        return -1;
    }

    private void acceptClients() {
        List<ClientThread> clients = new ArrayList<>();
        try {
            while(true) {
                this.updateAuctionItemList();
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
        List<Item.BidInstance> bidHistory = auctionItem != null ? auctionItem.bidHistory : new ArrayList<>();
        Message sendMessage = new Message("");
        switch (command) {
            case "checkCustomer":
                if (request.customer != null) {
                    Customer customer = request.customer;
                    if (customerList.containsKey(customer.getUsername()) && !customerList.get(customer.getUsername()).equals(customer.getPassword()))
                        sendMessage = new Message("invalidPassword");
                    else if (!customerList.containsKey(customer.getUsername()))
                        sendMessage = new Message("userDNE");
                    else
                        sendMessage = new Message("validInput");
                }
                break;
            case "addCustomer":
                if (request.customer != null)
                    addCustomer(request.customer);
                break;
            case "buyNow":
                auctionItem.timeRemaining = 0;
                auctionItem.sold = true;
                sendMessage = new Message("itemPurchased", auctionItem);
                updateDocument("sold", auctionItem, true);
                updateDocument("buyer", auctionItem, auctionItem.buyer);
                updateDocumentBidHistory(auctionItem, bidHistory.get(bidHistory.size()-1));
                break;
            case "updateBid":
                sendMessage = new Message("updateItemBid", auctionItem);
                updateDocument("currentBid", auctionItem, auctionItem.currentBid);
                updateDocumentBidHistory(auctionItem, bidHistory.get(bidHistory.size()-1));
                break;
            case "itemEnded":
                if (!auctionItem.bidHistory.isEmpty()) {
                    auctionItem.buyer = bidHistory.get(bidHistory.size() - 1).bidder;
                    auctionItem.soldPrice = bidHistory.get(bidHistory.size() - 1).bidPrice;
                }
                if (!auctionItem.sold) {
                    auctionItem.sold = true;
                    sendMessage = new Message("itemEnded", auctionItem);
                }
                break;
        }
        this.setChanged();
        this.notifyObservers(sendMessage);
    }

    public void updateDocument(String field, Item item, Object updatedValue) {
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

    public void addCustomer(Customer customer) {
        Document doc = new Document().append("username", customer.getUsername()).append("password", customer.getPassword());
        customers.insertOne(doc);
    }

    private List<Item.BidInstance> retrieveItemBidHistoryFromDB(String name) {
        for (Document d : items.find()) {
            if (d.getString("name").equals(name))
                return Item.BidInstance.fromDocument(d);
        }
        return null;
    }

    public List<Item> getAuctionItemList() {
        return auctionItemList;
    }
}
