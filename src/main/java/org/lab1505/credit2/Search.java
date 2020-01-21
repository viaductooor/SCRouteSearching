package org.lab1505.credit2;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.lab1505.credit.BasicTraveltimeCreditEdge;

import java.util.List;
import java.util.Set;

/**
 * @author Jie Wu
 * @date 2020/1/18 15:24
 */
public class Search {
    public double run(SimpleDirectedGraph<Long, BasicTraveltimeCreditEdge> graph, Set<Long> locations, SearchStrategy strategy, double searchRange) {
        double totalCredit = 0;

        // Init volume graph
        SimpleDirectedGraph<Long, Integer> volumeGraph = new SimpleDirectedGraph<>(Integer.class);
        for (BasicTraveltimeCreditEdge e : graph.edgeSet()) {
            long from = graph.getEdgeSource(e);
            long to = graph.getEdgeTarget(e);
            volumeGraph.addVertex(from);
            volumeGraph.addVertex(to);
            volumeGraph.addEdge(from, to, 0);
        }

        List<List<Long>> routes = strategy.assignRoutes(graph, locations, searchRange);

        // Compute volume graph
        for (List<Long> route : routes) {
            for (int i = 0; i < route.size() - 1; i++) {
                int v = volumeGraph.getEdge(route.get(i), route.get(i + 1));
                volumeGraph.addEdge(route.get(i), route.get(i + 1), v + 1);
            }
        }

        // Compute total credit
        for (BasicTraveltimeCreditEdge e : graph.edgeSet()) {
            long from = graph.getEdgeSource(e);
            long to = graph.getEdgeTarget(e);
            if (volumeGraph.getEdge(from, to) > 0) {
                double credit = e.credit;
                int volume = volumeGraph.getEdge(from, to);
                totalCredit += credit * volume;
            }
        }
        return totalCredit;
    }

    public SimpleDirectedGraph<Long, BasicTraveltimeCreditEdge> copyGraph(SimpleDirectedGraph<Long, BasicTraveltimeCreditEdge> graph) {
        SimpleDirectedGraph<Long, BasicTraveltimeCreditEdge> copy = new SimpleDirectedGraph<>(BasicTraveltimeCreditEdge.class);
        for (BasicTraveltimeCreditEdge e : graph.edgeSet()) {
            long from = graph.getEdgeSource(e);
            long to = graph.getEdgeTarget(e);
            copy.addVertex(from);
            copy.addVertex(to);
            copy.addEdge(from, to, new BasicTraveltimeCreditEdge(e.traveltime, e.credit));
        }
        return copy;
    }
}
