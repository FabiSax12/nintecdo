package com.nintecdo.exception;

/**
 * Se lanza cuando hay error durante la ejecución de un juego.
 * Casos de uso:
 * - El juego se cuelga
 * - Error en I/O durante la partida
 * - Falta de recursos
 */
public class GameExecutionException extends Exception {
    public GameExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}