package com.nintecdo.games.tiktaktoe;

import javafx.scene.control.Button;

public class Board {
    public static final int PLAYER_X = 1;
    public static final int PLAYER_O = -1;

    private int[][] grid;
    private int currentPlayer;
    private Button[][] buttons;
    private MoveHistory moveHistory;

    public Board() {
        grid = new int[3][3];
        buttons = new Button[3][3];
        currentPlayer = PLAYER_X;
        moveHistory = new MoveHistory();
        reset();
    }

    public void reset() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grid[i][j] = 0;
            }
        }
        currentPlayer = PLAYER_X;
        moveHistory.clear();
    }

    public void makeMove(int row, int col) throws InvalidMoveException {
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            throw new InvalidMoveException("Posición fuera de rango");
        }
        if (grid[row][col] != 0) {
            throw new InvalidMoveException("Celda ya ocupada");
        }

        grid[row][col] = currentPlayer;
        moveHistory.addMove(new Move(row, col, currentPlayer));
    }

    public GameState checkGameState() {
        // Verificar filas
        for (int i = 0; i < 3; i++) {
            if (grid[i][0] != 0 && grid[i][0] == grid[i][1] &&
                    grid[i][1] == grid[i][2]) {
                return grid[i][0] == PLAYER_X ? GameState.X_WINS : GameState.O_WINS;
            }
        }

        // Verificar columnas
        for (int i = 0; i < 3; i++) {
            if (grid[0][i] != 0 && grid[0][i] == grid[1][i] &&
                    grid[1][i] == grid[2][i]) {
                return grid[0][i] == PLAYER_X ? GameState.X_WINS : GameState.O_WINS;
            }
        }

        // Verificar diagonales
        if (grid[0][0] != 0 && grid[0][0] == grid[1][1] &&
                grid[1][1] == grid[2][2]) {
            return grid[0][0] == PLAYER_X ? GameState.X_WINS : GameState.O_WINS;
        }
        if (grid[0][2] != 0 && grid[0][2] == grid[1][1] &&
                grid[1][1] == grid[2][0]) {
            return grid[0][2] == PLAYER_X ? GameState.X_WINS : GameState.O_WINS;
        }

        // Verificar empate
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[i][j] == 0) return GameState.IN_PROGRESS;
            }
        }
        return GameState.DRAW;
    }

    public boolean isEmpty(int row, int col) {
        return grid[row][col] == 0;
    }

    public void switchPlayer() {
        currentPlayer = (currentPlayer == PLAYER_X) ? PLAYER_O : PLAYER_X;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setButton(int row, int col, Button button) {
        buttons[row][col] = button;
    }

    public Button getButton(int row, int col) {
        return buttons[row][col];
    }

    public MoveHistory getMoveHistory() {
        return moveHistory;
    }
}
