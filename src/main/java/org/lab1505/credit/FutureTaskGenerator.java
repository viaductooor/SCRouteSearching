package org.lab1505.credit;

import org.lab1505.map.CityMap;
import org.lab1505.map.Intersection;

import java.util.*;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/29 10:28
 * <p>
 * FutureTaskGenerator can generate a series of tasks at a specific time period
 * on the map which obey a given distribution of location.
 */
public class FutureTaskGenerator {
    private CityMap cityMap;
    private HistoricalModel historicalModel;
    private long windowTimeSeconds;

    public FutureTaskGenerator(CityMap cityMap, HistoricalModel historicalModel) {
        this.cityMap = cityMap;
        this.historicalModel = historicalModel;
        windowTimeSeconds = historicalModel.windowTimeSeconds;

    }

    public List<CSTask> generateTasks(int taskNumber, long timeSeconds) {
        // Run clustering
        DBScan dbScan = new DBScan(115.7, 4, new ArrayList<>(cityMap.intersections().values()));
        Map<Long, DBScan.PointInfo> clusterResult = dbScan.process();

        // Get number of clusters which is the result of DBScan clustering
        int nClusters = clusterResult.values().stream().mapToInt(info -> info.cluster).max().getAsInt();

        // Get heat value of every intersection
        Map<Long, Double> heatMap = historicalModel.getHeatMap(timeSeconds);

        // Get heat value of every cluster
        Map<Integer, Double> clusterHeatMap = new HashMap<Integer, Double>() {
            {
                for (Entry<Long, Double> entry : heatMap.entrySet()) {
                    long intersectionId = entry.getKey();
                    final double itnersectionHeat = entry.getValue();
                    int cluster = clusterResult.get(intersectionId).cluster;
                    if (cluster > 0) {
                        if (!containsKey(cluster)) {
                            put(cluster, itnersectionHeat);
                        } else {
                            compute(cluster, (k, v) -> v += itnersectionHeat);
                        }
                    }
                }
            }
        };

        // Get intersections of every cluster
        Map<Integer, List<Long>> clusterMap = new HashMap<Integer, List<Long>>() {
            {
                for (Entry<Long, DBScan.PointInfo> e : clusterResult.entrySet()) {
                    int cluster = e.getValue().cluster;
                    long intersectionId = e.getKey();
                    if (cluster > 0) {
                        if (!containsKey(cluster)) {
                            List<Long> ilist = new ArrayList<>();
                            ilist.add(intersectionId);
                            put(cluster, ilist);
                        } else {
                            get(cluster).add(intersectionId);
                        }
                    }
                }
            }
        };

        List<CSTask> tasks = new LinkedList<>();
        double totalHeat = heatMap.values().stream().mapToDouble(x -> x).sum();

        // Every share of heat corresponds to a task.
        double share = totalHeat / taskNumber;

        /**
         * Let nt be the number of tasks which are supposed to be in that cluster,
         * then we should create nt tasks which are located randomly in
         * the cluster.
         */
        Random rnd = new Random();
        for (Map.Entry<Integer, Double> e : clusterHeatMap.entrySet()) {
            int clusterId = e.getKey();
            List<Long> intersectionIds = clusterMap.get(clusterId);
            int nt = (int) Math.floor(e.getValue() / share);
            if (nt > 0) {
                for (int i = 0; i < nt; i++) {
                    int rndIndex = rnd.nextInt(intersectionIds.size());
                    long rndIntersectionId = intersectionIds.get(rndIndex);
                    Intersection rndIntersection = cityMap.intersections().get(rndIntersectionId);
                    CSTask rndTask = new CSTask(timeSeconds, 60 * 60, rndIntersection);
                    tasks.add(rndTask);
                }
            }
        }
        return tasks;
    }

}
