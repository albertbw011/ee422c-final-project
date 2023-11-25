import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class BidController extends Client implements Initializable {
    private static Stage stage;
    private static Item item;
    @FXML
    private Label currentPriceLabel;
    @FXML
    private Button bidButton1;
    @FXML
    private Button bidButton2;
    @FXML
    private Button bidButton3;
    @FXML
    private TextField amountField;
    @FXML
    private Button mainBidButton;
    @FXML
    private Label enterOrMoreLabel;
    @FXML
    private Label errorLabel;
    int bidAmount1;
    int bidAmount2;
    int bidAmount3;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentPriceLabel.setText(String.format("$ %.2f", item.currentBid));
        bidAmount1 = (int) (Math.min(Math.floor(item.currentBid+1), item.buyNowPrice));
        bidAmount2 = (int) (Math.min(Math.floor(item.currentBid * 1.02), item.buyNowPrice));
        bidAmount3 = (int) (Math.min(Math.floor(item.currentBid * 1.05), item.buyNowPrice));
        bidButton1.setText("Bid $" + bidAmount1);
        bidButton2.setText("Bid $" + bidAmount2);
        bidButton3.setText("Bid $" + bidAmount3);
        enterOrMoreLabel.setText(String.format("Enter $%.2f or more.", item.currentBid));
    }

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void setItem(Item i) {
        item = i;
    }

    @FXML
    public void bidButton1Action() {
        sendBid(bidAmount1, item);
    }

    @FXML
    public void bidButton1MouseHover() {
        bidButton1.setStyle("-fx-background-color: #609cf0; -fx-background-radius: 20; -fx-cursor: hand;");
    }

    @FXML
    public void bidButton1MouseExit() {
        bidButton1.setStyle("-fx-background-color:  #3388ff; -fx-background-radius: 20;");
    }

    @FXML
    public void bidButton2Action() {
        sendBid(bidAmount2, item);
    }

    @FXML
    public void bidButton2MouseHover() {
        bidButton2.setStyle("-fx-background-color: #609cf0; -fx-background-radius: 20; -fx-cursor: hand;");
    }

    @FXML
    public void bidButton2MouseExit() {
        bidButton2.setStyle("-fx-background-color:  #3388ff; -fx-background-radius: 20;");
    }

    @FXML
    public void bidButton3Action() {
        sendBid(bidAmount3, item);
    }

    @FXML
    public void bidButton3MouseHover() {
        bidButton3.setStyle("-fx-background-color: #609cf0; -fx-background-radius: 20; -fx-cursor: hand;");
    }

    @FXML
    public void bidButton3MouseExit() {
        bidButton3.setStyle("-fx-background-color:  #3388ff; -fx-background-radius: 20;");
    }

    @FXML
    public void mainBidButtonAction() {
        try {
            double getAmount = Double.parseDouble(amountField.getText());
            if (getAmount < item.currentBid) {
                errorLabel.setText("Bid must be greater than current price");
                amountField.clear();
            } else {
                // Client.sendBid() processes whether or not the bid prices is higher than buy now price
                // customer buys item and now it can no longer be purchased by other customers
                sendBid(getAmount, item);
                stage.close();
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Please input a valid price.");
        }
    }

    @FXML
    public void mainBidButtonMouseHover() {
        mainBidButton.setStyle("-fx-background-color: #609cf0; -fx-background-radius: 20; -fx-cursor: hand;");
    }

    @FXML
    public void mainBidButtonMouseExit() {
        mainBidButton.setStyle("-fx-background-color:  #3388ff; -fx-background-radius: 20;");
    }
}
