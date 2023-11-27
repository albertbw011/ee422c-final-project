import javafx.scene.image.*;
import org.bson.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Item implements Serializable {
    private static final long serialVersionUID = 1234L;
    String name;
    String buyer;
    String currentBidder;
    String description;
    double soldPrice;
    double startingBid;
    double buyNowPrice;
    int timeRemaining;
    double currentBid;
    int totalBids;
    List<BidInstance> bidHistory;
    boolean sold;
    Image image;

    // constructor without image
    public Item(String name, String description, double minBid, double buyNow, int time) {
        this.name = name;
        this.currentBidder = null;
        this.description = description;
        this.startingBid = minBid;
        this.buyNowPrice = buyNow;
        this.timeRemaining = time;
        this.currentBid = (startingBid > 0) ? startingBid : 0;
        bidHistory = new ArrayList<>();
        totalBids = 0;
        sold = false;
    }

    // constructor with image
    public Item(String name, String description, String imagePath, double minBid, double buyNow, int time) {
        this.name = name;
        this.currentBidder = null;
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

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void decrementTimeRemaining() {
        timeRemaining--;
    }

    public static class BidInstance implements Serializable {
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

        public static BidInstance fromDocument(Document doc) {
            return new BidInstance(doc.getString("bidder"), doc.getInteger("bidPrice"),
                    doc.getInteger("timeRemaining"), doc.getBoolean("purchased"));
        }
        @Override
        public String toString() {
            return bidder + " " + bidPrice + " " + displayTime(timeRemaining);
        }
    }
}
