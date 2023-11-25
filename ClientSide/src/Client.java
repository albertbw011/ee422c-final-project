import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client extends Application {
    protected static Stage primaryStage;
    private static ObjectInputStream inputStream;
    private static ObjectOutputStream outputStream;
    private static Socket socket;
    private static List<CustomerInstance> customerHistory;
    private static Set<Item> auctionItemList;
    protected static String username;
    private int itemID;
    private static FXMLLoader loginLoader;
    private static FXMLLoader auctionLoader;
    private static FXMLLoader placeBidLoader;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        try {
            customerHistory = new ArrayList<>();
            auctionItemList = new HashSet<>();
            itemID = 0;
            Client.primaryStage = primaryStage;
            primaryStage.setScene(loginScene(primaryStage));
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.show();
    }

    public static Scene loginScene(Stage primaryStage) throws IOException {
        if (socket != null && !socket.isClosed())
            socket.close();
        primaryStage.setTitle("Login to eHills");
        primaryStage.setResizable(false);
        loginLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("Login.fxml")));
        LogInController.setPrimaryStage(primaryStage);
        return new Scene(loginLoader.load(), 1300, 800);
    }

    public static Scene auctionScene(Stage primaryStage) throws IOException {
        primaryStage.setTitle("eHills");
        primaryStage.setResizable(true);
        auctionLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("Auction.fxml")));
        ClientController.setPrimaryStage(primaryStage);
        setUpConnection();
        return new Scene(auctionLoader.load(), 1600, 900);
    }

    public static Scene placeBidScene(Stage primaryStage) throws IOException {
        primaryStage.setResizable(false);
        placeBidLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("PlaceBid.fxml")));
        return new Scene(placeBidLoader.load(), 700, 300);
    }

    public static Scene confirmScene(Stage primaryStage) throws IOException {
        primaryStage.setResizable(false);
        Parent confirm = FXMLLoader.load(Objects.requireNonNull(Client.class.getResource("Confirm.fxml")));
        return new Scene(confirm, 500, 200);
    }

    public static void setUpConnection() {
        try {
            socket = new Socket("localhost", 4444);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Unable to connect");
        }
    }

    private static void sendToServer(Object input) throws IOException {
        outputStream.reset();
        outputStream.writeObject(input);
        outputStream.flush();
    }

    public void readFromServer() throws IOException {
        Thread readerThread = new Thread (() -> {
            try {
                while (true) {
                    Message input = (Message) inputStream.readObject();
                    processRequest(input);
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        });

        Thread writerThread = new Thread (() -> {
            while (true) {

            }
        });

        readerThread.start();
        writerThread.start();
    }

    /**
     * Possible command list:
     * addItem, item: adds numItems auction items to the auction items list
     * itemPurchased, item: indicates to everyone the item has been successfully purchased and at what price
     * @param input
     */
    private void processRequest(Message input) {
        String command = input.getCommand();
        switch (command) {
            case "addItem":
                if (input.getAuctionItem() != null) {
                    Item auctionItem = input.getAuctionItem();
                    auctionItem.itemID = itemID;
                    itemID++;
                    auctionItemList.add(auctionItem);
                }
                break;
            case "itemPurchased":
                if (input.getAuctionItem() != null) {
                    Item auctionItem = input.getAuctionItem();
                    ClientController controller = auctionLoader.getController();
                    VBox clientVBox = controller.getItemListPaneVBox();
                    Node nodeToRemove = clientVBox.lookup(auctionItem.name);
                    if (nodeToRemove != null) {
                        clientVBox.getChildren().remove(nodeToRemove);
                        clientVBox.getChildren().add(auctionItem.display());
                    }
                }
                break;
        }
    }

    public void sendBid(double bidAmount, Item item) {
        synchronized (item) {
            if (bidAmount >= item.buyNowPrice) {
                item.buyer = username;
                Message sendMessage = new Message("buyNow", item);
                try {
                    sendToServer(sendMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Set<Item> getAuctionItemList() {
        return auctionItemList;
    }

    class CustomerInstance {
        String itemName;
        double bidPrice;
        boolean inProgress;
        boolean purchased;
        public CustomerInstance(String itemName, double bidPrice, boolean inProgress, boolean purchased) {
            this.itemName = itemName;
            this.bidPrice = bidPrice;
            this.inProgress = inProgress;
            this.purchased = purchased;
        }
    }
}
