package com.blakec.graph;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Graph class that has weighted directed edges.
 * <p/>
 * Has the ability to compute longest path via depth first search.
 * <p/>
 * Has the ability to compute shortest path via Dijkstra's Algorithm and priority queue.
 */
public class Graph {
    public static final double POSITIVE_INFINITY = Double.MAX_VALUE;
    final Map<Vertex, Set<Edge>> graph = Maps.newHashMap();
    int numPathsSeen = 0;

    /*****************************
     ********** CREATION *********
     *****************************/

    /**
     * Remove the vertex from the graph and remove all edges pointing to the vertex.
     *
     * @param v vertex to be removed
     */
    public void removeVertex(final Vertex v) {
        graph.remove(v);
        for (final Set<Edge> neighbors : graph.values()) {
            final Iterator<Edge> neighborIt = neighbors.iterator();
            while (neighborIt.hasNext()) {
                final Edge neighbor = neighborIt.next();
                if (v.equals(neighbor.getTarget())) {
                    neighborIt.remove();
                }
            }
        }
    }

    /**
     * Add an edge from start to end with weight.
     *
     * @param start  start vertex.
     * @param end    end vertex.
     * @param weight weight.
     */
    public void addEdge(final Vertex start, final Vertex end, final double weight) {
        final Set<Edge> edges = graph.containsKey(start) ?
                graph.get(start) :
                Sets.<Edge>newHashSet();
        edges.add(new Edge(end, weight));
        graph.put(start, edges);
        if (!graph.containsKey(end)) {
            graph.put(end, Sets.<Edge>newHashSet());
        }
    }

    /**
     * Gets all the edges going out of v.
     *
     * @param v vertex.
     * @return set of edges going out of v.
     */
    public Set<Edge> getEdges(final Vertex v) {
        return graph.get(v);
    }

    /**********************************
     ********** LONGEST PATH *********
     **********************************/

    /**
     * Computes the longest path from source to target. This uses Warnsdorf's rules as a
     * heuristic to speed up graph traversal.
     * <p/>
     * Warnsdorf's rule results in an ordering where neighbors who have fewer neighbors are closer to the
     * top of the list.
     * <p/>
     * Also does not use edge weights.
     * <p/>
     * NOTE: This is brute force and should not be run on large graphs.
     *
     * @param source
     * @param target
     * @return
     * @throws Exception
     */
    public Path computeLongestPath(final Vertex source, final Vertex target, int maxPathSize) throws Exception {
        final Set<Vertex> seen = Sets.newHashSet();
        final List<Vertex> longestPath = Lists.newArrayList();
        final List<Vertex> currentPath = Lists.newArrayList();
        computeLongestPathRecursive(source, target, seen, longestPath, currentPath, maxPathSize);
        return new Path(longestPath, longestPath.size() - 1);
    }

    /**
     * Recursive part of compute longest path.
     *
     * @param v           vertex to start at.
     * @param target      target vertex.
     * @param visited     nodes we have seen so far and shouldn't be traversed
     * @param longestPath current longest path.
     * @param currentPath current path we are on.
     * @param maxPathSize upper bound of path size in this graph.
     */
    public void computeLongestPathRecursive(final Vertex v,
                                            final Vertex target,
                                            final Set<Vertex> visited,
                                            final List<Vertex> longestPath,
                                            final List<Vertex> currentPath,
                                            final int maxPathSize) throws Exception {
        // add vertex to set of seen
        visited.add(v);
        // add vertex to current path
        currentPath.add(v);
        // if the vertex is the target, we have a path
        if (target.equals(v)) {
            // increase number of paths seen, just for diagnostics
            numPathsSeen++;
            if (currentPath.size() > longestPath.size()) {
                // we reached the target.  this is a path that has a larger size than current largest
                longestPath.clear();
                longestPath.addAll(currentPath);
            }
            // backtrack vertex off current path
            currentPath.remove(v);
            // backtrack vertex off seen set.
            visited.remove(v);
            return;
        }

        // get neighbors of this vertex
        // it will not include seen neighbors and will order the neighbors according to
        // Warnsdorf's rule.
        List<Edge> unvisitedNeighbors = getNeighborsSortedByFewestNeighbors(v, visited);
        for (final Edge e : unvisitedNeighbors) {
            // for each neighbor
            // find the longest path from it to the target
            final Vertex neighbor = e.getTarget();
            computeLongestPathRecursive(neighbor, target, visited, longestPath, currentPath, maxPathSize);
            if (longestPath.size() == maxPathSize) {
                // if we have already achieved a path with max possible size, return.
                return;
            }
        }
        // backtrack vertex off current path
        currentPath.remove(v);
        // backtrack vertex off seen set.
        visited.remove(v);
    }

    /**
     * Get neighbors of this vertex who haven't been visited.
     *
     * @param v       vertex
     * @param visited vertices that have already been visited.
     * @return set of Edges to unvisited neighbors.
     */
    public Set<Edge> getUnvisitedNeighbors(final Vertex v, final Set<Vertex> visited) {
        Set<Edge> neighbors = getEdges(v);
        Set<Edge> unvisitedNeighbors = Sets.newHashSet(Collections2.filter(neighbors, new Predicate<Edge>() {
            @Override
            public boolean apply(final Edge edge) {
                return !visited.contains(edge.getTarget());
            }
        }));
        return unvisitedNeighbors;
    }

    /**
     * get unvisited neighbors of this vertex and order the neighbors according to
     * Warnsdorf's rule.
     * <p/>
     * Warnsdorf's rule results in an ordering where neighbors who have fewer neighbors are closer to the
     * top of the list.
     *
     * @param v
     * @param seen
     * @return
     */
    public List<Edge> getNeighborsSortedByFewestNeighbors(final Vertex v, final Set<Vertex> seen) {
        Set<Edge> unvisitedNeighbors = getUnvisitedNeighbors(v, seen);
        List<Edge> sortedNeighbors = Lists.newArrayList(unvisitedNeighbors);
        sortedNeighbors.sort(new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                Vertex v1 = o1.getTarget();
                Vertex v2 = o2.getTarget();
                int numNeighbors1 = Graph.this.getUnvisitedNeighbors(v1, seen).size();
                int numNeighbors2 = Graph.this.getUnvisitedNeighbors(v2, seen).size();
                // return 1 if v2 has fewer neighbors than v1
                // return 0 if equal
                // return -1 if v1 has fewer neighbors than v2
                return Integer.compare(numNeighbors1, numNeighbors2);
            }
        });
        return sortedNeighbors;
    }

    /**********************************
     ********** SHORTEST PATH *********
     **********************************/

    /**
     * Computes the shortest path from source to target using Dijkstra's algorithm.  Uses a
     * priority queue.
     *
     * @param source source vertex.
     * @param target target vertex.
     * @return Shortest path from source to target.
     */
    public Path computeShortestPath(final Vertex source, final Vertex target) {
        final FibonacciHeap<Vertex> priorityQueue = new FibonacciHeap<Vertex>();
        // mapping from vertex to entry in the heap.
        // useful for updating an entry's priority in the heap
        final Map<Vertex, FibonacciHeap.Entry<Vertex>> entryPointers = Maps.newHashMap();
        // mapping of node in the optimal path to the not it points to.
        final Map<Vertex, Vertex> optimalPathVerticies = Maps.newHashMap();

        for (final Vertex v : graph.keySet()) {
            // iterate through all vertices
            // initialize their priority to 0 if source node,  pos infinity otherwise
            // also store a pointer from the vertex to its entry in the queue.
            double priority = v.equals(source) ? 0.0 : POSITIVE_INFINITY;
            final FibonacciHeap.Entry<Vertex> entry = priorityQueue.enqueue(v, priority);
            entryPointers.put(v, entry);
        }

        double optimalPathLength = POSITIVE_INFINITY;

        while (!priorityQueue.isEmpty()) {
            // pop of highest priority vertex in queue
            // priority here represents shortest known distance from that vertex to the source
            final FibonacciHeap.Entry<Vertex> uEntry = priorityQueue.dequeueMin();
            final Vertex u = uEntry.getValue();
            final double distanceToU = uEntry.getPriority();
            if (target.equals(u)) {
                // we have reached our target
                optimalPathLength = distanceToU;
                // we are done
                break;
            }

            for (final Edge e : getEdges(u)) {
                // look at neighbors of this vertex
                // see if going through u->v
                // creates a shorter path to target
                // than we currently have through v
                final Vertex v = e.getTarget();
                final double weight = e.getWeight();
                final double distanceThroughUAndV = distanceToU + weight;

                final FibonacciHeap.Entry<Vertex> vEntry = entryPointers.get(v);
                final double minDistanceThroughV = vEntry.getPriority();

                if (distanceThroughUAndV < minDistanceThroughV) {
                    // going from u through v results in a shorter path
                    // update the priority of this node in the queue
                    priorityQueue.decreaseKey(vEntry, distanceThroughUAndV);
                    // store that v <- us is the best path from target to source we have so far
                    optimalPathVerticies.put(v, u);
                }
            }
        }
        if (optimalPathLength == POSITIVE_INFINITY) {
            return new Path(Lists.<Vertex>newArrayList(), POSITIVE_INFINITY);
        }
        // add target to the path
        final List<Vertex> optimalPath = Lists.newArrayList();
        optimalPath.add(target);

        Vertex next = optimalPathVerticies.get(target);
        while (!source.equals(next)) {
            optimalPath.add(0, next);
            next = optimalPathVerticies.get(next);
        }
        optimalPath.add(0, source);

        // reverse since original was path from target to source, instead of source to target
        return new Path(optimalPath, optimalPathLength);
    }
}
