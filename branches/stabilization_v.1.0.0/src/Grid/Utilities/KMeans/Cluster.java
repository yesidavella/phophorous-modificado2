/*-----------------Cluster.java----------------*/
package Grid.Utilities.KMeans;

import java.util.*;
import java.util.ArrayList;

/**
 * This class represents a Cluster in a Cluster Analysis Instance. A Cluster is associated
 * with one and only one KMeans Instance. A Cluster is related to more than one DataPoints.
 * @author Jens Buysse
 * @version 1.1
 * @see DataPoint
 * @see Centroid
 */
class Cluster {

    private String mName;
    private Centroid mCentroid;
    private double mSumSqr;
    private ArrayList mDataPoints;

    public Cluster(String name) {
        this.mName = name;
        this.mCentroid = null; //will be set by calling setCentroid()
        mDataPoints = new ArrayList();
    }

    public void setCentroid(Centroid c) {
        mCentroid = c;
    }

    public Centroid getCentroid() {
        return mCentroid;
    }

    public void addDataPoint(DataPoint dp) { //called from CAInstance
        dp.setCluster(this);
        this.mDataPoints.add(dp);
        calcSumOfSquares();
    }

    public void removeDataPoint(DataPoint  dp) {
        this.mDataPoints.remove(dp);
        calcSumOfSquares();
    }

    public int getNumDataPoints() {
        return this.mDataPoints.size();
    }

    public DataPoint getDataPoint(int pos) {
        return (DataPoint) this.mDataPoints.get(pos);
    }

    public void calcSumOfSquares() { //called from Centroid
        int size = this.mDataPoints.size();
        double temp = 0;
        for (int i = 0; i < size; i++) {
            temp = temp + ((DataPoint) this.mDataPoints.get(i)).getCurrentDistance();
        }
        this.mSumSqr = temp;
    }

    public double getSumSqr() {
        return this.mSumSqr;
    }

    public String getName() {
        return this.mName;
    }

    public  ArrayList getDataPoints() {
        return this.mDataPoints;
    }
}
