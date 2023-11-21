import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller extends Client {

    private static Stage primaryStage;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Label errorLabel;
    @FXML
    private ScrollPane itemListPane;
    @FXML
    private VBox itemListPaneVBox;
    private String username;
    private String password;

    public static void setPrimaryStage(Stage s) {
        primaryStage = s;
    }

    @FXML
    public void loginButtonAction(ActionEvent event) {
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
                setUpItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void guestButtonAction(ActionEvent event) {
        username = usernameTextField.getText();
        password = passwordTextField.getText();

        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username");
        } else {
            try {
                primaryStage.setScene(auctionScene(primaryStage));
                setUpItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void logOutAction(ActionEvent event) {
        try {
            primaryStage.setScene(loginScene(primaryStage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUpItems() {
        itemListPane.setContent(itemListPaneVBox);
        for (Item i : getAuctionItemList()) {
            itemListPaneVBox.getChildren().add(i.display());
        }
    }
}
