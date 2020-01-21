package org.lab1505.map;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.lab1505.credit.BasicMapNode;
import org.lab1505.credit.RealtimeNetEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The MapWithData class is responsible for loading a resource dataset file,
 * map matching resources, and create a list of resource events.
 */
public class MapWithData {

    // Map without any data added to it
    public CityMap map;

    public MapWithData(CityMap map) {
        this.map = map;
    }

    /**
     * Match a point to the closest location on the map
     *
     * @param longitude
     * @param latitude
     * @return
     */
    public LocationOnRoad mapMatch(double longitude, double latitude) {
        Link link = map.getNearestLink(longitude, latitude);
        double xy[] = map.projector().fromLatLon(latitude, longitude);
        double[] snapResult = snap(link.from.getX(), link.from.getY(), link.to.getX(), link.to.getY(), xy[0], xy[1]);
        double distanceFromStartVertex = this.euclideanDistance(snapResult[0], snapResult[1], link.from.getX(), link.from.getY());
        long travelTimeFromStartVertex = Math.round(distanceFromStartVertex / link.length * link.travelTime);
        long travelTimeFromStartIntersection = link.beginTime + travelTimeFromStartVertex;
        return new LocationOnRoad(link.road, travelTimeFromStartIntersection);
    }

    /**
     * Find the closest point on a line segment with end points (x1, y1) and
     * (x2, y2) to a point (x ,y), a procedure called snap.
     *
     * @param x1 x-coordinate of an end point of the line segment
     * @param y1 y-coordinate of an end point of the line segment
     * @param x2 x-coordinate of another end point of the line segment
     * @param y2 y-coordinate of another end point of the line segment
     * @param x  x-coordinate of the point to snap
     * @param y  y-coordinate of the point to snap
     * @return the closest point on the line segment.
     */
    public double[] snap(double x1, double y1, double x2, double y2, double x, double y) {
        double[] snapResult = new double[3];
        double dist;
        double length = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);

        if (length == 0.0) {
            dist = this.euclideanDistance(x1, y1, x, y);
            snapResult[0] = x1;
            snapResult[1] = y1;
            snapResult[2] = dist;
        } else {
            double t = ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1)) / length;
            if (t < 0.0) {
                dist = euclideanDistance(x1, y1, x, y);
                snapResult[0] = x1;
                snapResult[1] = y1;
                snapResult[2] = dist;
            } else if (t > 1.0) {
                dist = euclideanDistance(x2, y2, x, y);
                snapResult[0] = x2;
                snapResult[1] = y2;
                snapResult[2] = dist;
            } else {
                double proj_x = x1 + t * (x2 - x1);
                double proj_y = y1 + t * (y2 - y1);
                dist = euclideanDistance(proj_x, proj_y, x, y);
                snapResult[0] = proj_x;
                snapResult[1] = proj_y;
                snapResult[2] = dist;
            }
        }
        return snapResult;
    }

    /**
     * Compute the Euclidean distance between point (x1, y1) and point (x2, y2).
     */
    public double euclideanDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * Compute the Manhattan distance between point (x1, y1) and point (x2, y2).
     */
    public double manhattanDistance(double x1, double y1, double x2, double y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public ArrayList<Long> placeAgentsRandomly(SimpleDirectedGraph<Long, RealtimeNetEdge> graph, int totalAgents, int agentPlacementRandomSeed) {
        ArrayList<Long> agentLocations = new ArrayList();
        List<Long> totalLocations = new ArrayList<>(graph.vertexSet());
        Random generator = new Random(agentPlacementRandomSeed);
        for (int i = 0; i < totalAgents; i++) {
            long rndNode = totalLocations.get(generator.nextInt(generator.nextInt(totalLocations.size())));
            while(graph.outgoingEdgesOf(rndNode).size()<1){
                rndNode = totalLocations.get(generator.nextInt(generator.nextInt(totalLocations.size())));
            }
            agentLocations.add(rndNode);
        }
        return agentLocations;
    }
}
