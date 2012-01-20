package Distributions;

import simbase.SimBaseSimulator;

/**
 * Discrete value normal distribution number generator. Call the
 * {@link #sample()} method to generate samples.
 * 
 * @version 2.0
 */
public class DDNormal extends DiscreteDistribution {

    /**
     * The average
     */
    private double avg = 0;
    /**
     * The deviation
     */
    private double dev = 0;

    /**
     * Constructor.
     */
    protected DDNormal() {
    }

    /**
     * Constructor. No random {@link #engine engine} is set, and one should be
     * provided manually, via the {@link #setMersenneTwister setMersenneTwister}
     * method.
     * 
     * @param average
     *            the average for the normal distribution
     * @param stddev
     *            the standard deviation of the normal distribution
     */
    public DDNormal(double average, double stddev) {
        avg = average;
        dev = stddev;
    }

    /**
     * Constructor. A SimBaseSimulator object is used to provide the random
     * {@link #engine engine}.
     * 
     * @param sim
     *            the SimBaseSimulator object associated with this instance.
     *            Will use the engine from that object.
     * @param average
     *            the average for the normal distribution
     * @param stddev
     *            the standard deviation of the normal distribution
     */
    public DDNormal(SimBaseSimulator sim, long average, long stddev) {
        super(sim);
        avg = average;
        dev = stddev;
    }

    /**
     * Returns a sample from the discrete normal distribution.
     * 
     * @return a sample from the discrete normal distribution
     */
    public long sample() {
        return (long) (avg + dev * (Math.sqrt(-2 * Math.log(engine.nextDouble())) * Math.sin(2 * Math.PI * engine.nextDouble())));
    }

    public double sampleDouble() {
        return avg + dev * Math.sqrt(-2 * Math.log(engine.nextDouble())) * Math.sin(2 * Math.PI * engine.nextDouble());
    }

    public String toString() {
        return this.getClass().toString().substring(11) + " - " + avg + " - " + dev;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public double getDev() {
        return dev;
    }

    public void setDev(double dev) {
        this.dev = dev;
    }
    
}
