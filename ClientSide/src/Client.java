import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client extends Application {

    private static PrintWriter printWriter;
    private static BufferedReader bufferedReader;
    private static Socket socket;
    private static int portNumber;
    private List<CustomerInstance> customerHistory;
    private List<Item> auctionItemList;

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
        primaryStage.setTitle("Login to eHills");
        primaryStage.setResizable(false);
        Parent login = FXMLLoader.load(Client.class.getResource("Login.fxml"));
        Controller.setPrimaryStage(primaryStage);
        return new Scene(login, 1300, 800);
    }

    public static Scene auctionScene(Stage primaryStage) throws IOException {
        primaryStage.setTitle("eHills");
        primaryStage.setResizable(true);
        Parent auction = FXMLLoader.load(Client.class.getResource("Auction.fxml"));
        Controller.setPrimaryStage(primaryStage);
        return new Scene(auction, 1600, 900);
    }

    public static void setUpConnection() {
        portNumber = 4444;
        try {
            socket = new Socket("localhost", portNumber);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Unable to connect");
        }
    }

    private static void sendToServer(String input) {
        printWriter.println(input);
        printWriter.flush();
    }

    private static void processRequest(String command) {

    }

    public List<Item> getAuctionItemList() {
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
