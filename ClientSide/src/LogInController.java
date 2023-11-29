import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LogInController extends Client {
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
    @FXML
    private Button registerButton;
    private static String password;

    public Label getErrorLabel() {
        return errorLabel;
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
            sendToServer(new Message("checkCustomer", new Customer(username, password)));
        }
    }

    @FXML
    public void loginButtonMouseHover() {
        loginButton.setStyle("-fx-background-color: #b5b5b5; -fx-border-color: BLACK; -fx-border-radius: 10; -fx-background-radius: 10");
    }

    @FXML
    public void guestButtonMouseHover() {
        loginGuestButton.setStyle("-fx-background-color: #b5b5b5; -fx-border-color: BLACK; -fx-border-radius: 10; -fx-background-radius: 10");
    }

    @FXML
    public void registerButtonMouseHover() {
        registerButton.setStyle("-fx-background-color: #b5b5b5; -fx-border-color: BLACK; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand");
    }

    @FXML
    public void loginButtonMouseExit() {
        loginButton.setStyle("-fx-background-color: WHITE; -fx-border-color: BLACK; -fx-border-radius: 10");
    }

    @FXML
    public void guestButtonMouseExit() {
        loginGuestButton.setStyle("-fx-background-color: WHITE; -fx-border-color: BLACK; -fx-border-radius: 10");
    }

    @FXML
    public void registerButtonMouseExit() {
        registerButton.setStyle("-fx-background-color: WHITE; -fx-border-color: BLACK; -fx-border-radius: 10");
    }

    @FXML
    public void guestButtonAction() {
        username = usernameTextField.getText();

        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username");
        } else {
            sendToServer(new Message("checkGuest", new Customer(username, "")));
        }
    }

    @FXML
    public void registerButtonAction() {
        username = usernameTextField.getText();
        password = passwordTextField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter a username and password to register");
        } else {
            sendToServer(new Message("addCustomer", new Customer(username, password)));
        }
    }
}
