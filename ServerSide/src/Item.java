public class Item {
    private String name;
    private double minBid;
    private double buyNow;
    private double time;


    public Item(String name, double minBid, double buyNow, double time) {
        this.name = name;
        this.minBid = minBid;
        this.buyNow = buyNow;
        this.time = time;
    }
}
