package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class GameOverMessage implements Serializable {
    private final String result; // "X wins" / "O wins" / "Draw"

    public GameOverMessage(String result) {
        this.result = result;
    }

    public String getResult() { return result; }
}
