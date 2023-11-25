import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import java.io.IOException;
import java.io.Serializable;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class Item extends Client implements Serializable {
    private static final long serialVersionUID = 1L;
    int itemID;
    String name;
    String buyer;
    String description;
    double soldPrice;
    double startingBid;
    double buyNowPrice;
    int timeRemaining; // this will be in seconds
    double currentBid;
    int totalBids;
    List<BidInstance> bidHistory;
    boolean sold;
    Image image;
    boolean displayed;


    public Item(String name, String description, String imagePath, double minBid, double buyNow, int time) {
        this.name = name;
        this.description = description;
        this.startingBid = minBid;
        this.buyNowPrice = buyNow;
        this.timeRemaining = time;
        this.currentBid = (startingBid > 0) ? startingBid : 0;
        bidHistory = new ArrayList<>();
        totalBids = 0;
        sold = false;
        this.image = new Image(imagePath);
        this.displayed = false;
    }

    public HBox display() {
        HBox hBox = new HBox();
        VBox leftBox = new VBox(5);
        VBox descriptionBox = new VBox(5);
        VBox rightBox = new VBox(5);
        rightBox.setAlignment(Pos.CENTER);
        ImageView imageView = new ImageView(image);

        // Item Name
        Label itemName = new Label(name);
        itemName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 25));

        // Sold Label
        Label soldLabel = new Label(String.format("Purchased by %s at $%.2f", buyer, soldPrice));
        soldLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        soldLabel.setTextFill(Color.RED);

        // Current Bid
        Label currentItemPriceLabel = new Label(String.format("Current Bid: $%.2f", currentBid));
        currentItemPriceLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        currentItemPriceLabel.setId("currentItemPriceLabel");

        // Buy Now Price
        Label buyNowLabel = new Label(String.format("Buy Now: $%.2f", buyNowPrice));
        buyNowLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));

        // Time Remaining in hh:mm:ss
        Label timeRemainingLabel = new Label(displayTime(timeRemaining) + " remaining");
        timeRemainingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        timeRemainingLabel.setTextFill(Color.RED);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0), event -> {
            if (timeRemaining > 0) {
                timeRemainingLabel.setText(displayTime(timeRemaining) + " remaining");
                timeRemaining--;
            } else {
                timeRemainingLabel.setText("Ended");
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Description
        Label descriptionLabel = new Label("About this item: " + ((description != null) ? description : ""));
        descriptionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        descriptionBox.getChildren().add(descriptionLabel);

        // Place Bid Button
        Button placeBidButton = new Button("Place Bid");
        placeBidButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        placeBidButton.setStyle("-fx-background-color: #42a1f5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26;");
        placeBidButton.setOnAction(event -> {
            try {
                BidController.setItem(this);
                Stage bidStage = new Stage();
                bidStage.setTitle("Place bid for " + this.name);
                bidStage.setScene(Client.placeBidScene(bidStage));
                BidController.setStage(bidStage);
                bidStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        placeBidButton.setOnMouseEntered(event -> placeBidButton.setStyle("-fx-background-color: #90c8f5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26; -fx-cursor: hand;"));
        placeBidButton.setOnMouseExited(event -> placeBidButton.setStyle("-fx-background-color: #42a1f5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26;"));

        // Buy Now Button
        Button buyNowButton = new Button("Buy Now");
        buyNowButton.setStyle("-fx-background-color: #3867c7; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26;");
        buyNowButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        buyNowButton.setOnAction(event -> {
            Stage newStage = new Stage();
            try {
                ConfirmController.setStage(newStage);
                ConfirmController.setItem(this);
                newStage.setScene(confirmScene(newStage));
                newStage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        buyNowButton.setOnMouseEntered(event -> buyNowButton.setStyle("-fx-background-color: #5a9cd1; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26; -fx-cursor: hand;"));
        buyNowButton.setOnMouseExited(event -> buyNowButton.setStyle("-fx-background-color: #3867c7; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26;"));

        // Bid History Button
        Button bidHistoryButton = new Button("Bid History");
        bidHistoryButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        bidHistoryButton.setStyle("-fx-background-color: #a8a8a8; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20;");
        bidHistoryButton.setOnAction(event -> {

        });
        bidHistoryButton.setOnMouseEntered(event -> bidHistoryButton.setStyle("-fx-background-color: #c7c7c7; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand;"));
        bidHistoryButton.setOnMouseExited(event -> bidHistoryButton.setStyle("-fx-background-color: #a8a8a8; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20;"));

        Label totalBidsLabel = new Label(String.format("%d Bids", totalBids));
        totalBidsLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        totalBidsLabel.setUnderline(true);

        // Combine all the containers together
        leftBox.setPrefWidth(400);
        rightBox.setPrefWidth(400);
        descriptionBox.setPrefWidth(600);
        if (!this.sold && this.timeRemaining > 0) {
            leftBox.getChildren().addAll(itemName, currentItemPriceLabel, buyNowLabel, timeRemainingLabel);
            rightBox.getChildren().addAll(placeBidButton, buyNowButton, bidHistoryButton, totalBidsLabel);
        } else {
            leftBox.getChildren().addAll(itemName, soldLabel, timeRemainingLabel);
            rightBox.getChildren().addAll(bidHistoryButton, totalBidsLabel);
        }
        hBox.getChildren().addAll(imageView, leftBox, descriptionBox, rightBox);
        hBox.setStyle("-fx-border-color: #f2f2f2; -fx-padding: 10");
        this.displayed = true;
        hBox.setId(this.name);
        return hBox;
    }

    public static String displayTime(int time) {
        int hours = time / 3600;
        int minutes = (time % 3600) / 60;
        int seconds = time % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public String toString() {
        return "name: " + name + " startingBid: " + startingBid + " buyNowPrice: " + buyNowPrice;
    }

    class BidInstance {
        String bidder;
        double bidPrice;
        int timeRemaining;
        public BidInstance(String bidder, double bidPrice, int timeRemaining) {
            this.bidder = bidder;
            this.bidPrice = bidPrice;
            this.timeRemaining = timeRemaining;
        }
    }
}
