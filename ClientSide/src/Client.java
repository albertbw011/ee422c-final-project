import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client extends Application {
    private static ObjectInputStream inputStream;
    private static ObjectOutputStream outputStream;
    private static Socket socket;
    private List<CustomerInstance> customerHistory;
    private static List<Item> auctionItemList;

    public Client() {
        this.customerHistory = new ArrayList<>();
        this.auctionItemList = new ArrayList<>();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
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
        Parent login = FXMLLoader.load(Objects.requireNonNull(Client.class.getResource("Login.fxml")));
        LogInController.setPrimaryStage(primaryStage);
        return new Scene(login, 1300, 800);
    }

    public static Scene auctionScene(Stage primaryStage) throws IOException {
        primaryStage.setTitle("eHills");
        primaryStage.setResizable(true);
        Parent auction = FXMLLoader.load(Objects.requireNonNull(Client.class.getResource("Auction.fxml")));
        ClientController.setPrimaryStage(primaryStage);
        setUpConnection();
        return new Scene(auction, 1600, 900);
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
     * addItems + numItems : adds numItems auction items to the auction items list
     * @param input
     */
    private void processRequest(Message input) {
        String command = input.getCommand();
        switch (command) {
            case "addItem":
                if (input.getAuctionItem() != null) {
                    Item auctionItem = input.getAuctionItem();
                    auctionItemList.add(auctionItem);
                }
                break;
        }
    }

    public static List<Item> getAuctionItemList() {
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
