package com.blakec;

/**
 * Created by blakec on 6/2/15.
 */
public class Position {
    final int r;
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
