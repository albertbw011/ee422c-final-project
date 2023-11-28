import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class Notification {
    /**
     * Notification types
     *
     * item Ended
     * item Bid
     * item Purchased
     */

    public enum TYPE {
        END,
        BID,
        PURCHASE
    }

    private final String bidder;
    private double price;
    private final boolean purchase;
    private final int timeRemaining;
    private final Item item;
    private final TYPE type;

    // item bid notification constructor
    public Notification(String bidder, double price, int timeRemaining, Item item) {
        this.bidder = bidder;
        this.price = price;
        this.timeRemaining = timeRemaining;
        this.purchase = false;
        this.item = item;
        this.type = TYPE.BID;
    }

    // item purchased / item ended constructor (latter only if there is a winning bidder when the item ends)
    public Notification(String bidder, double price, Item item, TYPE type) {
        this.bidder = bidder;
        this.price = price;
        this.purchase = true;
        this.timeRemaining = -1;
        this.item = item;
        this.type = type;
    }

    // item ended constructor (if there are no bids)
    public Notification(Item item) {
        this.item = item;
        this.bidder = "None";
        this.purchase = false;
        this.timeRemaining = -1;
        this.type = TYPE.END;
    }

    public HBox display() {
        HBox hBox = new HBox(5);
        Label notiLabel = new Label();
        switch (type) {
            case BID:
                notiLabel.setText(bidder + " has bid on " + item.name + " for $" + price);
                break;
            case END:
                if (!purchase)
                    notiLabel.setText(item.name + " auction has ended with no winning bid");
                else
                    notiLabel.setText(bidder + " has won the auction for " + item.name + " at $" + price);
                break;
            case PURCHASE:
                notiLabel.setText(bidder + " has bought " + item.name + " for $" + price);
        }

        notiLabel.setPadding(new Insets(5, 5, 5, 5));
        hBox.getChildren().add(notiLabel);
        return hBox;
    }
}
