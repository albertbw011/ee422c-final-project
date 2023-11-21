import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.ArrayList;
import java.util.List;

public class Item {
    String name;
    String buyer;
    String description;
    double soldPrice;
    double startingBid;
    double buyNowPrice;
    double timeRemaining;
    double currentBid;
    int totalBids;
    List<BidInstance> bidHistory;
    boolean sold;
    Image image;


    public Item(String name, String description, String imagePath, double minBid, double buyNow, double time) {
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
        HBox hBox = new HBox();
        VBox leftBox = new VBox();
        VBox descriptionBox = new VBox();
        VBox rightBox = new VBox();
        ImageView imageView = new ImageView(image);

        Label itemName = new Label(name);
        itemName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        Label currentItemPriceLabel = new Label("Current Bid: $" + Double.toString(currentBid));
        currentItemPriceLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));

        Label buyNowLabel = new Label("Buy Now: $" + Double.toString(buyNowPrice));
        buyNowLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));

        Label timeRemainingLabel = new Label("Ends in " + displayTime(timeRemaining));
        timeRemainingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        timeRemainingLabel.setTextFill(Color.RED);

        leftBox.getChildren().addAll(itemName, imageView, currentItemPriceLabel, buyNowLabel, timeRemainingLabel);

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

        rightBox.getChildren().addAll(placeBidButton, buyNowButton, bidHistoryButton);

        hBox.getChildren().addAll(leftBox, descriptionBox, rightBox);
        return hBox;
    }

    public static String displayTime(double time) {
        return null;
    }

    class BidInstance {
        String bidder;
        double bidPrice;
        double timeRemaining;
        public BidInstance(String bidder, double bidPrice, double timeRemaining) {
            this.bidder = bidder;
            this.bidPrice = bidPrice;
            this.timeRemaining = timeRemaining;
        }
    }
}
