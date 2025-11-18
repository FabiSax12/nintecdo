package com.games.flappybird;

import com.nintecdo.core.IGame;
import com.nintecdo.core.IGameListener;
import com.nintecdo.core.GameStats;
import javafx.scene.layout.Pane;
import java.util.*;

public class FlappyBird implements IGame {
    private static FlappyBird instance;
    private List<IGameListener> listeners = new ArrayList<>();
    private FlappyBirdPanel gamePanel;
    private boolean running = false;
    private int score = 0;
    private long startTime;

    private FlappyBird() {
        this.gamePanel = new FlappyBirdPanel(this);
    }

    public static synchronized FlappyBird getInstance() {
        if (instance == null) {
            instance = new FlappyBird();
        }
        return instance;
    }

    @Override
    public void start() {
        running = true;
        startTime = System.currentTimeMillis();
        gamePanel.startGame();
    }

    @Override
    public void stop() {
        running = false;
        gamePanel.stopGame();
    }

    @Override
    public String getName() {
        return "Flappy Bird";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("score", score);
        stats.put("time", System.currentTimeMillis() - startTime);
        return stats;
    }

    @Override
    public void addGameListener(IGameListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeGameListener(IGameListener listener) {
        listeners.remove(listener);
    }

    public void notifyGameFinished() {
        GameStats stats = new GameStats();
        stats.setGameName(getName());
        stats.setStats(getStats());
        stats.setTimestamp(java.time.LocalDateTime.now());

        listeners.forEach(listener -> listener.onGameFinished(stats));
    }

    public void updateScore(int newScore) {
        this.score = newScore;
    }

    public Pane getGamePanel() {
        return gamePanel.getRoot();
    }
}