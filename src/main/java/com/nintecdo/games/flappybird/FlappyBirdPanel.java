package com.games.flappybird;

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
        // Lógica del juego aquí
    }

    private void render() {
        // Renderización del juego aquí
    }

    public Pane getRoot() {
        return root;
    }
}