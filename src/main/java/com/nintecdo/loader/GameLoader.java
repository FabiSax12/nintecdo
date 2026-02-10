package com.nintecdo.loader;

import com.nintecdo.core.IGame;
import com.nintecdo.exception.GameLoadException;
import com.nintecdo.manager.GameManager;
import com.nintecdo.persistence.StatsRepository;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.InputStream;
import java.util.Properties;

public class GameLoader {
    private static final String PLUGINS_DIR = "./plugins";
    private static final String GAME_CLASS_MANIFEST_KEY = "Game-Class";

    // En GameLoader.java
    public static void loadAllGames(StatsRepository statsRepository)
            throws GameLoadException {
        try {
            Map<String, String> savedGames = statsRepository.getAllGamesWithPaths();

            if (savedGames.isEmpty()) {
                System.out.println("No hay juegos guardados en la base de datos.");
                return;
            }

            System.out.println("Cargando " + savedGames.size() + " juegos guardados...");

            for (Map.Entry<String, String> entry : savedGames.entrySet()) {
                String gameName = entry.getKey();
                String filePath = entry.getValue();

                System.out.println("Cargando " + gameName + " desde " + filePath);

                File jarFile = new File(filePath);

                if (jarFile.exists()) {
                    try {
                        // Cargar sin volver a guardar en DB (ya existe)
                        loadGameFromPath(jarFile);
                        System.out.println("✓ " + gameName + " cargado");
                    } catch (Exception e) {
                        System.err.println("✗ Error cargando " + gameName +
                                ": " + e.getMessage());
                    }
                } else {
                    System.err.println("✗ Archivo no encontrado: " + filePath);
                }
            }

        } catch (SQLException e) {
            throw new GameLoadException(
                    "Error leyendo juegos de la base de datos: " + e.getMessage(),
                    e
            );
        }
    }

    // Método auxiliar sin guardar en DB
    private static void loadGameFromPath(File jarFile) throws Exception {
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                GameLoader.class.getClassLoader()
        );

        InputStream manifestStream = classLoader.getResourceAsStream(
                "manifest.properties"
        );

        if (manifestStream == null) {
            throw new Exception("No se encontró manifest.properties");
        }

        Properties props = new Properties();
        props.load(manifestStream);

        String gameClass = props.getProperty("game.class");
        String gameName = props.getProperty("game.title");

        Class<?> clazz = classLoader.loadClass(gameClass);
        IGame gameInstance = null;

        // INTENTAR CARGAR COMO SINGLETON
        try {
            java.lang.reflect.Method getInstanceMethod =
                    clazz.getMethod("getInstance");
            gameInstance = (IGame) getInstanceMethod.invoke(null);
            System.out.println("✓ Juego cargado como Singleton");
        } catch (NoSuchMethodException e) {
            // NO ES SINGLETON, instanciar normalmente
            gameInstance = (IGame) clazz.getDeclaredConstructor().newInstance();
            System.out.println("✓ Juego cargado con constructor público");
        }

        GameManager.getInstance().registerGame(gameName, gameInstance);
    }

    private static IGame loadGameFromJar(File jar)
            throws GameLoadException {

        String gameClassName = null;

        try {
            // 1. Leer el Manifest del JAR
            gameClassName = readGameClassFromManifest(jar);

            if (gameClassName == null || gameClassName.isEmpty()) {
                throw new GameLoadException(
                        "No se encontró entrada '" + GAME_CLASS_MANIFEST_KEY +
                                "' en el Manifest de " + jar.getName() +
                                ". Asegúrate de configurar tu pom.xml correctamente."
                );
            }

            // 2. Crear URLClassLoader para cargar el JAR
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{jar.toURI().toURL()},
                    Thread.currentThread().getContextClassLoader()
            );

            // 3. Cargar la clase
            Class<?> gameClass = classLoader.loadClass(gameClassName);

            // 4. Validar que implemente IGame
            if (!IGame.class.isAssignableFrom(gameClass)) {
                throw new GameLoadException(
                        gameClassName + " no implementa la interfaz IGame"
                );
            }

            // 5. Instanciar la clase
//            IGame game = (IGame) gameClass
//                    .getDeclaredConstructor()
//                    .newInstance();
//            IGame game = (IGame) (Simpleton) gameClass.getInstance();
            java.lang.reflect.Method getInstance = gameClass.getDeclaredMethod("getInstance");

            return (IGame) getInstance.invoke(null);

        } catch (ClassNotFoundException e) {
            throw new GameLoadException(
                    "Clase no encontrada: " + gameClassName, e
            );
        } catch (InstantiationException e) {
            throw new GameLoadException(
                    "No se puede instanciar: " + gameClassName +
                            ". ¿Tiene constructor sin argumentos?", e
            );
        } catch (IllegalAccessException e) {
            throw new GameLoadException(
                    "Acceso denegado a: " + gameClassName, e
            );
        } catch (Exception e) {
            throw new GameLoadException(
                    "Error cargando " + jar.getName() + ": " +
                            e.getClass().getSimpleName() + " - " + e.getMessage(), e
            );
        }
    }

    public static void loadGame(File jarFile, StatsRepository statsRepository) throws GameLoadException {
        if (!jarFile.exists() || !jarFile.getName().endsWith(".jar")) {
            throw new GameLoadException(
                    "El archivo no es un JAR válido: " + jarFile.getName()
            );
        }

        try {
            // Cargar el JAR dinámicamente
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{jarFile.toURI().toURL()},
                    GameLoader.class.getClassLoader()
            );

            // Leer manifest.properties del JAR
            InputStream manifestStream = classLoader.getResourceAsStream(
                    "manifest.properties"
            );

            if (manifestStream == null) {
                throw new GameLoadException(
                        "No se encontró manifest.properties en " + jarFile.getName()
                );
            }

            Properties props = new Properties();
            props.load(manifestStream);

            String gameClass = props.getProperty("game.class");
            String gameName = props.getProperty("game.title");

            if (gameClass == null || gameName == null) {
                throw new GameLoadException(
                        "Manifest incompleto en " + jarFile.getName()
                );
            }

            Class<?> clazz = classLoader.loadClass(gameClass);
            IGame gameInstance = null;

            // INTENTAR CARGAR COMO SINGLETON
            try {
                java.lang.reflect.Method getInstanceMethod =
                        clazz.getMethod("getInstance");
                gameInstance = (IGame) getInstanceMethod.invoke(null);
                System.out.println("✓ Juego cargado como Singleton");
            } catch (NoSuchMethodException e) {
                // NO ES SINGLETON, instanciar normalmente
                gameInstance = (IGame) clazz.getDeclaredConstructor().newInstance();
                System.out.println("✓ Juego cargado con constructor público");
            }

            GameManager.getInstance().registerGame(gameName, gameInstance);

            // GUARDAR EN BASE DE DATOS
            try {
                statsRepository.addGame(gameName, jarFile.getAbsolutePath());
            } catch (SQLException e) {
                // Si ya existe, ignorar el error (UNIQUE constraint)
                if (!e.getMessage().contains("UNIQUE constraint")) {
                    throw e;
                }
            }

            System.out.println("✓ Juego cargado: " + gameName);

        } catch (Exception e) {
            throw new GameLoadException(
                    "Error cargando " + jarFile.getName() + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Lee la clase principal del Manifest del JAR.
     * Busca la entrada especificada en GAME_CLASS_MANIFEST_KEY.
     *
     * @param jar archivo JAR
     * @return nombre completo de la clase o null si no existe
     * @throws Exception si hay error leyendo el JAR
     */
    private static String readGameClassFromManifest(File jar)
            throws Exception {

        try (JarFile jarFile = new JarFile(jar)) {
            Manifest manifest = jarFile.getManifest();

            if (manifest == null) {
                return null;
            }

            return manifest.getMainAttributes().getValue(GAME_CLASS_MANIFEST_KEY);
        }
    }
}
