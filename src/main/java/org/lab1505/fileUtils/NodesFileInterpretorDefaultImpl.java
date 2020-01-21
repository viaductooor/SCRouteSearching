package org.lab1505.fileUtils;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/28 17:14
 */
public class NodesFileInterpretorDefaultImpl implements NodesFileInterpretor {
    @Override
    public long getId(String[] line) {
        return Long.parseLong(line[0]);
    }

    @Override
    public double getLat(String[] line) {
        return Double.parseDouble(line[2]);
    }

    @Override
    public double getLon(String[] line) {
        return Double.parseDouble(line[1]);
    }
}
