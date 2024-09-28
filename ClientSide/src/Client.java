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
    private Stage primaryStage;
    private static Socket socket;
    private static ObjectInputStream inputStream;
    private static ObjectOutputStream outputStream;
    private static List<Item> auctionItemList;
    protected static String username;
    protected static LogInController logInController;
    protected static ClientController clientController;
    protected static BidController bidController;
    protected static BidHistoryController bidHistoryController;
    protected static AddItemController addItemController;
    protected static AudioClip buttonSound;
    protected static AudioClip loginSound;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initialize the auctionItemList ArrayList and load the sound files for the sounds being used in the program
     * Creates a new socket and also initializes the output and input streams
     * Loads up the Login Screen
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set. The primary stage will be embedded in
     * the browser if the application was launched as an applet.
     * Applications may create other stages, if needed, but they will not be
     * primary stages and will not be embedded in the browser.
     */
    public void start(Stage primaryStage) {
        auctionItemList = new ArrayList<>();
        buttonSound = new AudioClip(Objects.requireNonNull(Client.class.getResource("ui-click-97915.mp3")).toExternalForm());
        loginSound = new AudioClip(Objects.requireNonNull(Client.class.getResource("button-pressed-38129.mp3")).toExternalForm());
        this.primaryStage = primaryStage;
        try {
            setUpConnection();
            primaryStage.setScene(loginScene(primaryStage));
            readFromServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.show();
    }

    /**
     * Loads the login screen
     * @param primaryStage primaryStage
     * @return login scene
     * @throws IOException .
     */
    public static Scene loginScene(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Login to eHills");
        primaryStage.setResizable(false);
        FXMLLoader loginLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("Login.fxml")));
        Parent loginParent = loginLoader.load();
        logInController = loginLoader.getController();
        for (Item i : auctionItemList)
            i.displayed = false;
        return new Scene(loginParent, 1300, 800);
    }

    /**
     * Loads the auction client where users can bid on items
     * @param primaryStage primaryStage
     * @return auction client scene
     * @throws IOException .
     */
    public static Scene auctionScene(Stage primaryStage) throws IOException {
        primaryStage.setTitle("eHills");
        primaryStage.setResizable(true);
        FXMLLoader auctionLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("Auction.fxml")));
        Parent auctionParent = auctionLoader.load();
        clientController = auctionLoader.getController();
        clientController.setPrimaryStage(primaryStage);
        sendToServer(new Message("readyForItems"));
        return new Scene(auctionParent, 1600, 900);
    }

    /**
     * Loads a stage where the user can bid on the item
     * Has three automatically generated bid options, and can allow user to type in a custom valid amount
     * Built-in feature to only allow bids through that are valid numbers or greater than the current bid (even by 1 cent)
     * @param primaryStage primaryStage
     * @param item Auction item the scene is for
     * @return bid window
     * @throws IOException .
     */
    public static Scene placeBidScene(Stage primaryStage, Item item) throws IOException {
        primaryStage.setResizable(false);
        FXMLLoader placeBidLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("PlaceBid.fxml")));
        Parent placeBidParent = placeBidLoader.load();
        bidController = placeBidLoader.getController();
        bidController.setItem(item);
        bidController.setStage(primaryStage);
        return new Scene(placeBidParent, 700, 300);
    }

    /**
     * Confirmation scene when a user wants to buy now instead of bid on the item
     * Helps prevent dangerous mis-clicks
     * @param primaryStage primaryStage
     * @return confirmation scene
     * @throws IOException .
     */
    public static Scene confirmScene(Stage primaryStage) throws IOException {
        primaryStage.setResizable(false);
        Parent confirm = FXMLLoader.load(Objects.requireNonNull(Client.class.getResource("Confirm.fxml")));
        return new Scene(confirm, 500, 150);
    }

    /**
     * Scene with all the previous bids on an item, which includes the bidder on the item and the price at which it was bid at
     * @param primaryStage primaryStage
     * @param item item to show bid history for
     * @return Bid history scene
     * @throws IOException .
     */
    public static Scene bidHistoryScene(Stage primaryStage, Item item) throws IOException {
        primaryStage.setResizable(false);
        FXMLLoader bidHistoryLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("BidHistory.fxml")));
        Parent bidHistoryParent = bidHistoryLoader.load();
        bidHistoryController = bidHistoryLoader.getController();
        bidHistoryController.setItem(item);
        bidHistoryController.setStage(primaryStage);
        return new Scene(bidHistoryParent, 400, item.bidHistory.size()*45);
    }

    public static Scene addItemScene(Stage primaryStage) throws IOException {
        primaryStage.setResizable(false);
        FXMLLoader listLoader = new FXMLLoader(Objects.requireNonNull(Client.class.getResource("AddItem.fxml")));
        Parent listLoaderParent = listLoader.load();
        addItemController = listLoader.getController();
        return new Scene(listLoaderParent, 500, 500);
    }

    /**
     * Sets up connection
     * Creates socket and initializes input and output streams
     * @throws IOException .
     */
    public static void setUpConnection() throws IOException {
        socket = new Socket("localhost", 4444);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
        System.out.println("Connected to server");
    }

    /**
     * Send a message object to the server
     * Message object includes the command and either an Item object or Customer object
     * @param input Message to send
     */
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

    /**
     * Read message inputs from the server
     * @throws IOException .
     */
    public synchronized void readFromServer() throws IOException {
        Thread readerThread = new Thread (() -> {
            try {
                while (!socket.isClosed()) {
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
     * invalidPassword: user has inputted an invalid password and the error label adjusts accordingly
     * userDNE: user does not exist
     * usernameExists: username already exists for an account trying to be registered/logged in as a guest
     * validInput: no troubles checking with customer info database, so user can successfully log in
     * userExists: trying to register with an account that already exists
     * addItem: adds an auction item to the auction items list
     * itemPurchased: indicates to everyone the item has been successfully purchased and at its purchase price
     * itemEnded: indicates to everyone that the time has ran out for the item and can no longer be purchased
     * removeItem: removes an item from the item list and from the client view
     * updateItemBid: indicates that someone has bid on an item
     * updateItemTime: update the time remaining for an item
     * @param input
     */
    private synchronized void processRequest(Message input) {
        String command = input.getCommand();
        Item auctionItem = input.getAuctionItem();
        if (auctionItem != null) {
            auctionItem.totalBids = auctionItem.bidHistory != null ? auctionItem.bidHistory.size() : 0;
            if (auctionItem.totalBids > 0) {
                auctionItem.currentBidder = auctionItem.bidHistory.get(auctionItem.totalBids - 1).bidder;
                auctionItem.currentBid = auctionItem.bidHistory.get(auctionItem.totalBids - 1).bidPrice;
            }
        }
        VBox clientVBox = clientController != null ? clientController.getItemListPaneVBox() : new VBox();
        VBox notiBox = clientController != null ? clientController.getNotificationVBox() : new VBox();
        Label errorLabel = logInController != null ? logInController.getErrorLabel(): new Label();
        switch (command) {
            case "invalidPassword":
                Platform.runLater(() -> errorLabel.setText("Incorrect username or password"));
                break;
            case "userDNE":
                Platform.runLater(() -> errorLabel.setText("Account does not exist"));
                break;
            case "usernameExists":
                Platform.runLater(() -> errorLabel.setText("Username already exists"));
                break;
            case "validInput":
                Platform.runLater(() -> {
                    try {
                        loginSound.play();
                        primaryStage.setScene(auctionScene(primaryStage));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case "userExists":
                Platform.runLater(() -> errorLabel.setText("Account already exists"));
            case "addItem":
                if (auctionItem != null) {
                    auctionItemList.add(auctionItem);
                    Platform.runLater(() -> clientVBox.getChildren().add(auctionItem.display()));
                }
                break;
            case "itemPurchased":
                if (auctionItem != null) {
                    clientVBox.getChildren().stream()
                            .filter(node -> node instanceof HBox && auctionItem.name.equals(node.getId()))
                            .map(node -> (HBox) node)
                            .findFirst().ifPresent(hBox -> Platform.runLater(() -> {
                                clientVBox.getChildren().remove(hBox);
                                clientVBox.getChildren().add(auctionItem.display());
                                setLabelValue(auctionItem.display(), "#totalBidsLabel",
                                        String.format("%d Bid%s", auctionItem.totalBids, auctionItem.totalBids == 1 ? "" : "s"));
                                notiBox.getChildren().add(new Notification(auctionItem.currentBidder, auctionItem.currentBid,
                                        auctionItem, Notification.TYPE.PURCHASE).display());
                            }));
                }
                break;
            case "itemEnded":
                // removes the item and adds it to the end without bid/buy buttons
                // indicates its ended and/or sold
                if (auctionItem != null) {
                    clientVBox.getChildren().stream()
                            .filter(node -> node instanceof HBox && auctionItem.name.equals(node.getId()))
                            .map(node -> (HBox) node)
                            .findFirst().ifPresent(hBox -> Platform.runLater(() -> {
                                clientVBox.getChildren().remove(hBox);
                                clientVBox.getChildren().add(auctionItem.display());
                                setLabelValue(auctionItem.display(), "#totalBidsLabel",
                                        String.format("%d Bid%s", auctionItem.totalBids, auctionItem.totalBids == 1 ? "" : "s"));
                                if (auctionItem.totalBids == 0)
                                    notiBox.getChildren().add(new Notification(auctionItem).display());
                                else
                                    notiBox.getChildren().add(new Notification(auctionItem.currentBidder, auctionItem.currentBid,
                                            auctionItem, Notification.TYPE.END).display());
                            }));
                }
                break;
            case "removeItem":
                // remove item from database, item list, and from client
                if (auctionItem != null) {
                    for (int i = 0; i < auctionItemList.size(); i++) {
                        Item it = auctionItemList.get(i);
                        if (it.name.equals(auctionItem.name)) {
                            auctionItemList.remove(i);
                            break;
                        }
                    }
                    clientVBox.getChildren().stream()
                            .filter(node -> node instanceof HBox && auctionItem.name.equals(node.getId()))
                            .map(node -> (HBox) node)
                            .findFirst().ifPresent(hBox -> Platform.runLater(() -> clientVBox.getChildren().remove(hBox)));
                }
                break;
            case "updateItemBid":
                // edits the current bid price and edits the total number of bids
                if (auctionItem != null) {
                    clientVBox.getChildren().stream()
                            .filter(node -> node instanceof HBox && auctionItem.name.equals(node.getId()))
                            .map(node -> (HBox) node)
                            .findFirst()
                            .ifPresent(hBox -> Platform.runLater(() -> {
                                updateHBoxContents(hBox, auctionItem);
                                notiBox.getChildren().add(new Notification(auctionItem.currentBidder, auctionItem.currentBid,
                                        auctionItem.timeRemaining, auctionItem).display());
                            }));
                }
                break;
            case "updateItemTime":
                if (auctionItem != null) {
                    clientVBox.getChildren().stream()
                            .filter(node -> node instanceof HBox && auctionItem.name.equals(node.getId()))
                            .map(node -> (HBox) node)
                            .findFirst()
                            .ifPresent(hBox -> Platform.runLater(() -> updateTime(hBox, auctionItem)));
                }
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
            if (auctionItem.timeRemaining > 0 && !auctionItem.sold) {
                timeLabel.setText(Item.displayTime(auctionItem.timeRemaining) + " remaining");
            } else if (auctionItem.timeRemaining < 0 && !auctionItem.sold && !timeLabel.getText().equals("Ended")) {
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
}
