package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.entities.MoveMessage;
import il.cshaifasweng.OCSFMediatorExample.entities.BoardUpdate;
import il.cshaifasweng.OCSFMediatorExample.entities.GameOverMessage;

import java.io.IOException;
import java.util.ArrayList;

public class SimpleServer extends AbstractServer {

    private static final ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();

    // XO GAME STATE
    private char[][] board = new char[3][3];
    private char currentPlayer = 'X';

    public SimpleServer(int port) {
        super(port);
        resetGame();
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

        // 1) XO move message
        if (msg instanceof MoveMessage) {
            MoveMessage move = (MoveMessage) msg;
            handleMove(move);
            return;
        }

        // 2) old string commands (warning + subscribe)
        String msgString = msg.toString();

        if (msgString.startsWith("#warning")) {
            Warning warning = new Warning("Warning from server!");
            try {
                client.sendToClient(warning);
                System.out.format("Sent warning to client %s\n", client.getInetAddress().getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (msgString.startsWith("add client")) {
            SubscribedClient connection = new SubscribedClient(client);
            SubscribersList.add(connection);
            try {
                client.sendToClient("client added successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // When a client joins, send the current board to everyone
            sendToAllClients(new BoardUpdate(copyBoard(), currentPlayer));
        }
        else if (msgString.startsWith("remove client")) {
            SubscribersList.removeIf(sc -> sc.getClient().equals(client));
        }
    }

    // ======= XO LOGIC =======

    private void resetGame() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c] = ' ';
            }
        }
        currentPlayer = 'X';
    }

    private void handleMove(MoveMessage move) {
        int r = move.getRow();
        int c = move.getCol();

        // validate
        if (r < 0 || r > 2 || c < 0 || c > 2) return;
        if (board[r][c] != ' ') return; // already taken

        // place symbol
        board[r][c] = currentPlayer;

        // send board update to everyone
        sendToAllClients(new BoardUpdate(copyBoard(), currentPlayer));

        // win?
        if (isWin(currentPlayer)) {
            sendToAllClients(new GameOverMessage(currentPlayer + " wins!"));
            resetGame();
            sendToAllClients(new BoardUpdate(copyBoard(), currentPlayer)); // clear board
            return;
        }

        // draw?
        if (isDraw()) {
            sendToAllClients(new GameOverMessage("Draw!"));
            resetGame();
            sendToAllClients(new BoardUpdate(copyBoard(), currentPlayer)); // clear board
            return;
        }

        // switch turn
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    private boolean isDraw() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == ' ') return false;
            }
        }
        return true;
    }

    private boolean isWin(char p) {
        // rows
        for (int r = 0; r < 3; r++) {
            if (board[r][0] == p && board[r][1] == p && board[r][2] == p) return true;
        }
        // cols
        for (int c = 0; c < 3; c++) {
            if (board[0][c] == p && board[1][c] == p && board[2][c] == p) return true;
        }
        // diagonals
        if (board[0][0] == p && board[1][1] == p && board[2][2] == p) return true;
        if (board[0][2] == p && board[1][1] == p && board[2][0] == p) return true;

        return false;
    }

    private char[][] copyBoard() {
        char[][] copy = new char[3][3];
        for (int r = 0; r < 3; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, 3);
        }
        return copy;
    }

    // ======= SEND TO ALL =======
    public void sendToAllClients(Object message) {
        for (SubscribedClient subscribedClient : SubscribersList) {
            try {
                subscribedClient.getClient().sendToClient(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
