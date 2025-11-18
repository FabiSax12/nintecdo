package com.nintecdo.games.tiktaktoe;

public class TicTacToeController {
    private Board board;
    private TicTacToe game;

    public TicTacToeController(Board board, TicTacToe game) {
        this.board = board;
        this.game = game;
    }

    public void resetGame() {
        board.reset();
    }

    public Board getBoard() {
        return board;
    }
}