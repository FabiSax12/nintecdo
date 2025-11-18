package com.nintecdo.ui;

import com.nintecdo.core.IGameListener;
import com.nintecdo.core.GameStats;
import com.nintecdo.exception.GameLoadException;
import com.nintecdo.loader.GameLoader;
import com.nintecdo.manager.GameManager;
import com.nintecdo.persistence.StatsRepository;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

/**
 * Ventana principal de la plataforma Consola NinTECdo.
 * Implementa la Vista del patrón MVC.
 */
public class MainWindow extends Application implements IGameListener {

    private Pane gameContainer;
    private StatusLabel statsLabel;
    private ComboBox<String> gameSelector;
    private Button playBtn;
    private Button rankingsBtn;
    private StatsRepository statsRepository;

    @Override
    public void start(Stage stage) throws Exception {
        // Inicializar persistencia
        statsRepository = new StatsRepository();
        statsRepository.init();

        // Agregar listener para actualizaciones
        GameManager.getInstance().addGameListener(this);

        try {
            GameLoader.loadAllGames();
        } catch (GameLoadException e) {
            System.err.println("Error cargando juegos: " + e.getMessage());
            e.printStackTrace();
        }

        VBox root = new VBox();
        root.setPadding(new Insets(25));
        root.setSpacing(15);

        // ===== PANEL SUPERIOR: Selector y controles =====
        HBox gameSelectorPane = createControlPanel();

        // ===== PANEL CENTRAL: Contenedor del juego =====
        gameContainer = new Pane();
        gameContainer.setPrefHeight(600);
        gameContainer.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");
        VBox.setVgrow(gameContainer, javafx.scene.layout.Priority.ALWAYS);

        // ===== PANEL INFERIOR: Estado =====
        statsLabel = new StatusLabel("Esperando juego...");
        statsLabel.setStatus(StatusLabel.Status.WAITING_GAME);

        root.getChildren().addAll(gameSelectorPane, gameContainer, statsLabel);

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Consola NinTECdo");
        stage.show();
    }

    /**
     * Crea el panel superior con selector de juegos y botones.
     */
    private HBox createControlPanel() {
        HBox controlPanel = new HBox(15);
        controlPanel.setAlignment(Pos.CENTER_LEFT);
        controlPanel.setPadding(new Insets(15));
        controlPanel.setStyle(
                "-fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-color: #f9f9f9;"
        );

        // Label
        Label selectLabel = new Label("Juego:");
        selectLabel.setFont(new Font(14));
        selectLabel.setStyle("-fx-font-weight: bold;");

        // ComboBox
        gameSelector = new ComboBox<>();
        gameSelector.setItems(FXCollections.observableArrayList(
                GameManager.getInstance().getAvailableGames()
        ));
        gameSelector.setPrefWidth(200);
        if (!GameManager.getInstance().getAvailableGames().isEmpty()) {
            gameSelector.setValue(
                    GameManager.getInstance().getAvailableGames().get(0)
            );
        }

        // Botón Jugar
        playBtn = new Button("▶ Jugar");
        playBtn.setPrefWidth(100);
        playBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        playBtn.setOnAction(e -> startGame());

        // Botón Rankings
        rankingsBtn = new Button("🏆 Rankings");
        rankingsBtn.setPrefWidth(100);
        rankingsBtn.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        rankingsBtn.setOnAction(e -> showRankings());

        // Espaciador
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        controlPanel.getChildren().addAll(
                selectLabel,
                gameSelector,
                playBtn,
                spacer,
                rankingsBtn
        );

        return controlPanel;
    }

    /**
     * Inicia el juego seleccionado.
     */
    private void startGame() {
        String selectedGame = gameSelector.getValue();

        if (selectedGame == null || selectedGame.isEmpty()) {
            updateStatus("Selecciona un juego primero", StatusLabel.Status.ERROR);
            return;
        }

        try {
            updateStatus("Iniciando " + selectedGame + "...", StatusLabel.Status.LOADING);
            playBtn.setDisable(true);
            rankingsBtn.setDisable(true);
            gameSelector.setDisable(true);

            Pane startedGamePane = GameManager.getInstance()
                    .startGame(selectedGame);

            gameContainer.getChildren().clear();
            gameContainer.getChildren().add(startedGamePane);
            startedGamePane.requestFocus();

        } catch (Exception ex) {
            updateStatus("Error: " + ex.getMessage(), StatusLabel.Status.ERROR);
            playBtn.setDisable(false);
            rankingsBtn.setDisable(false);
            gameSelector.setDisable(false);
        }
    }

    /**
     * Muestra los rankings de todos los juegos o del seleccionado.
     */
    private void showRankings() {
        try {
            // Crear ventana de rankings
            Stage rankingsStage = new Stage();
            rankingsStage.setTitle("Rankings - Consola NinTECdo");
            rankingsStage.setWidth(600);
            rankingsStage.setHeight(700);

            VBox rankingsRoot = new VBox(15);
            rankingsRoot.setPadding(new Insets(20));
            rankingsRoot.setStyle("-fx-background-color: #f5f5f5;");

            // Título
            Label titleLabel = new Label("🏆 TOP 3 POR JUEGO");
            titleLabel.setFont(new Font(18));
            titleLabel.setStyle("-fx-font-weight: bold;");

            // ScrollPane para los rankings
            VBox rankingsContainer = new VBox(15);
            rankingsContainer.setPadding(new Insets(10));

            // Obtener todos los juegos disponibles
            List<String> availableGames =
                    GameManager.getInstance().getAvailableGames();

            if (availableGames.isEmpty()) {
                Label noGamesLabel = new Label(
                        "No hay juegos disponibles"
                );
                noGamesLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #999;");
                rankingsContainer.getChildren().add(noGamesLabel);
            } else {
                // Para cada juego, mostrar top 3
                for (String gameName : availableGames) {
                    rankingsContainer.getChildren().add(
                            createGameRankingPanel(gameName)
                    );
                }
            }

            ScrollPane scrollPane = new ScrollPane(rankingsContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-control-inner-background: #f5f5f5;");

            // Botón cerrar
            Button closeBtn = new Button("Cerrar");
            closeBtn.setPrefWidth(100);
            closeBtn.setOnAction(e -> rankingsStage.close());

            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().add(closeBtn);

            rankingsRoot.getChildren().addAll(
                    titleLabel,
                    new Separator(),
                    scrollPane,
                    buttonBox
            );

            Scene rankingsScene = new Scene(rankingsRoot);
            rankingsStage.setScene(rankingsScene);
            rankingsStage.show();

        } catch (Exception e) {
            updateStatus(
                    "Error cargando rankings: " + e.getMessage(),
                    StatusLabel.Status.ERROR
            );
            e.printStackTrace();
        }
    }

    /**
     * Crea un panel con el top 3 de un juego específico.
     */
    private VBox createGameRankingPanel(String gameName) {
        VBox gamePanel = new VBox(8);
        gamePanel.setPadding(new Insets(12));
        gamePanel.setStyle(
                "-fx-border-color: #ddd; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-color: white;"
        );

        // Título del juego
        Label gameTitle = new Label(gameName);
        gameTitle.setFont(new Font(14));
        gameTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        try {
            List<GameStats> top3 = statsRepository.getTop3(gameName);

            if (top3.isEmpty()) {
                Label noStatsLabel = new Label("Sin registros aún");
                noStatsLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");
                gamePanel.getChildren().addAll(gameTitle, noStatsLabel);
            } else {
                gamePanel.getChildren().add(gameTitle);

                // Mostrar top 3
                for (int i = 0; i < top3.size(); i++) {
                    GameStats stats = top3.get(i);
                    HBox rankRow = createRankRow(i + 1, stats);
                    gamePanel.getChildren().add(rankRow);
                }
            }

        } catch (SQLException e) {
            Label errorLabel = new Label(
                    "Error: " + e.getMessage()
            );
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11;");
            gamePanel.getChildren().addAll(gameTitle, errorLabel);
        }

        return gamePanel;
    }

    /**
     * Crea una fila de ranking (posición, puntuación, fecha).
     */
    private HBox createRankRow(int position, GameStats stats) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(8));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f9f9f9; -fx-border-radius: 3;");

        // Posición (medalla)
        String medal = switch(position) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> String.valueOf(position);
        };

        Label positionLabel = new Label(medal);
        positionLabel.setFont(new Font(16));
        positionLabel.setPrefWidth(40);

        // Información
        VBox infoBox = new VBox(3);

        String score = stats.getStats()
                .getOrDefault("score", "N/A")
                .toString();

        Label scoreLabel = new Label("Puntuación: " + score);
        scoreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

        Label dateLabel = new Label(
                "Fecha: " + stats.getTimestamp()
        );
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");

        infoBox.getChildren().addAll(scoreLabel, dateLabel);

        row.getChildren().addAll(positionLabel, infoBox);

        return row;
    }

    /**
     * Actualiza el label de estado.
     */
    private void updateStatus(String message, StatusLabel.Status status) {
        Platform.runLater(() -> {
            statsLabel.setStatus(status);
            statsLabel.setText(message);
            statsLabel.setVisible(true);
        });
    }

    /**
     * Listener para cuando un juego termina (Observer).
     */
    @Override
    public void onGameFinished(GameStats stats) {

        System.out.println("Nuevas Stats");
        System.out.println(stats.toString());

        Platform.runLater(() -> {
            try {
                // Guardar estadísticas
                statsRepository.saveStats(
                        stats.getGameName(),
                        stats.getStats()
                );

                // Actualizar UI
                String message = String.format(
                        "✓ %s finalizado. Puntuación: %s",
                        stats.getGameName(),
                        stats.getStats().get("score")
                );
                updateStatus(message, StatusLabel.Status.SUCCESS);

                // Resetear controles
                playBtn.setDisable(false);
                rankingsBtn.setDisable(false);
                gameSelector.setDisable(false);

            } catch (SQLException e) {
                updateStatus(
                        "Error guardando estadísticas: " + e.getMessage(),
                        StatusLabel.Status.ERROR
                );
            }
        });
    }
}