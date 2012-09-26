/*----------------DataPoint.java----------------*/
package Grid.Utilities.KMeans;

import java.util.List;
import trs.core.Connection;
import trs.core.Network;
import trs.core.NetworkRouting;

/**
* This class represents a candidate for Cluster analysis. A Vector of  Data Point object
* is fed into the constructor of the KMeans class. Kmeans and DataPoint are the only
* classes which may be available from other packages.
* @author Jens Buysse
* @version 1.0
* @see KMeans
* @see Cluster
 */
public class DataPoint implements Comparable {

    private Cluster mCluster;
    private double mEuDt;
    private Network network;
    private String entity;
    private NetworkRouting networkRouting;

    public DataPoint(Network network, String entity, NetworkRouting networkRouting) {
        this.entity = entity;
        this.mCluster = null;
        this.network = network;
        this.networkRouting = networkRouting;
    }

    public void setCluster(Cluster cluster) {
        this.mCluster = cluster;
        calcDistance();
    }

    public void calcDistance() {
        if (!entity.equals(mCluster.getCentroid().getEntity())) {
            List<Connection> connections = networkRouting.findConnections(entity, mCluster.getCentroid().getEntity());
            if (!connections.isEmpty()) {
                Connection conn = connections.get(0);
                mEuDt = conn.getRoute().getEdgeIDs().size() - 1;
            } else {
                throw new IllegalStateException("No connection found between " + this.getEntity() + " and " + mCluster.getCentroid().getEntity());
            }
        }


    }

    public int calcDistance(DataPoint dp) {
        List<Connection> connections = networkRouting.findConnections(entity, dp.getEntity());
        if (!connections.isEmpty()) {
            Connection conn = connections.get(0);
            return conn.getRoute().getEdgeIDs().size() - 1;
        } else {
            throw new IllegalStateException("No connection found between " + this.getEntity() + " and " + dp.getEntity());
        }
    }

    public Cluster getCluster() {
        return mCluster;
    }

    public double getCurrentDistance() {
        return mEuDt;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public String toString() {
        return " Datapoint : " + entity;
    }

    public int compareTo(Object o) {
        return entity.compareTo(((DataPoint) o).getEntity());
    }
}

