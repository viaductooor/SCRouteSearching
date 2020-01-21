package org.lab1505;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.lab1505.credit.*;
import org.lab1505.fileUtils.*;
import org.lab1505.map.CityMap;
import org.lab1505.map.MapCreator;
import org.lab1505.map.MapWithData;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2020/1/16 11:25
 */
public class SCRouteSearching {
    private long searchRange;
    private CityMap cityMap;
    private String nodesUrl;
    private String mapJsonUrl;
    private String mapBoundaryUrl;
    private String linksUrl;
    private String histiricalModelFile;
    private double speedReduction;
    private int agentPlacementRandomSeed;
    private int totalAgents;
    private SimpleDirectedGraph<BasicMapNode, RealtimeNetEdge> realtimeNet;
    private HistoricalModel hmodel;
    private long time;

    public void configure() {
        // length of time for searching,
        searchRange = 60;

        // node file
        nodesUrl = "files/nodes.csv";

        // openstreetmap json file
        mapJsonUrl = "files/manhattan-map.json";

        // kml boundary file
        mapBoundaryUrl = "files/manhattan-boundary.kml";

        // link file, for loading link states of the map
        linksUrl = "files/2010.10.5 5am links.csv";

        // contains task possibility of each spatiotemporal segment
        histiricalModelFile = "files/2016-6-1800.txt";

        // speed = maximum_speed/speedReduction
        speedReduction = 4;

        // random seed for placing agents randomly on the map
        agentPlacementRandomSeed = 31;

        // number of agents
        totalAgents = 1000;

        // time when start searching, utc, in seconds
        time = 1464782404l; // 8:00 am
    }

    private void init() {
        MapCreator creator = new MapCreator(mapJsonUrl, mapBoundaryUrl, speedReduction);
        creator.createMap();
        cityMap = creator.outputCityMap();
        this.hmodel = HistoricalModel.fromFile(histiricalModelFile);
        this.realtimeNet = new SimpleDirectedGraph<>(RealtimeNetEdge.class);
        MapGraphConverter converter = new MapGraphConverter(cityMap);
        NodesFileInterpretor nfi = new NodesFileInterpretorDefaultImpl();
        LinksFileInterpretor lfi = new LinksFileInterpretorDefaultImpl();
        realtimeNet = converter.readLinksWithoutMathing(linksUrl, nodesUrl, lfi, nfi);
    }

    private void run() {
        configure();
        init();
        MapWithData mwd = new MapWithData(cityMap);
        SimpleDirectedGraph<Long,RealtimeNetEdge> m2 = new SimpleDirectedGraph<>(RealtimeNetEdge.class);
        for(RealtimeNetEdge e:realtimeNet.edgeSet()){
            BasicMapNode edgeSource = realtimeNet.getEdgeSource(e);
            BasicMapNode edgeTarget = realtimeNet.getEdgeTarget(e);
            m2.addVertex(edgeSource.id);
            m2.addVertex(edgeTarget.id);
            m2.addEdge(edgeSource.id,edgeTarget.id,e);
        }
        ArrayList<Long> agentLocations = mwd.placeAgentsRandomly(m2,totalAgents, agentPlacementRandomSeed);
        Map<Long, Double> heatMap = hmodel.getHeatMap(time);
        SearchAlgorithm.game(realtimeNet, agentLocations, heatMap, searchRange, 5, (graph, iteration) -> {
            CsvGraphWriter.write(graph, SearchAlgorithm.Marginal.class, new File("output/credit_output_" + iteration + ".csv"));
        });
        SearchAlgorithm.random(realtimeNet, agentLocations, searchRange, (graph) -> {
            CsvGraphWriter.write(graph, SearchAlgorithm.Marginal.class, new File("output/random_output.csv"));
        });
    }
    public static void main(String[] args){
        SCRouteSearching css = new SCRouteSearching();
        css.run();
    }
}
