package com.nintecdo.persistence;

import com.nintecdo.core.GameStats;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio para persistencia de estadísticas de juegos en SQLite.
 * Implementa el patrón DAO (Data Access Object).
 */
public class StatsRepository {
    private static final String DB_URL = "jdbc:sqlite:stats.db";

    /**
     * Inicializa la base de datos creando las tablas si no existen.
     *
     * @throws SQLException si hay error en la base de datos
     */
    public void init() throws SQLException {
        try (
                Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()
        ) {

            stmt.execute("CREATE TABLE IF NOT EXISTS games (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL," +
                    "file_path TEXT NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS stats (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "game_id INTEGER NOT NULL, " +
                    "score REAL NOT NULL, " +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(game_id) REFERENCES games(id))");

            System.out.println("✓ Base de datos inicializada: " + DB_URL);
        }
    }

    public void addGame(String gameName, String filePath) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "INSERT INTO games (name, file_path) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, gameName);
                pstmt.setString(2, filePath);
                pstmt.executeUpdate();

                System.out.println("✓ Juego Guardado: " + gameName + " - Path: " + filePath);
            }

        }
    }

    /**
     * Obtiene todos los juegos con sus rutas de archivo.
     *
     * @return mapa: nombre del juego → ruta del archivo
     * @throws SQLException si hay error en la base de datos
     */
    public Map<String, String> getAllGamesWithPaths() throws SQLException {
        Map<String, String> games = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "SELECT name, file_path FROM games ORDER BY name";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    games.put(
                            rs.getString("name"),
                            rs.getString("file_path")
                    );
                }
            }
        }

        return games;
    }

    /**
     * Guarda las estadísticas de un juego en la base de datos.
     * Crea el juego si no existe.
     *
     * @param gameName nombre del juego
     * @param stats mapa con estadísticas (debe contener "score")
     * @throws SQLException si hay error en la base de datos
     */
    public void saveStats(String gameName, Map<String, Object> stats)
            throws SQLException {

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // 1. Obtener o crear el juego
            int gameId = getOrCreateGame(conn, gameName, "");

            // 2. Extraer el score
            Object scoreObj = stats.get("score");
            if (scoreObj == null) {
                throw new SQLException("El mapa de stats debe contener 'score'");
            }

            double score = 0;
            try {
                if (scoreObj instanceof Number) {
                    score = ((Number) scoreObj).doubleValue();
                } else {
                    score = Double.parseDouble(scoreObj.toString());
                }
            } catch (NumberFormatException e) {
                throw new SQLException("El score no es un número válido: " + scoreObj);
            }

            // 3. Insertar estadísticas
            String sql = "INSERT INTO stats (game_id, score) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, gameId);
                pstmt.setDouble(2, score);
                pstmt.executeUpdate();

                System.out.println("✓ Estadística guardada: " + gameName +
                        " - Score: " + score);
            }

        }
    }

    /**
     * Obtiene los 3 mejores resultados de un juego.
     *
     * @param gameName nombre del juego
     * @return lista de GameStats ordenada por score descendente (máximo 3)
     * @throws SQLException si hay error en la base de datos
     */
    public List<GameStats> getTop3(String gameName) throws SQLException {
        List<GameStats> top3 = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // SQL para obtener top 3
            String sql = "SELECT s.score, s.date " +
                    "FROM stats s " +
                    "INNER JOIN games g ON s.game_id = g.id " +
                    "WHERE g.name = ? " +
                    "ORDER BY s.score DESC " +
                    "LIMIT 3";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, gameName);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        double score = rs.getDouble("score");
                        String dateStr = rs.getString("date");

                        // Crear GameStats
                        Map<String, Object> statsMap = new HashMap<>();
                        statsMap.put("score", score);

                        GameStats gameStats = new GameStats(
                                gameName,
                                statsMap,
                                parseTimestamp(dateStr)
                        );

                        top3.add(gameStats);
                    }
                }
            }

        }

        return top3;
    }

    /**
     * Obtiene todos los top 3 de TODOS los juegos.
     * Útil para mostrar rankings globales.
     *
     * @return mapa: nombre del juego → lista de top 3
     * @throws SQLException si hay error en la base de datos
     */
    public Map<String, List<GameStats>> getTop3AllGames()
            throws SQLException {

        Map<String, List<GameStats>> allTop3 = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // Primero obtener todos los juegos
            String gamesSql = "SELECT DISTINCT name FROM games ORDER BY name";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(gamesSql)) {

                while (rs.next()) {
                    String gameName = rs.getString("name");
                    List<GameStats> top3 = getTop3(gameName);
                    allTop3.put(gameName, top3);
                }
            }
        }

        return allTop3;
    }

    /**
     * Obtiene todas las estadísticas de un juego (sin límite).
     *
     * @param gameName nombre del juego
     * @return lista de todas las estadísticas del juego
     * @throws SQLException si hay error en la base de datos
     */
    public List<GameStats> getAllStats(String gameName)
            throws SQLException {

        List<GameStats> allStats = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "SELECT s.score, s.date " +
                    "FROM stats s " +
                    "INNER JOIN games g ON s.game_id = g.id " +
                    "WHERE g.name = ? " +
                    "ORDER BY s.date DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, gameName);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        double score = rs.getDouble("score");
                        String dateStr = rs.getString("date");

                        Map<String, Object> statsMap = new HashMap<>();
                        statsMap.put("score", score);

                        GameStats gameStats = new GameStats(
                                gameName,
                                statsMap,
                                parseTimestamp(dateStr)
                        );

                        allStats.add(gameStats);
                    }
                }
            }
        }

        return allStats;
    }

    /**
     * Obtiene la lista de todos los juegos registrados.
     *
     * @return lista de nombres de juegos
     * @throws SQLException si hay error en la base de datos
     */
    public List<String> getAllGames() throws SQLException {
        List<String> games = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "SELECT DISTINCT name FROM games ORDER BY name";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    games.add(rs.getString("name"));
                }
            }
        }

        return games;
    }

    /**
     * Obtiene estadísticas de un juego en un rango de fechas.
     *
     * @param gameName nombre del juego
     * @param startDate fecha inicio (formato: YYYY-MM-DD)
     * @param endDate fecha fin (formato: YYYY-MM-DD)
     * @return lista de estadísticas en el rango
     * @throws SQLException si hay error en la base de datos
     */
    public List<GameStats> getStatsByDateRange(String gameName,
                                               String startDate,
                                               String endDate)
            throws SQLException {

        List<GameStats> stats = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "SELECT s.score, s.date " +
                    "FROM stats s " +
                    "INNER JOIN games g ON s.game_id = g.id " +
                    "WHERE g.name = ? " +
                    "AND DATE(s.date) BETWEEN ? AND ? " +
                    "ORDER BY s.date DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, gameName);
                pstmt.setString(2, startDate);
                pstmt.setString(3, endDate);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        double score = rs.getDouble("score");
                        String dateStr = rs.getString("date");

                        Map<String, Object> statsMap = new HashMap<>();
                        statsMap.put("score", score);

                        GameStats gameStats = new GameStats(
                                gameName,
                                statsMap,
                                parseTimestamp(dateStr)
                        );

                        stats.add(gameStats);
                    }
                }
            }
        }

        return stats;
    }

    /**
     * Obtiene el mejor score de un juego.
     *
     * @param gameName nombre del juego
     * @return el score más alto, o -1 si no hay registros
     * @throws SQLException si hay error en la base de datos
     */
    public double getHighScore(String gameName) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "SELECT MAX(s.score) as max_score " +
                    "FROM stats s " +
                    "INNER JOIN games g ON s.game_id = g.id " +
                    "WHERE g.name = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, gameName);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("max_score");
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Elimina todas las estadísticas de un juego.
     *
     * @param gameName nombre del juego
     * @return número de registros eliminados
     * @throws SQLException si hay error en la base de datos
     */
    public int deleteGameStats(String gameName) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String sql = "DELETE FROM stats " +
                    "WHERE game_id = (SELECT id FROM games WHERE name = ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, gameName);
                int deleted = pstmt.executeUpdate();

                System.out.println("✓ Eliminadas " + deleted +
                        " estadísticas de " + gameName);

                return deleted;
            }
        }
    }

    /**
     * Obtiene o crea un juego en la base de datos.
     *
     * @param conn conexión a la BD
     * @param gameName nombre del juego
     * @return ID del juego
     * @throws SQLException si hay error en la base de datos
     */
    private int getOrCreateGame(Connection conn, String gameName, String filePath)
            throws SQLException {

        // Intentar obtener el juego
        String selectSql = "SELECT id FROM games WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, gameName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Si no existe, crear
        String insertSql = "INSERT INTO games (name, file_path) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(
                insertSql,
                Statement.RETURN_GENERATED_KEYS)
        ) {

            pstmt.setString(1, gameName);
            pstmt.setString(2, filePath);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int gameId = generatedKeys.getInt(1);
                    System.out.println("✓ Juego creado: " + gameName +
                            " (ID: " + gameId + ")");
                    return gameId;
                }
            }
        }

        throw new SQLException("No se pudo crear el juego: " + gameName);
    }

    /**
     * Convierte una cadena de fecha a LocalDateTime.
     * Formato esperado: YYYY-MM-DD HH:MM:SS
     *
     * @param dateStr cadena de fecha
     * @return LocalDateTime
     */
    private LocalDateTime parseTimestamp(String dateStr) {
        try {
            return LocalDateTime.parse(
                    dateStr,
                    java.time.format.DateTimeFormatter
                            .ofPattern("yyyy-MM-dd HH:mm:ss")
            );
        } catch (Exception e) {
            System.err.println("Error parseando fecha: " + dateStr);
            return LocalDateTime.now();
        }
    }
}