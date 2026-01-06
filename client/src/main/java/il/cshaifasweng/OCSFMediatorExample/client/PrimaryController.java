package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.BoardUpdate;
import il.cshaifasweng.OCSFMediatorExample.entities.GameOverMessage;
import il.cshaifasweng.OCSFMediatorExample.entities.MoveMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class PrimaryController {

    private SimpleClient client;

    @FXML private Button btn00;
    @FXML private Button btn01;
    @FXML private Button btn02;
    @FXML private Button btn10;
    @FXML private Button btn11;
    @FXML private Button btn12;
    @FXML private Button btn20;
    @FXML private Button btn21;
    @FXML private Button btn22;

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
        client = SimpleClient.getClient();

        // Until the server sends the first board (after 2 clients connect)
        setBoardDisabled(true);
        setAllTextsEmpty();
    }

    @FXML
    private void handleCellClick(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String id = btn.getId(); // "btn00"

        int row = id.charAt(3) - '0';
        int col = id.charAt(4) - '0';

        try {
            client.sendToServer(new MoveMessage(row, col));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onBoardUpdate(BoardUpdate update) {
        Platform.runLater(() -> {
            char[][] b = update.getBoard();

            setText(btn00, b[0][0]); setText(btn01, b[0][1]); setText(btn02, b[0][2]);
            setText(btn10, b[1][0]); setText(btn11, b[1][1]); setText(btn12, b[1][2]);
            setText(btn20, b[2][0]); setText(btn21, b[2][1]); setText(btn22, b[2][2]);

            setBoardDisabled(false);
        });
    }

    @Subscribe
    public void onGameOver(GameOverMessage msg) {
        Platform.runLater(() -> {
            setBoardDisabled(true);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg.getResult());
            alert.setHeaderText("Game Over");
            alert.showAndWait();
        });
    }

    private void setText(Button btn, char c) {
        btn.setText(c == ' ' ? "" : String.valueOf(c));
    }

    private void setAllTextsEmpty() {
        btn00.setText(""); btn01.setText(""); btn02.setText("");
        btn10.setText(""); btn11.setText(""); btn12.setText("");
        btn20.setText(""); btn21.setText(""); btn22.setText("");
    }

    private void setBoardDisabled(boolean disabled) {
        btn00.setDisable(disabled); btn01.setDisable(disabled); btn02.setDisable(disabled);
        btn10.setDisable(disabled); btn11.setDisable(disabled); btn12.setDisable(disabled);
        btn20.setDisable(disabled); btn21.setDisable(disabled); btn22.setDisable(disabled);
    }
}
