package org.lab1505.fileUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import net.iakovlev.timeshape.TimeZoneEngine;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Jie Wu
 * @date 2020/1/18 13:15
 */
public class DataPool {
    public static List<LatLonTime> readTaxiData(String[] urls) {
        ZoneId zoneId = getZoneId(urls[0]);
        List<LatLonTime> entries = new LinkedList<>();
        for (String url : urls) {
            CSVNewYorkParser parser = new CSVNewYorkParser(url, zoneId);
            parser.parse((resource -> {
                double pickupLon = resource.getPickupLon();
                double pickupLat = resource.getPickupLat();
                long time = resource.getTime();
                entries.add(new LatLonTime(pickupLat, pickupLon, time));
            }));
        }
        return entries;
    }

    private static ZoneId getZoneId(String url) {
        TimeZoneEngine engine = TimeZoneEngine.initialize();
        double rndLatitude = 0;
        double rndLongitude = 0;
        try (CSVReader reader = new CSVReader(new FileReader(url))) {
            reader.skip(1);
            String[] line = reader.readNext();
            rndLatitude = Double.parseDouble(line[6]);
            rndLongitude = Double.parseDouble(line[5]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ZoneId zoneId = engine.query(rndLatitude, rndLongitude).get();
        return zoneId;
    }

    public static void uniform(List<LatLonTime> list, Consumer<double[]> consumer) {
        double maxLat = Double.MIN_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE;
        double minLon = Double.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        long minTime = Long.MAX_VALUE;
        for (LatLonTime e : list) {
            maxLat = Math.max(maxLat, e.lat);
            minLat = Math.min(minLat, e.lat);
            maxLon = Math.max(maxLon, e.lon);
            minLon = Math.min(minLon, e.lon);
            maxTime = Math.max(maxTime, e.time % 86400);
            minTime = Math.min(minTime, e.time % 86400);
        }
        double latBase = maxLat - minLat;
        double lonBase = maxLon - minLon;
        double timeBase = maxTime - minTime;

        for (LatLonTime e : list) {
            consumer.accept(new double[]{
                    (e.lat - minLat) / latBase,
                    (e.lon - minLon) / lonBase,
                    (e.time % 86400 - minTime) / timeBase
            });
        }
    }

    public static List<double[]> uniform(List<LatLonTime> list) {
        List<double[]> uniformed = new LinkedList<>();
        uniform(list, (e) -> {
            uniformed.add(e);
        });
        return uniformed;
    }

    public static void main(String[] args) {
        String url = "files/yellow_tripdata_2016-06.csv";
        List<LatLonTime> latLonTimes = readTaxiData(new String[]{url});
        try (CSVWriter writer = new CSVWriter(new FileWriter("output/uniform_lat_lon_time.csv"))) {
            writer.writeNext(new String[]{"lat", "lon", "time"});
            uniform(latLonTimes, (e) -> {
                writer.writeNext(new String[]{
                        String.valueOf(e[0]),
                        String.valueOf(e[1]),
                        String.valueOf(e[2])
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class LatLonTime {
        public final double lat;
        public final double lon;
        public final long time;

        public LatLonTime(double lat, double lon, long time) {
            this.lat = lat;
            this.lon = lon;
            this.time = time;
        }
    }
}
