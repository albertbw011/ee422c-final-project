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

    // constructor with current bid and bidHistory
    public Item(String name, String description, double minBid, double buyNow, int time, Double currentBid, List<BidInstance> bidHistory) {
        this.name = name;
        this.currentBidder = null;
        this.description = description;
        this.startingBid = minBid;
        this.buyNowPrice = buyNow;
        this.timeRemaining = time;
        this.currentBid = currentBid != null ? currentBid : startingBid;
        this.bidHistory = bidHistory != null ? bidHistory : new ArrayList<>();
        this.totalBids = bidHistory != null ? bidHistory.size() : 0;
        if (!this.bidHistory.isEmpty()) {
            BidInstance mostRecent = this.bidHistory.get(this.bidHistory.size()-1);
            if (mostRecent.purchased || timeRemaining < 0) {
                this.sold = true;
                this.currentBid = mostRecent.bidPrice;
                this.buyer = mostRecent.bidder;
                this.soldPrice = mostRecent.bidPrice;
                this.timeRemaining = 0;
            }
        }
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

        public static List<BidInstance> fromDocument(Document doc) {
            List<BidInstance> bidInstances = new ArrayList<>();
            List<Document> bidDocs = doc.getList("bidHistory", Document.class);
            if (bidDocs != null) {
                for (Document bidDoc : bidDocs) {
                    bidInstances.add(new BidInstance(bidDoc.getString("bidder"), bidDoc.getDouble("bidPrice"),
                            bidDoc.getInteger("timeRemaining"), bidDoc.getBoolean("purchased")));
                }
            }
            return bidInstances;
        }
        @Override
        public String toString() {
            return bidder + " " + bidPrice + " " + displayTime(timeRemaining);
        }
    }
}
