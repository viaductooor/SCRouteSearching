package org.lab1505.fileUtils;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/28 17:13
 */
public class LinksFileInterpretorDefaultImpl implements LinksFileInterpretor {
    @Override
    public long getInitNode(String[] line) {
        return Long.parseLong(line[1]);
    }

    @Override
    public long getEndNode(String[] line) {
        return Long.parseLong(line[2]);
    }

    @Override
    public double getLength(String[] line) {
        return Double.parseDouble(line[6]);
    }

    @Override
    public int getNumLanes(String[] line) {
        return Integer.parseInt(line[8]);
    }

    @Override
    public double getInitTraveltime(String[] line) {
        return Double.parseDouble(line[4]);
    }

    @Override
    public double getOtherVolume(String[] line) {
        return Double.parseDouble(line[10]) * getNumLanes(line);
    }
}
