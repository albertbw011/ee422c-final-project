import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class BidController {
    private static Stage primaryStage;
    private static Item item;
    @FXML
    private Label placeYourBidLabel;
    @FXML
    private Label currentPriceLabel;
    @FXML
    private HBox bidButtonsHBox;
    @FXML
    private Button bidButton1;
    @FXML
    private Button bidButton2;
    @FXML
    private Button bidButton3;
    @FXML
    private HBox mainBidHBox;
    @FXML
    private TextField amountField;
    @FXML
    private Button mainBidButton;
    @FXML
    private Label enterOrMoreLabel;
    @FXML
    private Label errorLabel;

    public static void setPrimaryStage(Stage s) {
        primaryStage = s;
    }

    public static void setItem(Item i) {
        item = i;
    }

    @FXML
    public void bidButton1Action() {

    }

    @FXML
    public void bidButton2Action() {

    }

    @FXML
    public void bidButton3Action() {

    }

    @FXML
    public void mainBidButtonAction() {
        try {
            double getAmount = Double.parseDouble(amountField.getText());
            if (getAmount < item.currentBid) {
                errorLabel.setText("Bid must be greater than current price");
                amountField.clear();
            } else if (getAmount >= item.buyNowPrice) {
                // customer buys item and now it can no longer be purchased by other customers
            } else {
                // customer bids item and send to server
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Please input a valid price.");
        }
    }
}
