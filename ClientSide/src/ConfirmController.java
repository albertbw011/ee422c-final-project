import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ConfirmController {
    private static Stage stage;
    @FXML
    private Button yesButton;
    @FXML
    private Button noButton;

    public static void setStage(Stage s) {
        stage = s;
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
