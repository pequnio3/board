package com.blakec.graph;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by blakec on 6/2/15.
 *
 * Path within a graph between two points with the corresponding total weight of the path.
 *
 */
public class Path {
    final List<Vertex> path = Lists.newArrayList();
    final double weight;

    public Path(final List<Vertex> path, final double weight) {
        this.weight = weight;
        this.path.addAll(path);
    }

    public List<Vertex> getPath() {
        return path;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "Path{" +
                "path=" + path +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Path path1 = (Path) o;

        if (Double.compare(path1.weight, weight) != 0) return false;
        if (path != null ? !path.equals(path1.path) : path1.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = path != null ? path.hashCode() : 0;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
