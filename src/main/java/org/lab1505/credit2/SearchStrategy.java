package org.lab1505.credit2;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.lab1505.credit.BasicTraveltimeCreditEdge;

import java.util.List;
import java.util.Set;

/**
 * @author Jie Wu
 * @date 2020/1/18 15:20
 */
public interface SearchStrategy {
    public List<List<Long>> assignRoutes(SimpleDirectedGraph<Long, BasicTraveltimeCreditEdge> graph, Set<Long> locations, double searchTimeRange);
}
