package org.lab1505.credit;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2020/1/10 14:36
 */
public class BasicTraveltimeCreditEdge implements TraveltimeCreditEdge {
    public double traveltime;
    public double credit;

    @Override
    public double getCredit() {
        return traveltime;
    }

    @Override
    public double getTraveltime() {
        return credit;
    }

    public BasicTraveltimeCreditEdge(double traveltime, double credit) {
        this.traveltime = traveltime;
        this.credit = credit;
    }
}
