package Grid.Utilities.KMeans;

import java.util.Vector;
import trs.core.Network;
import java.util.*;
import java.util.ArrayList;
import trs.core.NetworkRouting;

/**
 * This class is the entry point for constructing Cluster Analysis objects.
 * Each instance of KMeans object is associated with one or more theClusters, 
 * and a Vector of DataPoint objects. The KMeans and DataPoint classes are
 * the only classes available from other packages.
 * @see DataPoint
 **/
public class KMeans {

    private Cluster[] clusters;
    private Vector mDataPoints = new Vector();
    private double mSWCSS;
    private Network network;
    private NetworkRouting networkRouting;
    private int loops;

    public KMeans(int k, Network network, NetworkRouting routing, int loops) {
        this.networkRouting = routing;
        this.network = network;
        this.loops = loops;
        clusters = new Cluster[k];
        for (int i = 0; i < k; i++) {
            clusters[i] = new Cluster("Cluster" + i);
        }
        mDataPoints = new Vector();
        int nrOfDataPoints = network.getNodeIDs().size();
        for (int i = 0; i < nrOfDataPoints; i++) {
            mDataPoints.add(new DataPoint(network, network.getNodeIDs().get(i).toString(), networkRouting));
        }

    }

    private void calcSWCSS() {
        double temp = 0;
        for (int i = 0; i < clusters.length; i++) {
            temp = temp + clusters[i].getSumSqr();
        }
        mSWCSS = temp;
    }

    public void startAnalysis() {
        //set Starting centroid positions - Start of Step 1
        setInitialCentroids();

        initClusters();
        int loop = 0;
        while (true) {

            List oldCentroids = getCentroids(clusters);

            //Populating the theClusters
            this.loop(mDataPoints);

            //recalculate Cluster centroids - Start of Step 2
            for (int i = 0; i < clusters.length; i++) {
                clusters[i].getCentroid().calcCentroid();
            }

            //get the centroids
            List centroids = getCentroids(clusters);
            loop++;
            if (centroids.containsAll(oldCentroids) || loop == loops) {

                break;
            } else {

                continue;
            }
        }
    }

    public void initClusters() {
        ArrayList<DataPoint> remainingDataPoints = new ArrayList<DataPoint>(mDataPoints);
        while (remainingDataPoints.size() > 0) {
            DataPoint refPoint = remainingDataPoints.get(0);
            Cluster newCluster = null;
            int currentDistance = Integer.MAX_VALUE;
            for (int clusterIndex = 0; clusterIndex < clusters.length; clusterIndex++) {
                Cluster cluster = clusters[clusterIndex];
                Centroid cent = cluster.getCentroid();
                int newDistance = cent.calcDistance(refPoint.getEntity());
                if (newDistance < currentDistance) {
                    newCluster = cluster;
                    currentDistance = newDistance;
                }
            }
            newCluster.addDataPoint(refPoint);
            remainingDataPoints.get(0).setCluster(newCluster);
            remainingDataPoints.remove(0);
        }
    }

    public void loop(List<DataPoint> dataPoints) {
        List<DataPoint> remainingDataPoints = new ArrayList(dataPoints);
        while (remainingDataPoints.size() > 0) {
            Cluster newCluster = null;
            int currentDistance = Integer.MAX_VALUE;
            for (int clusterIndex = 0; clusterIndex < clusters.length; clusterIndex++) {
                Cluster cluster = clusters[clusterIndex];
                int newDistance = cluster.getCentroid().calcDistance(remainingDataPoints.get(0).getEntity());
                if (newDistance < currentDistance) {
                    newCluster = cluster;
                    currentDistance = newDistance;
                }
            }
            remainingDataPoints.get(0).getCluster().removeDataPoint(remainingDataPoints.get(0));
            newCluster.addDataPoint(remainingDataPoints.get(0));
            remainingDataPoints.get(0).setCluster(newCluster);
            remainingDataPoints.remove(0);
        }
    }

    public void setInitialCentroids() {
        //WE TAKE THE FIST N nodes form the network and use them as centroids
        List<String> nodes = network.getNodeIDs();
        for (int n = 0; n < clusters.length; n++) {
            Centroid centroid = new Centroid(nodes.get(n), network, networkRouting);
            clusters[n].setCentroid(centroid);
            centroid.setCluster(clusters[n]);
        }
    }

    public int getKValue() {
        return clusters.length;
    }

    public int getTotalDataPoints() {
        return mDataPoints.size();
    }

    public double getSWCSS() {
        return mSWCSS;
    }

    public Cluster getCluster(int pos) {
        return clusters[pos];
    }

    public List<Centroid> getCentroids(Cluster[] theClusters) {
        ArrayList centroids = new ArrayList();
        for (int i = 0; i < theClusters.length; i++) {
            centroids.add(theClusters[i].getCentroid());
        }
        return centroids;
    }

    public void printCentroids() {
        for (int i = 0; i < clusters.length; i++) {
            System.out.println(clusters[i].getCentroid());
            for (int j = 0; j < clusters[i].getDataPoints().size(); j++) {
                System.out.println(clusters[i].getDataPoints().get(j));
            }
        }
    }

    public Cluster[] getClusters() {
        return clusters;
    }

    public Vector getMDataPoints() {
        return mDataPoints;
    }
}




