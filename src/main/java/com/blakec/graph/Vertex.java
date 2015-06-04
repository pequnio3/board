package com.blakec.graph;

/**
 * Created by blakec on 6/2/15.
 */
public class Vertex<T> {
    protected final T value;

    public Vertex(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vertex vertex = (Vertex) o;

        if (value != null ? !value.equals(vertex.value) : vertex.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Vertex{" +
                value +
                '}';
    }
}
