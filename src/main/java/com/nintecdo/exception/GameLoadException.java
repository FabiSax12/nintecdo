package com.nintecdo.exception;

public class GameLoadException extends Exception {
    public GameLoadException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public GameLoadException(String msg) {
        super(msg);
    }
}
