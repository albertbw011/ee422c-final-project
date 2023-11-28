import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 20L;
    String command;
    Customer customer;
    Item auctionItem;
    double number;

    protected Message(String command) {
        this.command = command;
        this.auctionItem = null;
        this.number = 0;
    }

    protected Message(String command, Customer customer) {
        this.command = command;
        this.customer = customer;
        this.auctionItem = null;
        this.number = 0;
    }

    protected Message(String command, Item auctionItem) {
        this.command = command;
        this.auctionItem = auctionItem;
        this.number = 0;
    }

    protected Message(String command, Item auctionItem, double number) {
        this.command = command;
        this.auctionItem = auctionItem;
        this.number = number;
    }

    public String getCommand() {
        return command;
    }

    public Item getAuctionItem() {
        return auctionItem;
    }

    public Customer getCustomer() { return customer; }

    public double getNumber() {
        return number;
    }
}
