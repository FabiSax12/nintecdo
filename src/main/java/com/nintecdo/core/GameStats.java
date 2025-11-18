package com.nintecdo.core;

import java.time.LocalDateTime;
import java.util.Map;

public class GameStats {
    private String gameName;
    private Map<String, Object> stats;
    private LocalDateTime timestamp;

    public GameStats(String gameName, Map<String, Object> stats,  LocalDateTime timestamp) {
        this.gameName = gameName;
        this.stats = stats;
        this.timestamp = timestamp;
    }

    public GameStats(String gameName, Map<String, Object> stats) {
        this.gameName = gameName;
        this.stats = stats;
        this.timestamp = LocalDateTime.now();
    }

    public GameStats() {}

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
