package org.lab1505.fileUtils;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/27 14:04
 */
public interface NodesFileInterpretor {
    public default int skipHeadingLines() {
        return 1;
    }

    public long getId(String[] line);

    public double getLat(String[] line);

    public double getLon(String[] line);
}
