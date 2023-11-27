import javafx.animation.Animation;
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

public class Item implements Serializable {
    private static final long serialVersionUID = 1234L;
    int itemID;
    String name;
    String currentBidder;
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
    boolean hasItemEnded;


    public Item(String name, String description, String imagePath, double minBid, double buyNow, int time) {
        this.name = name;
        this.currentBidder = null;
        this.description = description;
        this.startingBid = minBid;
        this.buyNowPrice = buyNow;
        this.timeRemaining = time;
        this.currentBid = (startingBid > 0) ? startingBid : 0;
        this.bidHistory = new ArrayList<>();
        this.totalBids = 0;
        this.sold = false;
        this.image = new Image(imagePath);
        this.displayed = false;
        this.hasItemEnded = false;
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
        Label soldLabel = new Label();
        if (buyer != null) {
            soldLabel.setText((String.format("Purchased by %s at $%.2f", buyer, soldPrice)));
            soldLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            soldLabel.setTextFill(Color.RED);
        }

        // Current Bid
        Label currentItemPriceLabel = new Label(String.format("Current Price: $%.2f", currentBid));
        currentItemPriceLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        currentItemPriceLabel.setId("currentItemPriceLabel");

        // Buy Now Price
        Label buyNowLabel = new Label(String.format("Buy Now: $%.2f", buyNowPrice));
        buyNowLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));

        // Time Remaining in hh:mm:ss
        Label timeRemainingLabel = (timeRemaining > 0) ? new Label(displayTime(timeRemaining) + " remaining")
                : new Label("Ended");
        timeRemainingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        timeRemainingLabel.setTextFill((timeRemaining < 600) ? Color.RED : Color.BLACK);
        timeRemainingLabel.setId("timeRemainingLabel");
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1.0), event -> {
            if (timeRemaining > 0) {
                timeRemainingLabel.setText(displayTime(timeRemaining) + " remaining");
                //this.decrementTimeRemaining();
            } else {
                timeline.stop();
                if (!timeRemainingLabel.getText().equals("Ended")) {
                    Message send = new Message("itemEnded", this);
                    try {
                        Client.sendToServer(send);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
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
            Client.buttonSound.play();
            Client.openBidStage(this);
        });
        placeBidButton.setOnMouseEntered(event -> placeBidButton.setStyle("-fx-background-color: #90c8f5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26; -fx-cursor: hand;"));
        placeBidButton.setOnMouseExited(event -> placeBidButton.setStyle("-fx-background-color: #42a1f5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26;"));
        placeBidButton.setId("placeBidButton");

        // Buy Now Button
        Button buyNowButton = new Button("Buy Now");
        buyNowButton.setStyle("-fx-background-color: #3867c7; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26;");
        buyNowButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        buyNowButton.setOnAction(event -> {
            Stage newStage = new Stage();
            try {
                Client.buttonSound.play();
                ConfirmController.setStage(newStage);
                ConfirmController.setItem(this);
                newStage.setScene(Client.confirmScene(newStage));
                newStage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        buyNowButton.setOnMouseEntered(event -> buyNowButton.setStyle("-fx-background-color: #5a9cd1; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26; -fx-cursor: hand;"));
        buyNowButton.setOnMouseExited(event -> buyNowButton.setStyle("-fx-background-color: #3867c7; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 26;"));
        buyNowButton.setId("buyNowButton");

        // Bid History Button
        Button bidHistoryButton = new Button("Bid History");
        bidHistoryButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        bidHistoryButton.setStyle("-fx-background-color: #a8a8a8; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20;");
        bidHistoryButton.setOnAction(event -> {
            Client.buttonSound.play();
        });
        bidHistoryButton.setOnMouseEntered(event -> bidHistoryButton.setStyle("-fx-background-color: #c7c7c7; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand;"));
        bidHistoryButton.setOnMouseExited(event -> bidHistoryButton.setStyle("-fx-background-color: #a8a8a8; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20;"));

        Label totalBidsLabel = new Label(String.format("%d Bid%s", totalBids, totalBids == 1 ? "" : "s"));
        totalBidsLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        totalBidsLabel.setUnderline(true);
        totalBidsLabel.setId("totalBidsLabel");

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
        int days = time / (24*3600);
        int hours = time / 3600;
        int minutes = (time % 3600) / 60;
        int seconds = time % 60;

        StringBuilder timeDisplay = new StringBuilder();
        if (days > 0) timeDisplay.append(String.format("%dd ", days));
        if (hours > 0) timeDisplay.append(hours < 10 ? String.format("%dh ", hours) : String.format("%02dh ", hours));
        if (minutes > 0) timeDisplay.append(minutes < 10 ? String.format("%dm ", minutes) : String.format("%02dm ", minutes));
        if (seconds > 0) timeDisplay.append(seconds < 10 ? String.format("%ds", seconds) : String.format("%02ds", seconds));
        return timeDisplay.toString();
    }

    private void decrementTimeRemaining() {
        timeRemaining--;
    }

    public void addBidInstance(String bidder, double bidPrice, int timeRemaining, boolean purchased) {
        synchronized (this) {
            bidHistory.add(new BidInstance(bidder, bidPrice, timeRemaining, purchased));
        }
    }

    @Override
    public String toString() {
        return "name: " + name + " startingBid: " + startingBid + " buyNowPrice: " + buyNowPrice;
    }

    static class BidInstance implements Serializable {
        private static final long serialVersionUID = 30L;
        String bidder;
        double bidPrice;
        int timeRemaining;
        boolean purchased;
        public BidInstance(String bidder, double bidPrice, int timeRemaining, boolean purchased) {
            this.bidder = bidder;
            this.bidPrice = bidPrice;
            this.timeRemaining = timeRemaining;
            this.purchased = purchased;
        }

        public HBox display() {
            HBox Hbox = new HBox();
            return Hbox;
        }

        @Override
        public String toString() {
            return bidder + " " + bidPrice + " " + displayTime(timeRemaining);
        }
    }
}
