package com.blakec;

import com.blakec.graph.Vertex;

/**
 * Created by blakec on 6/2/15.
 * <p/>
 * <p/>
 * Class that represents a position on a board in row, column coordinates.
 * It is also a vertex and can be used in a graph.
 */
public class Position implements Vertex {
    /**
     * Row position.
     */
    final int r;
    /**
     * Column position.
     */
    final int c;

    public Position(final int r, final int c) {
        this.r = r;
        this.c = c;
    }

    public int getR() {
        return r;
    }

    public int getC() {
        return c;
    }

    /**
     * Add a position to another.
     *
     * @param p position to add.
     * @return new position with coordinates added.
     */
    public Position add(Position p) {
        return new Position(r + p.getR(), c + p.getC());
    }

    @Override
    public String toString() {
        return "Position{" +
                "r=" + r +
                ", c=" + c +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (c != position.c) return false;
        if (r != position.r) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = r;
        result = 31 * result + c;
        return result;
    }
}
