package org.lab1505.credit;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/28 17:17
 */
public class RealtimeNetEdge implements TraveltimeCreditEdge {
    public int numLanes;
    public double initialTraveltime;
    public double traveltime;
    public double marginalcost;
    double otherVolume;
    double taxiVolume;
    double length;

    public RealtimeNetEdge(double length, int numLanes, double initialTraveltime, double othervolume) {
        this.otherVolume = othervolume;
        this.taxiVolume = 0;
        this.initialTraveltime = initialTraveltime;
        this.traveltime = initialTraveltime;
        this.length = length;
        this.numLanes = numLanes;
        this.marginalcost = 0;
    }

    public void increTaxiVolume() {
        this.taxiVolume += 1;
    }

    public void decreaseTaxiVolume() {
        if (taxiVolume > 0) taxiVolume--;
    }

    @Override
    public double getCredit() {
        return 0;
    }

    public double getTaxiVolume() {
        return taxiVolume;
    }

    public void setTaxiVolume(double taxiVolume) {
        this.taxiVolume = taxiVolume;
    }

    @Override
    public double getTraveltime() {
        return 0;
    }

    public void setTraveltime(double traveltime) {
        this.traveltime = traveltime;
    }
}