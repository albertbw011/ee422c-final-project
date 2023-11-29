import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddItemController extends Client {

    private static Stage stage;
    @FXML
    private TextField itemNameField;
    @FXML
    private TextField startingBidField;
    @FXML
    private TextField buyNowField;
    @FXML
    private TextField timeRemainingField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Button listButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label errorLabel;

    public static void setStage(Stage s) {
        stage = s;
    }

    @FXML
    public void addItemAction() {
        buttonSound.play();
        Item newItem;
        String name = itemNameField.getText();
        String description = descriptionField.getText();
        String startingBid = startingBidField.getText();
        String buyNowPrice = buyNowField.getText();
        String timeRemaining = timeRemainingField.getText();
        double startingBidDouble = !startingBid.isEmpty() ? Double.parseDouble(startingBid) : -1;
        double buyNowDouble = !buyNowPrice.isEmpty() ? Double.parseDouble(buyNowPrice) : -1;
        int timeRemainingInt = !timeRemaining.isEmpty() ? Integer.parseInt(timeRemaining)*60 : -1;

        if (name.isEmpty() || startingBid.isEmpty() || buyNowPrice.isEmpty() || timeRemaining.isEmpty())
            errorLabel.setText("Please fill all required fields");
        else if (startingBidDouble+1 >= buyNowDouble)
            errorLabel.setText("Starting bid must be at least $1 less than buy now price");
        else if (timeRemainingInt < 1)
            errorLabel.setText("Time must be at least one minute");
        else if (checkItemExists(name)) {
            errorLabel.setText("Item already exists");
        } else {
                newItem = new Item(name, description, startingBidDouble, buyNowDouble, timeRemainingInt);
                sendToServer(new Message("addItem", newItem));
                stage.close();
        }
    }

    private boolean checkItemExists(String name) {
        for (Item i : Client.getAuctionItemList()) {
            if (i.name.equals(name))
                return true;
        }
        return false;
    }

    @FXML
    public void addItemMouseHover() {
        listButton.setStyle("-fx-background-color: #71bef5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 40; -fx-cursor: hand;");
    }

    @FXML
    public void addItemMouseExit() {
        listButton.setStyle("-fx-background-color: #399ce3; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 40;");
    }

    @FXML
    public void cancelButtonAction() {
        buttonSound.play();
        stage.close();
    }

    @FXML
    public void cancelMouseHover() {
        cancelButton.setStyle("-fx-background-color: #708b9e; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 40; -fx-cursor: hand;");
    }

    @FXML
    public void cancelMouseExit() {
        cancelButton.setStyle("-fx-background-color: #4b5c69; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 40;");
    }
}
