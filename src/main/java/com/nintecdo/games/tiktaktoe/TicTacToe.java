package com.nintecdo.games.tiktaktoe;

import com.nintecdo.core.GameStats;
import com.nintecdo.core.IGame;
import com.nintecdo.core.IGameListener;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class TicTacToe implements IGame {
    private static TicTacToe instance;

    private final String NAME = "Tic Tac Toe";
    private final String VERSION = "1.0";

    private Board board;
    private TicTacToeController controller;
    private Pane gamePanel;
    private List<IGameListener> listeners;
    private Map<String, Object> stats;
    private LocalDateTime startTime;
    private boolean gameActive;

    private TicTacToe() {
        listeners = new ArrayList<>();
        stats = new HashMap<>();
        board = new Board();
        controller = new TicTacToeController(board, this);
        initializeUI();
    }

    public static synchronized TicTacToe getInstance() {
        if (instance == null) {
            instance = new TicTacToe();
        }
        return instance;
    }

    @Override
    public void start() {
        board.reset();
        startTime = LocalDateTime.now();
        gameActive = true;
        stats.put("score", 0);
        stats.put("moves", 0);
        stats.put("winner", null);
        stats.put("startTime", Instant.now());
        updateUI();
    }

    @Override
    public void stop() {
        gameActive = false;
        long elapsedTime = System.currentTimeMillis() -
                startTime.atZone(java.time.ZoneId.systemDefault())
                        .toInstant().toEpochMilli();
        stats.put("elapsedTime", elapsedTime);
        notifyGameEnded();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public Map<String, Object> getStats() {
        return Collections.unmodifiableMap(stats);
    }

    @Override
    public void addGameListener(IGameListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeGameListener(IGameListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Pane getGamePanel() {
        return gamePanel;
    }

    private void initializeUI() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button button = new Button();
                button.setPrefSize(80, 80);
                button.setStyle("-fx-font-size: 20;");

                final int r = row;
                final int c = col;
                button.setOnAction(e -> handleMove(r, c, button));

                grid.add(button, col, row);
                board.setButton(row, col, button);
            }
        }

        container.getChildren().add(grid);
        gamePanel = new Pane(container);
    }

    private void handleMove(int row, int col, Button button) {
        if (!gameActive || !board.isEmpty(row, col)) return;

        try {
            board.makeMove(row, col);
            button.setText(board.getCurrentPlayer() == Board.PLAYER_X ? "O" : "X");
            stats.put("moves", (Integer) stats.get("moves") + 1);

            GameState state = board.checkGameState();
            if (state == GameState.X_WINS) {
                stats.put("winner", "X");
                stop();
            } else if (state == GameState.O_WINS) {
                stats.put("winner", "O");
                stop();
            } else if (state == GameState.DRAW) {
                stats.put("winner", "DRAW");
                stop();
            }

            board.switchPlayer();
        } catch (InvalidMoveException e) {
            System.err.println("Movimiento inválido: " + e.getMessage());
        }
    }

    private void updateUI() {
        Platform.runLater(() -> {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    Button btn = board.getButton(row, col);
                    btn.setText("");
                }
            }
        });
    }

    private void notifyGameEnded() {
        System.out.println("Tic Tac Toe has ended");

        stats.put(
                "score",
                calculateScore(
                        (Integer) stats.get("moves"),
                        Duration.between((Instant) stats.get("startTime"), Instant.now()).toMillis()
                )
        );

        GameStats finalStats = new GameStats();
        finalStats.setGameName(getName());
        finalStats.setStats(getStats());
        finalStats.setTimestamp(LocalDateTime.now());

        Platform.runLater(() -> {
            for (IGameListener listener : listeners) {
                listener.onGameFinished(finalStats);
            }
        });
    }

    private Double calculateScore(Integer moves, Long timeMs) {
        // Convert time to seconds for readability
        double timeSeconds = timeMs / 1000.0;

        // Base score: 100 points
        // Penalty: 5 points per move, 1 point per second
        return Math.max(0, 100 - (moves * 5) - (timeSeconds * 1));
    }
}
