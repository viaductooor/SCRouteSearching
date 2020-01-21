package org.lab1505.credit;

import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Jie Wu
 * @date 2020/1/16 12:03
 */
public class SearchAlgorithm {
    public static void game(
            final SimpleDirectedGraph<BasicMapNode, RealtimeNetEdge> map,
            final List<Long> locations,
            final Map<Long, Double> heatMap,
            double searchRange,
            int iteration,
            BiConsumer<SimpleDirectedGraph<Long, Marginal>, Integer> outputConsumer) {

        SimpleDirectedGraph<Long, Marginal> marginalGraph = new SimpleDirectedGraph<>(Marginal.class);
        /**
         * Init marginal graph
         */
        for (RealtimeNetEdge e : map.edgeSet()) {
            long from = map.getEdgeSource(e).id;
            long to = map.getEdgeTarget(e).id;
            double otherVolume = e.taxiVolume + e.otherVolume;
            double tripPossibility = 0.001;
            if (heatMap.containsKey(from)) {
                tripPossibility += heatMap.get(from);
            }
            if (heatMap.containsKey(to)) {
                tripPossibility += heatMap.get(to);
            }
            marginalGraph.addVertex(from);
            marginalGraph.addVertex(to);
            marginalGraph.addEdge(from, to, new Marginal(otherVolume, tripPossibility, e.length, e.numLanes, e.traveltime));
        }

        int count = 0;
        while (++count < iteration + 1) {
            // Clear taxi volume
            for (Marginal e : marginalGraph.edgeSet()) {
                e.taxiVolume = 0;
            }
            // Greedy search and deploy workers
            MaxCreditWithLimit<Long, Marginal> mcl = new MaxCreditWithLimit<>(marginalGraph);
            for (long locationId : locations) {
                LinkedList<Long> creditRoute = mcl.greedy(locationId, searchRange);
                for (int i = 0; i < creditRoute.size() - 1; i++) {
                    Marginal edge = marginalGraph.getEdge(creditRoute.get(i), creditRoute.get(i + 1));
                    edge.taxiVolume += 1 * (3600 / searchRange); // Other volume is an hour's volume, so is taxi volume.
                }
            }
            // Update marginal cost and Surcharge
            //Calculate all surcharge and update weight of the edges
            for (Marginal edge : marginalGraph.edgeSet()) {
                edge.updateMarginalCost();
                double surcharge = (1.0 / count) * edge.marginalCost + (1 - (1.0 / count)) * edge.lastSurcharge;
                edge.lastSurcharge = edge.surcharge;
                edge.surcharge = surcharge;
                edge.updateTraveltime();
                edge.updateUtility();
            }
            outputConsumer.accept(marginalGraph, count);
        }
    }

    public static void random(
            final SimpleDirectedGraph<BasicMapNode, RealtimeNetEdge> map,
            final List<Long> locations,
            double searchRange,
            Consumer<SimpleDirectedGraph<Long, Marginal>> outputConsumer) {

        SimpleDirectedGraph<Long, Marginal> marginalGraph = new SimpleDirectedGraph<>(Marginal.class);
        /**
         * Init marginal graph
         */
        for (RealtimeNetEdge e : map.edgeSet()) {
            long from = map.getEdgeSource(e).id;
            long to = map.getEdgeTarget(e).id;
            double otherVolume = e.taxiVolume + e.otherVolume;
            double tripPossibility = 0;
            marginalGraph.addVertex(from);
            marginalGraph.addVertex(to);
            marginalGraph.addEdge(from, to, new Marginal(otherVolume, tripPossibility, e.length, e.numLanes, e.traveltime));
        }

        // Clear taxi volume
        for (Marginal e : marginalGraph.edgeSet()) {
            e.taxiVolume = 0;
        }
        // Random search and deploy workers
        MaxCreditWithLimit<Long, Marginal> mcl = new MaxCreditWithLimit<>(marginalGraph);
        for (long locationId : locations) {
            LinkedList<Long> creditRoute = mcl.random(locationId, searchRange);
            for (int i = 0; i < creditRoute.size() - 1; i++) {
                Marginal edge = marginalGraph.getEdge(creditRoute.get(i), creditRoute.get(i + 1));
                edge.taxiVolume += 1 * (3600 / searchRange); // Other volume is an hour's volume, so is taxi volume.
            }
        }
        // Update marginal cost and Surcharge
        //Calculate all surcharge and update weight of the edges
        for (Marginal edge : marginalGraph.edgeSet()) {
            edge.updateMarginalCost();
            edge.updateTraveltime();
        }

        outputConsumer.accept(marginalGraph);
    }

    public static class Marginal implements TraveltimeCreditEdge {
        public double otherVolume;
        public double taxiVolume;
        public double utility;
        public double marginalCost;
        public double surcharge;
        public double lastSurcharge;
        public double tripPossibility;
        public double traveltime;
        public double length;
        public int numLanes;
        public double computedTraveltime;


        public Marginal(double otherVolume, double tripPossibility, double length, int numLanes, double traveltime) {
            this.otherVolume = otherVolume;
            this.tripPossibility = tripPossibility;
            this.length = length;
            this.numLanes = numLanes;
            this.marginalCost = 0;
            this.surcharge = 0;
            this.lastSurcharge = 0;
            this.utility = 0;
            this.traveltime = 0;
            this.traveltime = traveltime;
            updateUtility();
        }


        private void updateTraveltime() {
            this.computedTraveltime = computeTraveltime(otherVolume, taxiVolume, numLanes, length);
        }

        /**
         *
         */
        public void updateUtility() {
            this.utility = tripPossibility / (traveltime + surcharge);
        }

        public void updateMarginalCost() {
            double volumeIn = computeVolumeIn(taxiVolume, otherVolume, numLanes);
            double marginalCost = computeMarginalCost(volumeIn, length);
            this.marginalCost = marginalCost;
        }

        private double computeMarginalCost(double volumeIn, double length) {
            double marginalCost;
            if (volumeIn < 785) {
                double sq = Math.sqrt(473967 - 600 * volumeIn);
                marginalCost = 15 * length * (300 * volumeIn / sq + sq - 687) / (88 * volumeIn);

            } else {
                double sq = Math.sqrt(600 * volumeIn - 468033);
                marginalCost = 15 * length * ((300 * 1570 - 300 * volumeIn) / sq + sq + 687) / (88 * 1570 - 88 * volumeIn);
            }
            return marginalCost;
        }

        private double computeVolumeIn(double taxiVolume, double otherVolume, int numLanes) {
            double volumeIn = (taxiVolume + otherVolume) / numLanes;
            if (volumeIn > 1569) {
                volumeIn = 1569;
            } else if (volumeIn < 4) {
                volumeIn = 4;
            }
            return volumeIn;
        }

        private double computeTraveltime(double otherVolume, double taxiVolume, int numLanes, double length) {
            double volumeIn = computeVolumeIn(taxiVolume, otherVolume, numLanes);
            double traveltime;

            if (volumeIn < 785) {
                traveltime = 15 * length * (687.0 - Math.sqrt(473967 - 600 * volumeIn)) / (88 * volumeIn);
            } else {
                traveltime = 15 * length * (687.0 + Math.sqrt(600 * volumeIn - 468033)) / (88 * 1570 - 88 * volumeIn);
            }
            return traveltime;
        }

        @Override
        public double getCredit() {
            return utility;
        }

        @Override
        public double getTraveltime() {
            return traveltime + surcharge;
        }
    }
}
