import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
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
        return new Scene(confirm, 500, 150);
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
        Item auctionItem = Objects.requireNonNull(input.getAuctionItem());
        ClientController controller = auctionLoader.getController();
        VBox clientVBox = controller.getItemListPaneVBox();
        switch (command) {
            case "addItem":
                auctionItem.itemID = itemID;
                itemID++;
                auctionItemList.add(auctionItem);
                break;
            case "itemPurchased":
                clientVBox.getChildren().stream()
                        .filter(node -> node instanceof HBox && auctionItem.name.equals(node.getId()))
                        .map(node -> (HBox) node)
                        .findFirst().ifPresent(hBoxToRemove -> Platform.runLater(() -> {
                            clientVBox.getChildren().remove(hBoxToRemove);
                            clientVBox.getChildren().add(auctionItem.display());
                        }));
                break;
            case "updateItemBid":
                clientVBox.getChildren().stream()
                        .filter(node -> node instanceof HBox && auctionItem.name.equals(node.getId()))
                        .map(node -> (HBox) node)
                        .findFirst().ifPresent(hBox -> Platform.runLater(() -> {
                            Label editLabel = (Label) hBox.lookup("#currentItemPriceLabel");
                            editLabel.setText(String.format("Current Bid: $%.2f", auctionItem.currentBid));
                        }));
                break;
        }
    }

    public void sendBid(double bidAmount, Item item) {
        synchronized (item) {
            if (bidAmount >= item.buyNowPrice) {
                item.buyer = username;
                item.soldPrice = item.buyNowPrice;
                Message sendMessage = new Message("buyNow", item);
                try {
                    sendToServer(sendMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                item.currentBid = bidAmount;
                Message sendMessage = new Message("updateBid", item);
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
