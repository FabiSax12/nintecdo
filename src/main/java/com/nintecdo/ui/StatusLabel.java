package com.nintecdo.ui;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class StatusLabel extends Label {
    private Status status;

    public StatusLabel(String text) {
        super(text);
        this.setTextFill(Color.BLACK);
    }

    public enum Status {
        WAITING_GAME,
        LOADING,
        ERROR,
        FINISHED,
        SUCCESS,
    }

    public void setStatus(Status status) {
        this.status = status;

        if (status == Status.WAITING_GAME) {
            setText("Esperando Juego");
            this.setTextFill(Color.BLUE);
            return;
        }

        if (status == Status.ERROR) {
            setText("Error");
            this.setTextFill(Color.RED);
            return;
        }

        if (status == Status.FINISHED) {
            setText("Juego finalizado");
            this.setTextFill(Color.GREEN);
        }
    }

    public Status getStatus() {
        return status;
    }
}
