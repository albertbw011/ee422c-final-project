import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.*;
import javafx.scene.media.AudioClip;

public class Client extends Application {
    private static ObjectInputStream inputStream;
    private static ObjectOutputStream outputStream;
    private static Socket socket;
    private static List<CustomerInstance> customerHistory;
    private static List<Item> auctionItemList;
    protected static String username;
    private static FXMLLoader auctionLoader;
    protected static LogInController logInController;
    protected static ClientController clientController;
    protected static BidController bidController;
    protected static BidHistoryController bidHistoryController;
    protected static AudioClip buttonSound;
    protected static AudioClip loginSound;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        customerHistory = new ArrayList<>();
        auctionItemList = new ArrayList<>();
        buttonSound = new AudioClip(Objects.requireNonNull(Client.class.getResource("ui-click-97915.mp3")).toExternalForm());
        loginSound = new AudioClip(Objects.requireNonNull(Client.class.getResource("button-pressed-38129.mp3")).toExternalForm());
        try {
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
        FXMLLoader loginLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("Login.fxml")));
        Parent loginParent = loginLoader.load();
        logInController = loginLoader.getController();
        logInController.setPrimaryStage(primaryStage);
        return new Scene(loginParent, 1300, 800);
    }

    public static Scene auctionScene(Stage primaryStage) throws IOException {
        primaryStage.setTitle("eHills");
        primaryStage.setResizable(true);
        auctionLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("Auction.fxml")));
        Parent auctionParent = auctionLoader.load();
        clientController = auctionLoader.getController();
        clientController.setPrimaryStage(primaryStage);
        setUpConnection();
        return new Scene(auctionParent, 1600, 900);
    }

    public static Scene placeBidScene(Stage primaryStage, Item item) throws IOException {
        primaryStage.setResizable(false);
        FXMLLoader placeBidLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("PlaceBid.fxml")));
        Parent placeBidParent = placeBidLoader.load();
        bidController = placeBidLoader.getController();
        bidController.setItem(item);
        bidController.setStage(primaryStage);
        return new Scene(placeBidParent, 700, 300);
    }

    public static Scene confirmScene(Stage primaryStage) throws IOException {
        primaryStage.setResizable(false);
        Parent confirm = FXMLLoader.load(Objects.requireNonNull(Client.class.getResource("Confirm.fxml")));
        return new Scene(confirm, 500, 150);
    }

    public static Scene bidHistoryScene(Stage primaryStage, Item item) throws IOException {
        primaryStage.setResizable(false);
        FXMLLoader bidHistoryLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("BidHistory.fxml")));
        Parent bidHistoryParent = bidHistoryLoader.load();
        bidHistoryController = bidHistoryLoader.getController();
        bidHistoryController.setItem(item);
        bidHistoryController.setStage(primaryStage);
        return new Scene(bidHistoryParent, 400, item.bidHistory.size()*50);
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

    protected static void sendToServer(Message input) {
        try {
            outputStream.reset();
            outputStream.writeUnshared(input);
            outputStream.flush();
            System.out.println("Sending to server: " + input.command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFromServer() throws IOException {
        Thread readerThread = new Thread (() -> {
            try {
                while (true) {
                    Message input = (Message) inputStream.readUnshared();
                    processRequest(input);
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        });
        readerThread.start();
    }

    /**
     * Possible command list:
     * addItem, item: adds numItems auction items to the auction items list
     * itemPurchased, item: indicates to everyone the item has been successfully purchased and at what price
     * itemEnded, item: indicates to everyone that the time has ran out for the item and can no longer be purchased
     * updateItemBid, item: indicates that the bid for an item has increased
     * @param input
     */
    private synchronized void processRequest(Message input) {
        String command = input.getCommand();
        Item auctionItem = Objects.requireNonNull(input.getAuctionItem());
        auctionItem.totalBids = auctionItem.bidHistory.size();
        ClientController controller = auctionLoader.getController();
        VBox clientVBox = controller.getItemListPaneVBox();
        switch (command) {
            case "addItem":
                auctionItemList.add(auctionItem);
                break;
            case "itemPurchased":
            case "itemEnded":
                // removes the item and adds it to the end without bid/buy buttons
                // indicates its ended and/or sold
                clientVBox.getChildren().stream()
                        .filter(node -> node instanceof HBox && auctionItem.name.equals(node.getId()))
                        .map(node -> (HBox) node)
                        .findFirst().ifPresent(hBox -> Platform.runLater(() -> {
                            clientVBox.getChildren().remove(hBox);
                            clientVBox.getChildren().add(auctionItem.display());
                            setLabelValue(auctionItem.display(), "#totalBidsLabel",
                                    String.format("%d Bid%s", auctionItem.totalBids, auctionItem.totalBids == 1 ? "" : "s"));
                        }));
                break;
            case "updateItemBid":
                // edits the current bid price and edits the total number of bids
                clientVBox.getChildren().stream()
                        .filter(node -> node instanceof HBox && auctionItem.name.equals(node.getId()))
                        .map(node -> (HBox) node)
                        .findFirst()
                        .ifPresent(hBox -> Platform.runLater(() -> updateHBoxContents(hBox, auctionItem)));
                break;
            case "updateItemTime":
                clientVBox.getChildren().stream()
                        .filter(node -> node instanceof HBox && auctionItem.name.equals(node.getId()))
                        .map(node -> (HBox) node)
                        .findFirst()
                        .ifPresent(hBox -> Platform.runLater(() -> updateTime(hBox, auctionItem)));
                break;
        }
    }

    private static void updateHBoxContents(HBox hBox, Item auctionItem) {
        setLabelValue(hBox, "#currentItemPriceLabel", String.format("Current Bid: $%.2f", auctionItem.currentBid));
        setLabelValue(hBox, "#totalBidsLabel", String.format("%d Bid%s", auctionItem.totalBids, auctionItem.totalBids == 1 ? "" : "s"));
        setButtonAction(hBox, "#placeBidButton", event -> openBidStage(auctionItem));
        setButtonAction(hBox, "#buyNowButton", event -> openConfirmStage(auctionItem));
    }

    private static void updateTime(HBox hBox, Item auctionItem) {
        Label timeLabel = (Label) hBox.lookup("#timeRemainingLabel");
        if (timeLabel != null) {
            if (auctionItem.timeRemaining < 600)
                timeLabel.setTextFill(Color.RED);
            if (auctionItem.timeRemaining > 0) {
                timeLabel.setText(Item.displayTime(auctionItem.timeRemaining) + " remaining");
            } else if (auctionItem.timeRemaining < 0 && !auctionItem.sold) {
                timeLabel.setText("Ended");
                sendToServer(new Message("itemEnded", auctionItem));
            }
        }
    }

    private static void setLabelValue(HBox hBox, String labelId, String text) {
        Label label = (Label) hBox.lookup(labelId);
        if (label != null) {
            label.setText(text);
        }
    }

    private static void setButtonAction(HBox hBox, String buttonId, EventHandler<ActionEvent> eventHandler) {
        Button button = (Button) hBox.lookup(buttonId);
        if (button != null) {
            button.setOnAction(eventHandler);
        }
    }

    protected static void openBidStage(Item auctionItem) {
        try {
            Stage bidStage = new Stage();
            bidStage.setTitle("Place bid for " + auctionItem.name);
            bidStage.setScene(Client.placeBidScene(bidStage, auctionItem));
            bidController.setStage(bidStage);
            bidStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void openConfirmStage(Item auctionItem) {
        Stage newStage = new Stage();
        try {
            ConfirmController.setStage(newStage);
            ConfirmController.setItem(auctionItem);
            newStage.setScene(confirmScene(newStage));
            newStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendBid(double bidAmount, Item item) {
        if (bidAmount >= item.buyNowPrice) {
            item.buyer = username;
            item.soldPrice = item.buyNowPrice;
            Message sendMessage = new Message("buyNow", item);
            sendToServer(sendMessage);
        } else {
            item.currentBidder = username;
            item.currentBid = bidAmount;
            Message sendMessage = new Message("updateBid", item);
            sendToServer(sendMessage);
        }
    }

    public static List<Item> getAuctionItemList() {
        return auctionItemList;
    }

    static class CustomerInstance {
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
