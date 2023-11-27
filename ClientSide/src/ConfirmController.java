import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ConfirmController extends Client {
    private static Stage stage;
    private static Item item;
    @FXML
    private Button yesButton;
    @FXML
    private Button noButton;

    public ConfirmController() {}

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void setItem(Item i) {
        item = i;
    }
    @FXML
    private void yesButtonMouseHover() {
        yesButton.setStyle("-fx-background-color: #9c9c9c; -fx-background-radius: 20; -fx-cursor: hand;");
    }

    @FXML
    private void yesButtonMouseExit() {
        yesButton.setStyle("-fx-background-color: #e1e3e1; -fx-background-radius: 20;");
    }

    @FXML
    private void yesButtonAction() {
        item.addBidInstance(item.buyer, item.buyNowPrice, item.timeRemaining, true);
        sendBid(item.buyNowPrice, item);
        stage.close();
    }

    @FXML
    private void noButtonMouseHover() {
        noButton.setStyle("-fx-background-color: #5a9cd1; -fx-background-radius: 20; -fx-cursor: hand;");
    }

    @FXML
    private void noButtonMouseExit() {
        noButton.setStyle("-fx-background-color: #3c6fde; -fx-background-radius: 20;");
    }

    @FXML
    private void noButtonMouseAction() {
        stage.close();
    }
}
