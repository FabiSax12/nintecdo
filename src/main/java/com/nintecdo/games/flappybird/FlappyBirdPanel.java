package com.nintecdo.games.flappybird;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class FlappyBirdPanel {
    private final Canvas canvas;
    private final Pane root;
    private final FlappyBird game;
    private AnimationTimer gameLoop;
    private boolean jumping = false;

    // GAME
    private float birdY = 100;
    private float birdVelocity = 0;
    private float gravity = 0.3f;
    private float jumpPower = 8;
    private int score = 0;

    private float pipeX = 400;
    private float pipeY = 150;
    private float pipeGap = 120;
    private float pipeWidth = 60;
    private float pipeSpeed = 4;

    public FlappyBirdPanel(FlappyBird game) {
        this.game = game;
        this.canvas = new Canvas(400, 600);
        this.root = new Pane(canvas);
        setupInput();
    }

    private void setupInput() {
        root.setOnKeyPressed(this::handleKeyPress);
        root.setFocusTraversable(true);
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode().getName().equals("Space")) {
            jumping = true;
        }
    }

    public void startGame() {
        System.out.println("Starting Flappy Bird Panel");
        root.requestFocus();
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        gameLoop.start();
    }

    public void stopGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    private void update() {
        // Aplicar gravedad
        birdVelocity += gravity;
        birdY += birdVelocity;

        // Si salta
        if (jumping) {
            birdVelocity = -jumpPower;
            jumping = false;
        }

        // Mover tuberías
        pipeX -= pipeSpeed;

        // Reiniciar tuberías si salen de pantalla
        if (pipeX < -pipeWidth) {
            pipeX = (float) canvas.getWidth();
            score++;
            game.updateScore(score);
        }

        // Colisiones
        if (birdY > canvas.getHeight() ||
                birdY < 0 ||
                checkCollision()) {
            gameOver();
        }
    }

    private boolean checkCollision() {
        float birdX = 50;
        float birdSize = 30;

        // Colisión con tuberías
        if (birdX + birdSize > pipeX && birdX < pipeX + pipeWidth) {
            if (birdY < pipeY || birdY + birdSize > pipeY + pipeGap) {
                return true;
            }
        }
        return false;
    }

    private void render() {
        var gc = canvas.getGraphicsContext2D();

        gc.setFill(javafx.scene.paint.Color.LIGHTBLUE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Pájaro amarillo
        gc.setFill(javafx.scene.paint.Color.YELLOW);
        gc.fillOval(50, birdY, 30, 30);

        // Tuberías verdes
        gc.setFill(javafx.scene.paint.Color.GREEN);
        gc.fillRect(pipeX, 0, pipeWidth, pipeY);
        gc.fillRect(pipeX, pipeY + pipeGap, pipeWidth, (float)canvas.getHeight());

        // Puntuación
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.setFont(new javafx.scene.text.Font(20));
        gc.fillText("Score: " + score, 10, 30);
    }

    public Pane getRoot() {
        return root;
    }

    private void gameOver() {
        stopGame();
        game.notifyGameFinished();
    }
}