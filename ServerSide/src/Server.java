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
import org.mindrot.jbcrypt.BCrypt;

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
                    i.decrementTimeRemaining();
                    updateDocument("timeRemaining", i, i.getTimeRemaining());
                    Message sendMessage = new Message("");
                    if (i.timeRemaining < -600) {
                        removeItem(i);
                        sendMessage = new Message("removeItem", i);
                    } else {
                        sendMessage = new Message("updateItemTime", i);
                    }
                    this.setChanged();
                    this.notifyObservers(sendMessage);
                }
                System.out.println("updated database");
                update = true;
            }, 0, 1, TimeUnit.SECONDS);
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
            customerList.put(customer.getString("username"), BCrypt.hashpw(customer.getString("password"), BCrypt.gensalt()));
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
        try {
            while(true) {
                Socket clientSocket = serverSocket.accept();
                ClientThread client = new ClientThread(this, clientSocket);
                System.out.println("Connecting to..." + clientSocket);
                Thread thread = new Thread(client);
                this.addObserver(client);
                thread.start();
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
                    Customer customer = new Customer(request.customer.getUsername(), BCrypt.hashpw(request.customer.getPassword(), BCrypt.gensalt()));
                    if (customerList.containsKey(customer.getUsername()) && getCustomerHash(customer))
                        sendMessage = new Message("invalidPassword");
                    else if (!customerList.containsKey(customer.getUsername()))
                        sendMessage = new Message("userDNE");
                    else
                        sendMessage = new Message("validInput");
                }
                break;
            case "checkGuest":
                if (request.customer != null) {
                    if (customerList.containsKey(request.customer.getUsername()))
                        sendMessage = new Message("usernameExists");
                    else
                        sendMessage = new Message("validInput");
                }
                break;
            case "addCustomer":
                if (request.customer != null) {
                    if (!customerList.containsKey(request.customer.getUsername())) {
                        addCustomer(new Customer(request.customer.getUsername(), BCrypt.hashpw(request.customer.getPassword(), BCrypt.gensalt())));
                        retrieveCustomerInfo();
                        sendMessage = new Message("validInput");
                    } else
                        sendMessage = new Message("userExists");
                }
                break;
            case "buyNow":
                if (auctionItem != null) {
                    auctionItem.timeRemaining = 0;
                    auctionItem.sold = true;
                    sendMessage = new Message("itemPurchased", auctionItem);
                    updateDocument("sold", auctionItem, true);
                    updateDocument("buyer", auctionItem, auctionItem.buyer);
                    updateDocumentBidHistory(auctionItem, bidHistory.get(bidHistory.size() - 1));
                }
                break;
            case "updateBid":
                if (auctionItem != null) {
                    sendMessage = new Message("updateItemBid", auctionItem);
                    updateDocument("currentBid", auctionItem, auctionItem.currentBid);
                    updateDocumentBidHistory(auctionItem, bidHistory.get(bidHistory.size() - 1));
                }
                break;
            case "itemEnded":
                if (auctionItem != null) {
                    if (!auctionItem.bidHistory.isEmpty()) {
                        auctionItem.buyer = bidHistory.get(bidHistory.size() - 1).bidder;
                        auctionItem.soldPrice = bidHistory.get(bidHistory.size() - 1).bidPrice;
                    }
                    if (!auctionItem.sold) {
                        auctionItem.sold = true;
                        sendMessage = new Message("itemEnded", auctionItem);
                    }
                }
                break;
            case "readyForItems":
                for (Item item : auctionItemList) {
                    Message send = new Message("addItem", item);
                    this.setChanged();
                    this.notifyObservers(send);
                }
                break;
            case "addItem":
                if (auctionItem != null) {
                    addItem(auctionItem);
                    sendMessage = new Message("addItem", auctionItem);
                    update = true;
                }
                break;
        }
        this.setChanged();
        this.notifyObservers(sendMessage);
    }

    private boolean getCustomerHash(Customer customer) {
        String username = customer.getUsername();
        for (String user : customerList.keySet()) {
            if (user.equals(username))
                return customer.getPassword().equals(customerList.get(user));
        }
        return false;
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

    public void addItem(Item item) {
        auctionItemList.add(item);
        Document doc = new Document().append("name", item.name).
                append("description", item.description).
                append("startingBid", item.startingBid).
                append("buyNowPrice", item.buyNowPrice).
                append("timeRemaining", item.timeRemaining);
        items.insertOne(doc);
    }

    private void removeItem(Item item) {
        items.deleteOne(new Document("name", item.name));
        auctionItemList.remove(auctionItemList.get(returnIndex(item.name)));
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

    public void removeObserver(ClientThread observer) {
        deleteObserver(observer);
    }
}
