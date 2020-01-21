package org.lab1505.credit;

import org.lab1505.map.LocationOnRoad;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/29 11:18
 *
 * Basic crowd-sourcing worker.
 */
public class CSWorker {
    public LocationOnRoad locationOnRoad;
    public final long id;

    public CSWorker(LocationOnRoad locationOnRoad, long id) {
        this.locationOnRoad = locationOnRoad;
        this.id = id;
    }
}
