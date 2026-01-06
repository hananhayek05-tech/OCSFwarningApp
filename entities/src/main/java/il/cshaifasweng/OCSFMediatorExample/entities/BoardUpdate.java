package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class BoardUpdate implements Serializable {
    private final char[][] board;
    private final char currentPlayer; // whose turn next

    public BoardUpdate(char[][] board, char currentPlayer) {
        this.board = board;
        this.currentPlayer = currentPlayer;
    }

    public char[][] getBoard() { return board; }
    public char getCurrentPlayer() { return currentPlayer; }
}
