import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController extends Client implements Initializable {
    private Stage primaryStage;
    @FXML
    private ScrollPane itemListPane;
    @FXML
    private VBox itemListPaneVBox;
    @FXML
    private Button logOutButton;
    @FXML
    private Button exitButton;
    @FXML
    private Text title;
    @FXML
    private VBox notificationVBox;
    @FXML
    private ScrollPane notiScrollPane;
    @FXML
    private Button sellItemButton;

    public void setPrimaryStage(Stage s) {
        primaryStage = s;
    }

    public VBox getItemListPaneVBox() {
        return itemListPaneVBox;
    }

    public VBox getNotificationVBox() { return notificationVBox; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Auction FXML Initialization called");
        title.setText("Welcome to eHills, " + username + "!");
        itemListPane.setContent(itemListPaneVBox);
        notiScrollPane.setContent(notificationVBox);
    }

    @FXML
    public void logOutAction() {
        try {
            buttonSound.play();
            primaryStage.setScene(Client.loginScene(primaryStage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logOutMouseHover() {
        logOutButton.setStyle("-fx-background-color: #b5b5b5; -fx-border-color: BLACK; -fx-border-radius: 5");
    }

    @FXML
    public void logOutMouseExit() {
        logOutButton.setStyle("-fx-background-color: WHITE; -fx-border-color: BLACK; -fx-border-radius: 5");
    }

    @FXML
    public void quitAction() {
        buttonSound.play();
        primaryStage.close();
        sendToServer(new Message("close"));
        System.exit(0);
    }

    @FXML
    public void exitButtonMouseHover() {
        exitButton.setStyle("-fx-background-color: #b5b5b5; -fx-border-color: BLACK; -fx-border-radius: 5");
    }

    @FXML
    public void exitButtonMouseExit() {
        exitButton.setStyle("-fx-background-color: WHITE; -fx-border-color: BLACK; -fx-border-radius: 5");
    }

    @FXML
    public void sellItemAction() {
        try {
            Stage newStage = new Stage();
            buttonSound.play();
            AddItemController.setStage(newStage);
            newStage.setScene(Client.addItemScene(newStage));
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sellItemMouseHover() {
        sellItemButton.setStyle("-fx-background-color: #b5b5b5; -fx-border-color: BLACK; -fx-border-radius: 5; -fx-cursor: hand");
    }

    @FXML
    public void sellItemMouseExit() {
        sellItemButton.setStyle("-fx-background-color: WHITE; -fx-border-color: BLACK; -fx-border-radius: 5");
    }
}
