package com.nintecdo.loader;

import com.nintecdo.core.IGame;
import com.nintecdo.exception.GameLoadException;
import com.nintecdo.manager.GameManager;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class GameLoader {
    private static final String PLUGINS_DIR = "./plugins";
    private static final String GAME_CLASS_MANIFEST_KEY = "Game-Class";

    public static void loadAllGames() throws GameLoadException {
        File pluginsDir = new File(PLUGINS_DIR);
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }

        File[] jars = pluginsDir.listFiles((d, n) -> n.endsWith(".jar"));
        if (jars == null) return;

        for (File jar : jars) {
            try {
                IGame game = loadGameFromJar(jar);
                GameManager.getInstance().registerGame(game.getName(), game);
            } catch (Exception e) {
                System.err.println("Error cargando " + jar.getName() + ": " + e);
            }
        }
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
