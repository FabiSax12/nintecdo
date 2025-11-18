package com.nintecdo.manager;

import com.nintecdo.core.IGame;
import com.nintecdo.core.IGameListener;
import com.nintecdo.exception.GameLoadException;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    private static GameManager instance;
    private Map<String, IGame> games = new HashMap<>();
    private List<IGameListener> listeners = new ArrayList<>();
    private IGame currentGame;

    private GameManager() {}

    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void registerGame(String name, IGame game) {
        games.put(name, game);
    }

    public Pane startGame(String gameName) throws GameLoadException {
        IGame game = games.get(gameName);

        System.out.println("Starting game " + gameName + " Instance " + game);

        if (game == null) {
            throw new GameLoadException("Juego no encontrado: " + gameName);
        }

        currentGame = game;
        // Agregar listeners
        listeners.forEach(game::addGameListener);
        game.start();

        return game.getGamePanel();
    }

    public void addGameListener(IGameListener listener) {
        listeners.add(listener);
    }

    public List<String> getAvailableGames() {
        return new ArrayList<>(games.keySet());
    }
}
