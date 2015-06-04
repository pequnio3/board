package com.blakec.graph;

/**
 * Created by blakec on 6/2/15.
 *
 * Directed Edge to a target vertex.  Also contains a weight.
 */
public class Edge {
    protected final Vertex target;
    protected final double weight;

    public Edge(final Vertex target, final double weight) {
        this.target = target;
        this.weight = weight;
    }

    public Vertex getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "target=" + target +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (Double.compare(edge.weight, weight) != 0) return false;
        if (target != null ? !target.equals(edge.target) : edge.target != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = target != null ? target.hashCode() : 0;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
