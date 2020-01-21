package org.lab1505.credit;

import com.opencsv.CSVReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.lab1505.fileUtils.LinksFileInterpretor;
import org.lab1505.fileUtils.NodesFileInterpretor;
import org.lab1505.map.CityMap;
import org.lab1505.map.Intersection;
import org.lab1505.map.Link;
import org.lab1505.map.Road;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/25 23:12
 *
 * <p>
 * MapGraphConvertor can match {@link RealtimeNetEdge} graph
 * with existing CityMap. It is assured that they have as many edges as the others, which is to say
 * any edge of the same startNodeId and endNodeId can be found in both CityMap and the graph.
 * </p>
 */
public class MapGraphConverter {
    private static CityMap cityMap;
    private static final Log logger = LogFactory.getLog(MapGraphConverter.class);

    public MapGraphConverter(CityMap cityMap) {
        this.cityMap = cityMap;
        checkCityMap();
    }

    /**
     * Print crucial statistics of CityMap to logger.
     * Nothing to do with the system.
     */
    public void checkCityMap(){
        int numIntersections = cityMap.intersections().size();
        int numRoads = cityMap.roads().size();
        int numLoops = 0;
        Set<Long> vertexSet = new HashSet<>();
        for(Road road:cityMap.roads()){
            for(Link l:road.links){
                vertexSet.add(l.from.id);
                vertexSet.add(l.to.id);
            }
            if(road.from.id==road.to.id){
                numLoops += 1;
            }
        }
        logger.info("# of intersections: "+numIntersections);
        logger.info("# of roads: "+numRoads);
        logger.info("# of vertexes: "+vertexSet.size());
        logger.info("# of loops: "+numLoops);

    }

    /**
     * The matching work is based on CityMap.
     *
     * @param net
     * @return
     */
    public SimpleDirectedWeightedGraph<BasicMapNode, RealtimeNetEdge> matchLinks(
            final SimpleDirectedWeightedGraph<BasicMapNode, RealtimeNetEdge> net) {
        SimpleDirectedWeightedGraph<BasicMapNode, RealtimeNetEdge> matchedMap =
                new SimpleDirectedWeightedGraph<>(RealtimeNetEdge.class);
        Map<Long, BasicMapNode> idNodeMap = new HashMap<>();
        for (Road road : cityMap.roads()) {
            Intersection from = road.from;
            Intersection to = road.to;
            BasicMapNode fromNode = new BasicMapNode(from.id, from.latitude, from.longitude);
            BasicMapNode toNode = new BasicMapNode(to.id, to.latitude, to.longitude);
            RealtimeNetEdge defaultEdge = new RealtimeNetEdge(road.length, 1, road.travelTime, 0);
            matchedMap.addVertex(fromNode);
            matchedMap.addVertex(toNode);
            if (!fromNode.equals(toNode)) {
                matchedMap.addEdge(fromNode, toNode, defaultEdge);
                idNodeMap.put(from.id, fromNode);
                idNodeMap.put(to.id, toNode);
            }
        }

        // Create a hash map of vertices and the roads to which they belong.
        Map<Long, List<Road>> m = new HashMap<>();
        for (Road road : cityMap.roads()) {
            Set<Long> vertexSet = getVertexIds(road);
            for (long vertexId : vertexSet) {
                if (m.containsKey(vertexId)) {
                    m.get(vertexId).add(road);
                } else {
                    m.put(vertexId, new LinkedList<Road>() {
                        {
                            add(road);
                        }
                    });
                }
            }
        }

        int matchedRoads = 0;
        int matchedLinks = 0;
        int totalRoads = cityMap.roads().size();
        int totalLinks = net.edgeSet().size();

        HashSet<Road> matchedRoadSet = new HashSet<>();

        for (RealtimeNetEdge e : net.edgeSet()) {
            long edgeSourceId = net.getEdgeSource(e).id;
            long edgeTargetId = net.getEdgeTarget(e).id;

            // If the road contains both edgeSourceId and edgeTargetId, see the road as the link.
            if (m.containsKey(edgeSourceId)) {
                for (Road road : m.get(edgeSourceId)) {
                    if (getVertexIds(road).contains(edgeTargetId)) {
                        matchedLinks++;
                        matchedRoadSet.add(road);
                        RealtimeNetEdge rte = matchedMap.getEdge(idNodeMap.get(road.from.id), idNodeMap.get(road.to.id));
                        rte.initialTraveltime = e.initialTraveltime;
                        rte.otherVolume = e.otherVolume;
                        rte.numLanes = e.numLanes;
                        break;
                    }
                }
            }
        }

        matchedRoads = matchedRoadSet.size();
        logger.info(matchedRoads + "/" + totalRoads + " of roads matched.");
        logger.info(matchedLinks + "/" + totalLinks + " of links matched.");

        return matchedMap;
    }

    private Set<Long> getVertexIds(Road road) {
        Set<Long> vertexSet = new HashSet<>();
        for (Link link : road.links) {
            vertexSet.add(link.from.id);
            vertexSet.add(link.to.id);
        }
        return vertexSet;
    }

    public SimpleDirectedGraph<BasicMapNode, RealtimeNetEdge> readLinksWithoutMathing(
            String linksUrl, String nodesUrl, LinksFileInterpretor lfi, NodesFileInterpretor nfi){
        SimpleDirectedGraph<BasicMapNode,RealtimeNetEdge> linksMap = new SimpleDirectedGraph<>(RealtimeNetEdge.class);
        Map<Long, double[]> nodesMap = new HashMap<>();
        try (CSVReader nodesReader = new CSVReader(new FileReader(nodesUrl));
             CSVReader linksReader = new CSVReader(new FileReader(linksUrl))) {
            for (int i = 0; i < nfi.skipHeadingLines(); i++) {
                nodesReader.readNext();
            }
            String[] line = null;
            while ((line = nodesReader.readNext()) != null) {
                long id = nfi.getId(line);
                double lat = nfi.getLat(line);
                double lon = nfi.getLon(line);
                nodesMap.put(id, new double[]{lat, lon});
            }
            for (int i = 0; i < lfi.skipHeadingLines(); i++) {
                linksReader.readNext();
            }
            while ((line = linksReader.readNext()) != null) {
                long startNodeId = lfi.getInitNode(line);
                long endNodeId = lfi.getEndNode(line);
                if (nodesMap.containsKey(startNodeId) && nodesMap.containsKey(endNodeId) && startNodeId != endNodeId) {
                    double length = lfi.getLength(line);
                    int numLanes = lfi.getNumLanes(line);
                    double initTraveltime = lfi.getInitTraveltime(line);
                    double otherVolume = lfi.getOtherVolume(line);
                    RealtimeNetEdge edge = new RealtimeNetEdge(length, numLanes, initTraveltime, otherVolume);
                    double traveltime = computeTraveltime(otherVolume,0,numLanes,length);
                    edge.setTraveltime(traveltime);
                    BasicMapNode startNode = new BasicMapNode(startNodeId, nodesMap.get(startNodeId)[0], nodesMap.get(startNodeId)[1]);
                    BasicMapNode endNode = new BasicMapNode(endNodeId, nodesMap.get(endNodeId)[0], nodesMap.get(endNodeId)[1]);
                    linksMap.addVertex(startNode);
                    linksMap.addVertex(endNode);
                    linksMap.addEdge(startNode, endNode, edge);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return linksMap;
    }

    public SimpleDirectedWeightedGraph<BasicMapNode, RealtimeNetEdge> readAndMatchLinks(
            String linksUrl, String nodesUrl, LinksFileInterpretor lfi, NodesFileInterpretor nfi) {
        Map<Long, double[]> nodesMap = new HashMap<>();
        SimpleDirectedWeightedGraph<BasicMapNode, RealtimeNetEdge> linksMap =
                new SimpleDirectedWeightedGraph<>(RealtimeNetEdge.class);
        try (CSVReader nodesReader = new CSVReader(new FileReader(nodesUrl));
             CSVReader linksReader = new CSVReader(new FileReader(linksUrl))) {
            int numLinksMatchedWithNodes = 0;
            int numLinks = 0;
            for (int i = 0; i < nfi.skipHeadingLines(); i++) {
                nodesReader.readNext();
            }
            String[] line = null;
            while ((line = nodesReader.readNext()) != null) {
                long id = nfi.getId(line);
                double lat = nfi.getLat(line);
                double lon = nfi.getLon(line);
                nodesMap.put(id, new double[]{lat, lon});
            }
            for (int i = 0; i < lfi.skipHeadingLines(); i++) {
                linksReader.readNext();
            }
            while ((line = linksReader.readNext()) != null) {
                numLinks++;
                long startNodeId = lfi.getInitNode(line);
                long endNodeId = lfi.getEndNode(line);
                if (nodesMap.containsKey(startNodeId) && nodesMap.containsKey(endNodeId) && startNodeId != endNodeId) {
                    double length = lfi.getLength(line);
                    int numLanes = lfi.getNumLanes(line);
                    double initTraveltime = lfi.getInitTraveltime(line);
                    double otherVolume = lfi.getOtherVolume(line);
                    RealtimeNetEdge edge = new RealtimeNetEdge(length, numLanes, initTraveltime, otherVolume);
                    BasicMapNode startNode = new BasicMapNode(startNodeId, nodesMap.get(startNodeId)[0], nodesMap.get(startNodeId)[1]);
                    BasicMapNode endNode = new BasicMapNode(endNodeId, nodesMap.get(endNodeId)[0], nodesMap.get(endNodeId)[1]);
                    linksMap.addVertex(startNode);
                    linksMap.addVertex(endNode);
                    linksMap.addEdge(startNode, endNode, edge);
                    numLinksMatchedWithNodes++;
                }
            }
            logger.info(numLinksMatchedWithNodes + "/" + numLinks + " of links matched with nodes.");
        } catch (IOException e) {
            logger.error("Error during reading links and nodes.");
            logger.error(e.getMessage());
        }
        SimpleDirectedWeightedGraph<BasicMapNode, RealtimeNetEdge> result = matchLinks(linksMap);
        return result;
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
}
