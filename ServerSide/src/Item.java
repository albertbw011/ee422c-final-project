import javafx.scene.image.*;

import java.io.Serializable;
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
    int timeRemaining;
    double currentBid;
    int totalBids;
    List<BidInstance> bidHistory;
    boolean sold;
    Image image;

    // constructor without image
    public Item(String name, String description, double minBid, double buyNow, int time) {
        this.name = name;
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
