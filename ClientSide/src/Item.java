import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import java.io.Serializable;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;
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
    private Timeline timeline;


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
    }

    public HBox display() {
        HBox hBox = new HBox(20);
        VBox leftBox = new VBox(5);
        VBox descriptionBox = new VBox(5);
        VBox rightBox = new VBox(5);
        ImageView imageView = new ImageView(image);

        Label itemName = new Label(name);
        itemName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 25));

        Label currentItemPriceLabel = new Label("Current Bid: $" + currentBid);
        currentItemPriceLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));

        Label buyNowLabel = new Label("Buy Now: $" + buyNowPrice);
        buyNowLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));

        Label timeRemainingLabel = new Label(displayTime(timeRemaining));
        timeRemainingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        timeRemainingLabel.setTextFill(Color.RED);
        this.timeline = new Timeline(new KeyFrame(Duration.seconds(1.0), event -> updateTimer(timeRemainingLabel)));
        this.timeline.setCycleCount(Timeline.INDEFINITE);
        this.timeline.play();

        Label descriptionLabel = new Label(description);
        descriptionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        descriptionBox.getChildren().add(descriptionLabel);

        Button placeBidButton = new Button("Place Bid");
        placeBidButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        placeBidButton.setStyle("-fx-background-color: #42a1f5; -fx-text-fill: white; -fx-border-radius: 10; -fx-padding: 10 20;");
        placeBidButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            }
        });

        Button buyNowButton = new Button("Buy Now");
        buyNowButton.setStyle("-fx-background-color: #3867c7; -fx-text-fill: white; -fx-border-radius: 10; -fx-padding: 10 20;");
        buyNowButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        buyNowButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            }
        });

        Button bidHistoryButton = new Button("Bid History");
        bidHistoryButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        bidHistoryButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-radius: 10; -fx-padding: 10 20;");
        bidHistoryButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            }
        });

        leftBox.setPrefWidth(400);
        rightBox.setPrefWidth(400);
        descriptionBox.setPrefWidth(400);
        leftBox.getChildren().addAll(itemName, imageView, currentItemPriceLabel, buyNowLabel, timeRemainingLabel);
        rightBox.getChildren().addAll(placeBidButton, buyNowButton, bidHistoryButton);
        hBox.getChildren().addAll(leftBox, descriptionBox, rightBox);
        return hBox;
    }

    private void updateTimer(Label timerLabel) {
        if (timeRemaining > 0) {
            timerLabel.setText(displayTime(timeRemaining));
            timeRemaining--;
        } else {
            this.timeline.stop();
        }
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
