package com.nintecdo.core;

import javafx.scene.layout.Pane;

import java.util.Map;

public interface IGame {
    void start();
    void stop();
    String getName();
    String getVersion();
    Map<String, Object> getStats();
    void addGameListener(IGameListener listener);
    void removeGameListener(IGameListener listener);

    Pane getGamePanel();
}
