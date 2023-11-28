import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class BidHistoryController {
    @FXML
    private VBox bidHistoryVBox;
    private static Stage stage;
    private static Item thisItem;

    public void setItem(Item i) {
        thisItem = i;
        initializeWithItem();
    }

    public void setStage(Stage s) {
        stage = s;
    }

    public void initializeWithItem() {
        List<Item.BidInstance> bidHistoryList = thisItem.bidHistory;
        for (Item.BidInstance bidInstance : bidHistoryList) {
            bidHistoryVBox.getChildren().add(0, bidInstance.display());
        }
    }
}
