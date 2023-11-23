import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    String command;
    Item auctionItem;
    int number;

    protected Message(String command) {
        this.command = command;
        auctionItem = null;
        number = 0;
    }

    protected Message(String command, Item auctionItem) {
        this.command = command;
        this.auctionItem = auctionItem;
        this.number = 0;
    }

    protected Message(String command, Item auctionItem, int number) {
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

    public int getNumber() {
        return number;
    }
}
