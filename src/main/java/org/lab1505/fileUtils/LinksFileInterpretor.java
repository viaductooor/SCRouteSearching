package org.lab1505.fileUtils;

/**
 * When reading a csv file containing information of links line by line, extract crucial values from every line.
 * The interface LinksFileInterpretor defines how to extract the values from a line.
 */
public interface LinksFileInterpretor {
    public default int skipHeadingLines() {
        return 1;
    }

    public long getInitNode(String[] line);

    public long getEndNode(String[] line);

    public double getLength(String[] line);

    public int getNumLanes(String[] line);

    public double getInitTraveltime(String[] line);

    public double getOtherVolume(String[] line);
}