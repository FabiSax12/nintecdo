package com.nintecdo.games.tiktaktoe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MoveHistory implements Iterable<Move> {
    private List<Move> moves;

    public MoveHistory() {
        moves = new ArrayList<>();
    }

    public void addMove(Move move) {
        moves.add(move);
    }

    public void clear() {
        moves.clear();
    }

    @Override
    public Iterator<Move> iterator() {
        return moves.iterator();
    }

    public int size() {
        return moves.size();
    }
}
