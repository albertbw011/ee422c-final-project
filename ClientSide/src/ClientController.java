import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class ClientController extends Client implements Initializable {
    private static Stage primaryStage;
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

    public static void setPrimaryStage(Stage s) {
        primaryStage = s;
    }

    public VBox getItemListPaneVBox() {
        return itemListPaneVBox;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Auction FXML Initialization called");
        title.setText("Welcome to eHills, " + username + "!");
        itemListPane.setContent(itemListPaneVBox);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.2), this::updateTimer));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimer(ActionEvent event) {
        for (Item i : Client.getAuctionItemList()) {
            if (!i.displayed)
                itemListPaneVBox.getChildren().add(i.display());
            if (i.sold)
                itemListPaneVBox.getChildren().remove(i.display());
        }
    }

    @FXML
    public void logOutAction() {
        try {
            primaryStage.setScene(Client.loginScene(primaryStage));
            for (Item i : getAuctionItemList())
                i.displayed = false;
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
        primaryStage.close();
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
}
