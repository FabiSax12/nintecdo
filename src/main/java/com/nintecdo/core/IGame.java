package com.nintecdo.api;

public interface IGame {
    void start();
    void stop();
    String getName();
    String getVersion();
    Map<String, Object> getStats();
    void addGameListener(GameListener listener);
    void removeGameListener(GameListener listener);
}
