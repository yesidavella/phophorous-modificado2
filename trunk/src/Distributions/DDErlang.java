package Distributions;

import simbase.SimBaseSimulator;

/**
 * Discrete value Erlang distribution number generator. Call the
 * {@link #sample()} method to generate samples.
 * 
 * @version 2.0
 */
public class DDErlang extends DiscreteDistribution {

    /**
     * The average
     */
    private double avg = 0;
    /**
     * The order of the distribution
     */
    private int n = 0;

    /**
     * Constructor.
     */
    protected DDErlang() {
    }

    /**
     * Constructor. A SimBaseSimulator object is used to provide the random
     * {@link #engine engine}.
     * 
     * @param sim
     *            the SimBaseSimulator object associated with this instance.
     *            Will use the engine from that object.
     * @param average
     *            the average for the Erlang distribution
     * @param order
     *            the order of the Erlang distribution
     */
    public DDErlang(SimBaseSimulator sim, double average, int order) {
        super(sim);
        this.avg = average;
        this.n = order;
    }

    public double getAvg() {
        return avg;
    }

    public int getN() {
        return n;
    }

    /**
     * Constructor. No random {@link #engine engine} is set, and one should be
     * provided manually, via the {@link #setMersenneTwister setMersenneTwister}
     * method.
     * 
     * @param average
     *            the average for the Erlang distribution
     * @param order
     *            the order of the Erlang distribution
     */
    public DDErlang(double average, int order) {
        this.avg = average;
        this.n = order;
    }

    /**
     * Returns a sample from the discrete Erlang distribution.
     * 
     * @return a sample from the discrete Erlang distribution
     */
    public long sample() {
        double K = 0;
        for (int i = 0; i < n; i++) {
            K += engine.nextDouble();
        }
        return (long) (-avg * Math.log(K));
    }

    public double sampleDouble() {
        double K = 0;
        for (int i = 0; i < n; i++) {
            K += engine.nextDouble();
        }
        return -avg * Math.log(K);
    }

    public String toString() {
        return this.getClass().toString().substring(11) + " - " + n + " - " + avg;
    }
}
