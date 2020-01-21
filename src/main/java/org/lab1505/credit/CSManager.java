package org.lab1505.credit;

import org.lab1505.fordFulkerson.FlowEdge;
import org.lab1505.fordFulkerson.FlowNetwork;
import org.lab1505.fordFulkerson.FordFulkerson;
import org.lab1505.map.CityMap;
import org.lab1505.map.Intersection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/29 11:18
 *
 * <p>
 * CSManager is the central role to run crowd-sourcing assignment.
 * </p>
 */
public class CSManager {
    public static List<CSInstance> runCsAssignment(CityMap cityMap, long windowTimeSeconds, List<? extends CSTask> tasks, List<? extends CSWorker> workers) {
        /**
         * Vertex[0:workers.size()]: workers
         * Vertex[workers.size():]: tasks
         * Vertex[workers.size()+tasks.size()]: source
         * Vertex[workers.size()+tasks.size()+1]: target
         */
        Map<Integer, Intersection> locationMap = new HashMap<Integer, Intersection>() {
            {
                for (int i = 0; i < workers.size(); i++)
                    put(i, workers.get(i).locationOnRoad.road.to);
                for (int i = 0; i < tasks.size(); i++)
                    put(i + workers.size(), tasks.get(i).intersection);
            }
        };

        Map<Integer, CSWorker> workerMap = new HashMap<Integer, CSWorker>() {
            {
                for (int i = 0; i < workers.size(); i++)
                    put(i, workers.get(i));
            }
        };

        Map<Integer, CSTask> taskMap = new HashMap<Integer, CSTask>() {
            {
                for (int i = 0; i < tasks.size(); i++)
                    put(i + workers.size(), tasks.get(i));
            }
        };

        int v = tasks.size() + workers.size() + 2;
        FlowNetwork flowNetwork = new FlowNetwork(v);

                int source = v - 2;
                int target = v - 1;

                // From source to workers
                for (int i = 0; i < workers.size(); i++) {
                    flowNetwork.addEdge(new FlowEdge(source, i, 1, 0));
                }

                // From workers to tasks
                for (int i = 0; i < workers.size(); i++) {
                    for (int j = workers.size(); j < source; j++) {
                Intersection l1 = locationMap.get(i);
                Intersection l2 = locationMap.get(j);
                long traveltime = cityMap.travelTimeBetween(l1, l2);
                if (traveltime <= windowTimeSeconds) {
                    flowNetwork.addEdge(new FlowEdge(i, j, 1, 0));
                }
            }
        }

        // From tasks to target
        for (int i = workers.size(); i < source; i++) {
            flowNetwork.addEdge(new FlowEdge(i, target, 1, 0));
        }

        // Run Ford Fulkerson Algorithm to calculate maximum flow
        FordFulkerson fk = new FordFulkerson(flowNetwork, source, target);
        List<CSInstance> instances = new LinkedList<>();
        for (int i = 0; i < workers.size(); i++) {
            for (FlowEdge fe : flowNetwork.adj(i)) {
                if (fe.flow() > 0&&fe.to()<source&&fe.from()<source) {
                    instances.add(new CSInstance(workerMap.get(i), taskMap.get(fe.to())));
                }
            }
        }
        return instances;
    }
}
