package com.blakec.graph;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph<T> {
    public static final double POSITIVE_INFINITY = Double.MAX_VALUE;
    final Map<Vertex, Set<Edge>> graph = Maps.newHashMap();
    int numPathsSeen = 0;

    public boolean hasEdge(final Vertex<T> v, final Vertex<T> u, final long weight) {
        if (!graph.containsKey(v)) {
            return false;
        }
        final Edge e = new Edge(u, weight);
        if (graph.get(v) != null && graph.get(v).contains(e)) {
            return true;
        }
        return false;
    }

    public void addEdge(final Vertex<T> start, final Vertex<T> end, final double weight) {
        final Set<Edge> edges = graph.containsKey(start) ?
                graph.get(start) :
                Sets.<Edge>newHashSet();
        edges.add(new Edge(end, weight));
        graph.put(start, edges);
        if (!graph.containsKey(end)) {
            graph.put(end, Sets.<Edge>newHashSet());
        }
    }

    public Set<Edge> getEdges(final Vertex<T> v) {
        return graph.get(v);
    }

    /**
     * Computes the longest path from source to target.  NOTE: This is brute force and should not be run on large graphs.
     * <p/>
     * Also does not use edge weights.
     *
     * @param source
     * @param target
     * @return
     * @throws Exception
     */
    public Path computeLongestPath(final Vertex<T> source, final Vertex<T> target, int maxPathSize) throws Exception {
        final Set<Vertex<T>> seen = Sets.newHashSet();
        final List<Vertex<T>> longestPath = Lists.newArrayList();
        final List<Vertex<T>> currentPath = Lists.newArrayList();
        computeLongestPathRecursive(source, target, seen, longestPath, currentPath, maxPathSize);
        return new Path(longestPath, longestPath.size() - 1);
    }

    public void computeLongestPathRecursive(final Vertex<T> v,
                                            final Vertex<T> target,
                                            final Set<Vertex<T>> seen,
                                            final List<Vertex<T>> longestPath,
                                            final List<Vertex<T>> currentPath,
                                            final int maxPathSize) throws Exception {
        seen.add(v);

        currentPath.add(v);
        if (target.equals(v)) {
            numPathsSeen++;
            if (currentPath.size() > longestPath.size()) {
                // we reached the target.  this is a path.
                longestPath.clear();
                longestPath.addAll(currentPath);
            }
            currentPath.remove(v);
            seen.remove(v);
            return;
        }

        List<Edge> unvisitedNeighbors = getNeighborsSortedByFewestNeighbors(v, seen);
        for (final Edge e : unvisitedNeighbors) {
            final Vertex<T> neighbor = e.getTarget();
            computeLongestPathRecursive(neighbor, target, seen, longestPath, currentPath, maxPathSize);
            if (longestPath.size() == maxPathSize) {
                return;
            }
        }
        currentPath.remove(v);
        seen.remove(v);
    }

    public Set<Edge> getUnvisitedNeighbors(final Vertex<T> v, final Set<Vertex<T>> seen) {
        Set<Edge> neighbors = getEdges(v);
        Set<Edge> unvisitedNeighbors = Sets.newHashSet(Collections2.filter(neighbors, new Predicate<Edge>() {
            @Override
            public boolean apply(final Edge edge) {
                return !seen.contains(edge.getTarget());
            }
        }));
        return unvisitedNeighbors;
    }

    public List<Edge> getNeighborsSortedByFewestNeighbors(final Vertex<T> v, final Set<Vertex<T>> seen) {
        Set<Edge> unvisitedNeighbors = getUnvisitedNeighbors(v, seen);
        List<Edge> sortedNeighbors = Lists.newArrayList(unvisitedNeighbors);
        sortedNeighbors.sort(new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                Vertex<T> v1 = o1.getTarget();
                Vertex<T> v2 = o2.getTarget();
                int numNeighbors1 = Graph.this.getUnvisitedNeighbors(v1, seen).size();
                int numNeighbors2 = Graph.this.getUnvisitedNeighbors(v2, seen).size();
                // return 1 if v2 has fewer neighbors than v1
                // return 0 if equal
                // return -1 if v1 has fewer neighbors than v2
                return Integer.compare(numNeighbors1,numNeighbors2);
            }
        });
        return sortedNeighbors;
    }

    public Path computeShortestPath(final Vertex<T> source, final Vertex<T> target) throws Exception {
        final FibonacciHeap<Vertex<T>> priorityQueue = new FibonacciHeap<Vertex<T>>();
        final Map<Vertex<T>, FibonacciHeap.Entry<Vertex<T>>> entryPointers = Maps.newHashMap();
        final Map<Vertex<T>, Vertex<T>> optimalPathVerticies = Maps.newHashMap();
        for (Vertex<T> v : graph.keySet()) {
            double priority = v.equals(source) ? 0.0 : POSITIVE_INFINITY;
            final FibonacciHeap.Entry<Vertex<T>> entry = priorityQueue.enqueue(v, priority);
            entryPointers.put(v, entry);
        }

        double optimalPathLength = POSITIVE_INFINITY;

        while (!priorityQueue.isEmpty()) {
            final FibonacciHeap.Entry<Vertex<T>> uEntry = priorityQueue.dequeueMin();
            final Vertex<T> u = uEntry.getValue();
            final double distanceToU = uEntry.getPriority();
            if (target.equals(u)) {
                optimalPathLength = distanceToU;
            }
            for (final Edge e : getEdges(u)) {
                final Vertex<T> v = e.getTarget();
                final double weight = e.getWeight();
                final double distanceThroughUAndV = distanceToU + weight;

                final FibonacciHeap.Entry<Vertex<T>> vEntry = entryPointers.get(v);
                if (vEntry == null) {
                    System.out.println("");
                }
                final double minDistanceThroughV = vEntry.getPriority();

                if (distanceThroughUAndV < minDistanceThroughV) {
                    priorityQueue.decreaseKey(vEntry, distanceThroughUAndV);
                    optimalPathVerticies.put(v, u);
                }
            }
        }
        if (optimalPathLength == POSITIVE_INFINITY) {
            return new Path(Lists.<Vertex<T>>newArrayList(), POSITIVE_INFINITY);
        }
        final List<Vertex<T>> optimalPath = Lists.newArrayList();
        optimalPath.add(target);

        Vertex<T> next = optimalPathVerticies.get(target);
        while (!source.equals(next)) {
            optimalPath.add(0, next);
            next = optimalPathVerticies.get(next);
        }
        optimalPath.add(0, source);

        // reverse since original was path from target to source, instead of source to target
        return new Path(optimalPath, optimalPathLength);
    }

}
