package org.lab1505.credit;

import org.lab1505.map.Intersection;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/29 11:18
 * <p>
 * Basic crowd-sourcing task.
 * </p>
 */
public class CSTask {
    public final long time;
    public final long expireTime;
    public final Intersection intersection;

    public CSTask(long time, long expireTime, Intersection intersection) {
        this.time = time;
        this.expireTime = expireTime;
        this.intersection = intersection;
    }
}
