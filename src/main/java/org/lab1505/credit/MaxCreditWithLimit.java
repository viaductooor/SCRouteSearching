package org.lab1505.credit;

import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.*;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2020/1/9 15:53
 */
public class MaxCreditWithLimit<V, E extends TraveltimeCreditEdge> {
    SimpleDirectedGraph<V, E> graph;

    public MaxCreditWithLimit(SimpleDirectedGraph<V, E> graph) {
        this.graph = graph;
    }

    public LinkedList<V> maxCreditPathWithLimit(V startNode, double timeLimit) {
        double maxCredit = 0;
        double totalTraveltime = 0;
        double totalCredit = 0;
        LinkedList<LinkedList<V>> states = new LinkedList<>();
        LinkedList<V> route = new LinkedList<>();
        LinkedList<V> result = new LinkedList<>();
        states.add(new LinkedList<V>() {
            {
                add(startNode);
            }
        });
        route.add(startNode);
        while (!route.isEmpty()) {
            V last = route.peekLast();
            LinkedList<V> toVisit = new LinkedList<>();
            for (E e : graph.outgoingEdgesOf(last)) {
                V target = graph.getEdgeTarget(e);
                if (!route.contains(target) && e.getTraveltime() + totalTraveltime <= timeLimit) {
                    toVisit.add(target);
                }
            }
            if (toVisit.size() > 0) {
                // Move forward and update the route
                route.addLast(toVisit.getFirst());
                states.addLast(toVisit);
                TraveltimeCreditEdge e = graph.getEdge(last, toVisit.getFirst());
                totalTraveltime += e.getTraveltime();
                totalCredit += e.getCredit();
                if (totalCredit > maxCredit) {
                    maxCredit = totalCredit;
                    result.clear();
                    result.addAll(route);
                }
            } else {
                // Cannot move forward, back track
                while (states.size() > 0 && states.peekLast().size() <= 1) {
                    states.pollLast();
                    V la = route.pollLast();
                    if (states.size() == 0) {
                        totalTraveltime = 0;
                        totalCredit = 0;
                    } else {
                        totalTraveltime -= graph.getEdge(route.peekLast(), la).getTraveltime();
                        totalCredit -= graph.getEdge(route.peekLast(), la).getCredit();
                    }
                }

                if (route.size() > 0) {
                    V removed = states.peekLast().pollFirst();
                    V toAdd = states.peekLast().getFirst();
                    route.pollLast();
                    totalTraveltime -= graph.getEdge(route.peekLast(), removed).getTraveltime();
                    totalCredit -= graph.getEdge(route.peekLast(), removed).getCredit();
                    totalTraveltime += graph.getEdge(route.peekLast(), toAdd).getTraveltime();
                    totalCredit += graph.getEdge(route.peekLast(), toAdd).getCredit();
                    route.addLast(toAdd);
                    if (totalCredit > maxCredit) {
                        maxCredit = totalCredit;
                        result.clear();
                        result.addAll(route);
                    }
                }
            }
        }
        return result;
    }

    public LinkedList<V> greedy(V startNode, double searchRange) {
        double totalTraveltime = 0;
        LinkedList<V> route = new LinkedList<>();
        Set<V> routeSet = new HashSet<>();
        route.add(startNode);
        routeSet.add(startNode);
        while(true){
            Set<E> adjacents = graph.outgoingEdgesOf(route.getLast());
            V maxv = null;
            double maxvalue = Double.MIN_VALUE;
            double maxtraveltime = 0;
            for(E e:adjacents){
                V target = graph.getEdgeTarget(e);
                if(!routeSet.contains(target)){
                    double value = e.getCredit()/e.getTraveltime();
                    if(value>maxvalue){
                        maxvalue = value;
                        maxtraveltime = e.getTraveltime();
                        maxv = target;
                    }
                }
            }
            if(maxv==null){
                break;
            }else{
                if(totalTraveltime+maxtraveltime<=searchRange){
                    totalTraveltime += maxtraveltime;
                    route.addLast(maxv);
                    routeSet.add(maxv);
                }else{
                    break;
                }
            }
        }
        return route;
    }

    public LinkedList<V> random(V startNode, double searchRange) {
        double totalTraveltime = 0;
        LinkedList<V> route = new LinkedList<>();
        Set<V> routeSet = new HashSet<>();
        route.add(startNode);
        while(true){
            List<E> adjacents = new ArrayList<>(graph.outgoingEdgesOf(route.getLast()));
            if(adjacents.size()<1){
                break;
            }else{
                Random rnd = new Random(31);
                int rndInt = rnd.nextInt(adjacents.size());
                V rndVertex = graph.getEdgeTarget(adjacents.get(rndInt));
                double rndTraveltime = adjacents.get(rndInt).getTraveltime();
                if(totalTraveltime + rndTraveltime <=searchRange){
                    totalTraveltime += rndTraveltime;
                    route.add(rndVertex);
                }else{
                    break;
                }
            }
        }
        return route;
    }
}
