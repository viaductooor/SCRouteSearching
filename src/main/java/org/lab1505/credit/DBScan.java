package org.lab1505.credit;


import org.lab1505.map.Intersection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/30 20:13
 */
public class DBScan {
    private double radius;
    private int minPts;
    private Map<Long, PointInfo> pointInfoMap;
    private ArrayList<Intersection> points;

    public DBScan(double radius, int minPts, ArrayList<Intersection> intersections) {
        this.radius = radius;
        this.minPts = minPts;
        this.points = intersections;
        pointInfoMap = new HashMap<>();
        for (Intersection intersection : intersections) {
            pointInfoMap.put(intersection.id, new PointInfo(false, false, 0));
        }
    }

    /**
     * Compute the Euclidean distance between point (x1, y1) and point (x2, y2).
     */
    public static double euclideanDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static double getDistance(Intersection centerPoint, Intersection p) {
        double centerx = centerPoint.xy.getX();
        double centery = centerPoint.xy.getY();
        double px = p.xy.getX();
        double py = p.xy.getY();
        return euclideanDistance(centerx, centery, px, py);
    }

    private void setVisited(Intersection intersection, boolean isVisited) {
        pointInfoMap.get(intersection.id).visited = isVisited;
    }

    private void setNoise(Intersection intersection, boolean isNoise) {
        pointInfoMap.get(intersection.id).noise = isNoise;
    }

    private void setCluster(Intersection intersection, int cluster) {
        pointInfoMap.get(intersection.id).cluster = cluster;
    }

    private boolean isVisited(Intersection intersection) {
        return pointInfoMap.get(intersection.id).visited;
    }

    private int getCluster(Intersection intersection) {
        return pointInfoMap.get(intersection.id).cluster;
    }

    private boolean isNoise(Intersection intersection) {
        return pointInfoMap.get(intersection.id).noise;
    }

    public Map<Long, PointInfo> process() {
        int size = points.size();
        int idx = 0;
        int cluster = 1;
        while (idx < size) {
            Intersection p = points.get(idx++);
            //choose an unvisited point
            if (!isVisited(p)) {
                setVisited(p, true);//set visited
                ArrayList<Intersection> adjacentPoints = getAdjacentPoints(p, points);
                //set the point which adjacent points less than minPts noised
                if (adjacentPoints != null && adjacentPoints.size() < minPts) {
                    setNoise(p, true);
                } else {
                    setCluster(p, cluster);
                    for (int i = 0; i < adjacentPoints.size(); i++) {
                        Intersection adjacentPoint = adjacentPoints.get(i);
                        //only check unvisited point, cause only unvisited have the chance to add new adjacent points
                        if (!isVisited(adjacentPoint)) {
                            setVisited(adjacentPoint, true);
                            ArrayList<Intersection> adjacentAdjacentPoints = getAdjacentPoints(adjacentPoint, points);
                            //add point which adjacent points not less than minPts noised
                            if (adjacentAdjacentPoints != null && adjacentAdjacentPoints.size() >= minPts) {
                                adjacentPoints.addAll(adjacentAdjacentPoints);
                            }
                        }
                        //add point which doest not belong to any cluster
                        if (getCluster(adjacentPoint) == 0) {
                            setCluster(adjacentPoint, cluster);
                            //set point which marked noised before non-noised
                            if (isNoise(adjacentPoint)) {
                                setNoise(adjacentPoint, false);
                            }
                        }
                    }
                    cluster++;
                }
            }
        }
        return pointInfoMap;
    }

    private ArrayList<Intersection> getAdjacentPoints(Intersection centerPoint, ArrayList<Intersection> points) {
        ArrayList<Intersection> adjacentPoints = new ArrayList<Intersection>();
        for (Intersection p : points) {
            //include centerPoint itself
            double distance = getDistance(centerPoint, p);
            if (distance <= radius) {
                adjacentPoints.add(p);
            }
        }
        return adjacentPoints;
    }

    public static class PointInfo {
        public boolean visited;
        public boolean noise;
        public int cluster;

        public PointInfo(boolean visited, boolean noise, int cluster) {
            this.visited = visited;
            this.noise = noise;
            this.cluster = cluster;
        }

        @Override
        public String toString() {
            return "PointInfo{" +
                    "visited=" + visited +
                    ", noise=" + noise +
                    ", cluster=" + cluster +
                    '}';
        }
    }
}
