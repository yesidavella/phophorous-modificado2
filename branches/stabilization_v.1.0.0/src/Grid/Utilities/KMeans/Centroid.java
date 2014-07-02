/*---------------Centroid.java-----------------*/
package Grid.Utilities.KMeans;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import trs.core.Connection;
import trs.core.Network;
import trs.core.NetworkRouting;

/**
 * This class represents the Centroid for a Cluster. 
 * @author Jens Buysse
 * @version 1.0
 * @see Cluster
 */
public class Centroid {

    private Cluster mCluster;
    private String entity;
    private Network network;
    private NetworkRouting networkRouting;

    public Centroid(String entity, Network network, NetworkRouting networkRouting) {
        this.entity = entity;
        this.network = network;
        this.networkRouting = networkRouting;

    }

    public void calcCentroid() {
        int nrOfDataPoints = mCluster.getNumDataPoints();
        Map<DataPoint, Integer> counting = new TreeMap();
        for (int i = 0; i < nrOfDataPoints; i++) {
            int totalDistance = 0;
            for (int j = 0; j < nrOfDataPoints; j++) {
                if (i != j) {
                    totalDistance += mCluster.getDataPoint(i).calcDistance(mCluster.getDataPoint(j));
                }
            }
            counting.put(mCluster.getDataPoint(i), new Integer(totalDistance));
        }
        Iterator<DataPoint> it = counting.keySet().iterator();
        DataPoint newCentroid = null;
        int newValue = Integer.MAX_VALUE;
        while (it.hasNext()) {
            DataPoint dp = it.next();
            Integer value = counting.get(dp);
            if (value.intValue() <= newValue) {
                newValue = value.intValue();
                newCentroid = dp;
            }
        }
        if (newCentroid != null) {
            Centroid centroid = new Centroid(newCentroid.getEntity(), network, networkRouting);
            mCluster.setCentroid(centroid);
            centroid.setCluster(mCluster);
        }

    }

    public void setCluster(Cluster c) {
        this.mCluster = c;
    }

    public Cluster getCluster() {
        return mCluster;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public int calcDistance(String otherEntity) {
        List connections = networkRouting.findConnections(entity, otherEntity);
        if (!connections.isEmpty()) {
            Connection conn = (Connection) connections.get(0);
            return conn.getRoute().getNumberOfEdges() - 1;
        } else {
            return -1;
        }

    }

    @Override
    public String toString() {
        return "Centroid " + entity;
    }
}

