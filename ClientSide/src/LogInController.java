import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LogInController extends Client {
    private static Stage primaryStage;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button loginGuestButton;
    private static String password;

    public static void setPrimaryStage(Stage s) {
        primaryStage = s;
    }

    @FXML
    public void loginButtonAction() {
        username = usernameTextField.getText();
        password = passwordTextField.getText();

        if (password.isEmpty()) {
            errorLabel.setText("Please enter a password");
            usernameTextField.clear();
        } else if (username.isEmpty()) {
            errorLabel.setText("Please enter a username");
            passwordTextField.clear();
        } else {
            try {
                primaryStage.setScene(auctionScene(primaryStage));
                readFromServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void loginButtonMouseHover() {
        loginButton.setStyle("-fx-background-color: #b5b5b5; -fx-border-color: BLACK; -fx-border-radius: 5; -fx-background-radius: 5");
    }

    @FXML
    public void guestButtonMouseHover() {
        loginGuestButton.setStyle("-fx-background-color: #b5b5b5; -fx-border-color: BLACK; -fx-border-radius: 5; -fx-background-radius: 5");
    }

    @FXML
    public void loginButtonMouseExit() {
        loginButton.setStyle("-fx-background-color: WHITE; -fx-border-color: BLACK; -fx-border-radius: 5");
    }

    @FXML
    public void guestButtonMouseExit() {
        loginGuestButton.setStyle("-fx-background-color: WHITE; -fx-border-color: BLACK; -fx-border-radius: 5");
    }

    @FXML
    public void guestButtonAction() {
        username = usernameTextField.getText();
        password = passwordTextField.getText();

        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username");
        } else {
            try {
                primaryStage.setScene(auctionScene(primaryStage));
                readFromServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
