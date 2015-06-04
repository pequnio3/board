package com.blakec.graph;

import com.blakec.Position;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by blakec on 6/2/15.
 */
public class GraphTest {
    Graph graph;

    @Before
    public void setup() {
        graph = new Graph();
    }

    @Test
    public void test_simple() throws Exception {
        Vertex v0 =new Position(0, 0);
        Vertex v1 = new Position(1, 1);
        Vertex v2 = new Position(2, 2);
        Vertex v3 = new Position(3, 3);
        Vertex v4 = new Position(4, 4);
        Vertex v5 = new Position(5, 5);
        Vertex v6 = new Position(6, 6);
        final Graph graph = new Graph();
        graph.addEdge(v0, v1, 80);
        graph.addEdge(v1, v0, 80);
        graph.addEdge(v0, v5, 82);
        graph.addEdge(v5, v0, 82);
        graph.addEdge(v1, v2, 40);
        graph.addEdge(v2, v1, 40);
        graph.addEdge(v1, v3, 103);
        graph.addEdge(v3, v1, 103);
        graph.addEdge(v3, v5, 62);
        graph.addEdge(v5, v3, 62);
        graph.addEdge(v3, v6, 97);
        graph.addEdge(v6, v3, 97);
        graph.addEdge(v4, v5, 133);
        graph.addEdge(v5, v4, 133);
        graph.addEdge(v5, v6, 192);
        graph.addEdge(v6, v5, 192);

        Path p = graph.computeShortestPath(v0, v6);
        Path correct = new Path(Lists.newArrayList(v0, v5, v3, v6), 241);
        assertEquals(correct, p);
    }

    @Test
    public void test_simple_noSolution() throws Exception {
        Vertex v0 = new Position(0, 0);
        Vertex v1 = new Position(1, 1);
        Vertex v2 = new Position(2, 2);
        Vertex v3 = new Position(3, 3);
        Vertex v4 = new Position(4, 4);
        Vertex v5 = new Position(5, 5);
        Vertex v6 = new Position(6, 6);
        Vertex v7 = new Position(7, 7);
        final Graph graph = new Graph();
        graph.addEdge(v0, v1, 80);
        graph.addEdge(v1, v0, 80);
        graph.addEdge(v0, v5, 82);
        graph.addEdge(v5, v0, 82);
        graph.addEdge(v1, v2, 40);
        graph.addEdge(v2, v1, 40);
        graph.addEdge(v1, v3, 103);
        graph.addEdge(v3, v1, 103);
        graph.addEdge(v3, v5, 62);
        graph.addEdge(v5, v3, 62);
        graph.addEdge(v3, v6, 97);
        graph.addEdge(v6, v3, 97);
        graph.addEdge(v4, v5, 133);
        graph.addEdge(v5, v4, 133);
        graph.addEdge(v5, v6, 192);
        graph.addEdge(v6, v5, 192);

        Path p = graph.computeShortestPath(v0, v7);
        Path correct = new Path(Lists.<Vertex>newArrayList(), Graph.POSITIVE_INFINITY);
        assertEquals(correct, p);
    }

    @Test
    public void test_sortingNeighbors() throws Exception {
        Vertex v0 = new Position(0, 0);
        Vertex v1 = new Position(1, 1);
        Vertex v2 = new Position(2, 2);
        Vertex v3 = new Position(3, 3);
        Vertex v4 = new Position(4, 4);
        Vertex v5 = new Position(5, 5);
        Vertex v6 = new Position(6, 6);
        Vertex v7 = new Position(7, 7);
        final Graph graph = new Graph();
        graph.addEdge(v0, v1, 80);
        graph.addEdge(v0, v2, 82);
        graph.addEdge(v0, v3, 82);
        graph.addEdge(v4, v5, 40);
        graph.addEdge(v2, v4, 40);
        graph.addEdge(v2, v0, 40);


        List<Edge> e = graph.getNeighborsSortedByFewestNeighbors(v2, Sets.<Vertex>newHashSet());
        Path correct = new Path(Lists.<Vertex>newArrayList(), Graph.POSITIVE_INFINITY);

    }
}
