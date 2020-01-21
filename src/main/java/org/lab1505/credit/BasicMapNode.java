package org.lab1505.credit;

import java.util.Map;
import java.util.Objects;

/**
 * @author Jie Wu
 * mail wuj1e@cqu.edu.cn
 * @date 2019/12/27 13:47
 */
public class BasicMapNode {
    // Unique identification of a node
    public final long id;

    // Latitude and longitude
    public final double lat,lon;

    // Other information such as name, type etc.
    public Map<String,String> otherInfo;

    public BasicMapNode(long id, double lat, double lon){
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicMapNode)) return false;
        BasicMapNode that = (BasicMapNode) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BasicMapNode{" +
                "id=" + id +
                ", lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}
