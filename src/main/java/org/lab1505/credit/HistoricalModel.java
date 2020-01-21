package org.lab1505.credit;

import org.lab1505.fileUtils.CSVNewYorkParser;
import org.lab1505.map.CityMap;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/29 10:29
 * <p>
 * HistoricalModel is a data set which contains all the taxi trip data, especially
 * those from TLC Trip Record Data Yellow.
 * <p>
 * After a series of simple filtering and classifying procedures, the model can provide
 * trip demand at any arbitrary spatial-temporal segment.
 */
public class HistoricalModel {
    public int windowTimeSeconds;
    private Map<Long, double[]> classifiedData;

    public HistoricalModel(CityMap cityMap, String[] resourceDataUrls, int windowTimeSeconds) {
        this.classifiedData = loadAndclassify(cityMap, resourceDataUrls, windowTimeSeconds);
        this.windowTimeSeconds = windowTimeSeconds;
    }

    public HistoricalModel(Map<Long, double[]> classifiedData) {
        this.classifiedData = classifiedData;
        for (Long k : classifiedData.keySet()) {
            windowTimeSeconds = 3600 * 24 / classifiedData.get(k).length;
            break;
        }
    }

    public static HistoricalModel fromFile(String url) {
        Map<Long, double[]> classifiedData = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(url))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 10) {
                    String[] items = line.split(" ");
                    long iid = Long.parseLong(items[0]);
                    double[] heats = new double[items.length - 1];
                    for (int i = 0; i < heats.length; i++) {
                        heats[i] = Double.parseDouble(items[i + 1]);
                    }
                    classifiedData.put(iid, heats);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HistoricalModel(classifiedData);
    }


    public void toFile(String url) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(url))) {
            for (Map.Entry<Long, double[]> e : classifiedData.entrySet()) {
                writer.write(e.getKey() + "");
                for (double heat : e.getValue()) {
                    writer.write(" " + heat);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Long, Double> getHeatMap(long timeSeconds) {
        Map<Long, Double> heatMap = new HashMap<>();
        int timeSegment = getTimeSegment(timeSeconds, windowTimeSeconds);
        for (Map.Entry<Long, double[]> e : classifiedData.entrySet()) {
            heatMap.put(e.getKey(), e.getValue()[timeSegment]);
        }
        return heatMap;
    }

    /**
     * Merge the records according to time period, eg., [0,k),[k,2k],... , while 0 means time 00:00 and k
     * means the time k seconds later than 00:00.
     * Normally we set k to 30*60 seconds, or half an hour.
     *
     * @return
     */
    private Map<Long, double[]> loadAndclassify(CityMap cityMap, String[] resourceDataUrls, int windowTimeSeconds) {
        final long[] maxMinTime = new long[]{Long.MIN_VALUE, Long.MAX_VALUE};
        Map<Long, double[]> tripDemands = new HashMap<>();
        int np = 24 * 3600 / windowTimeSeconds;
        for (long iid : cityMap.intersections().keySet()) {
            tripDemands.put(iid, new double[np]);
        }
        CSVNewYorkParser parser = null;
        for (String url : resourceDataUrls) {
            parser = new CSVNewYorkParser(url, cityMap.computeZoneId());
            parser.parse((resource -> {
                double lat = resource.getPickupLat();
                double lon = resource.getDropoffLon();
                long time = resource.getTime();

                maxMinTime[0] = Math.max(maxMinTime[0], time);
                maxMinTime[1] = Math.min(maxMinTime[1], time);

                int timeSegment = getTimeSegment(time, windowTimeSeconds);
                long nearestIntersectionId = matchNearestIntersection(cityMap, lat, lon);
                tripDemands.get(nearestIntersectionId)[timeSegment] += 1;
            }));
        }

        int ndays = (int) (maxMinTime[0] - maxMinTime[1]) / (24 * 3600);

        // Compute average demand of a day
        for (double[] values : tripDemands.values()) {
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i] / ndays;
            }
        }

        return tripDemands;
    }

    private int getTimeSegment(long time, int windowTimeSeconds) {
        int np = 24 * 3600 / windowTimeSeconds;
        ZoneOffset offset = ZoneOffset.of("-04:00");
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(time, 0, offset);
        int hour = ldt.getHour(), minute = ldt.getMinute(), second = ldt.getSecond();
        return ((hour * 3600 + minute * 60 + second) / windowTimeSeconds) % np;
    }

    private long matchNearestIntersection(CityMap cityMap, double lat, double lon) {
        return cityMap.getNearestLink(lon, lat).road.from.id;
    }
}
