package Distributions;

import simbase.SimBaseSimulator;

/**
 * Discrete value uniform distribution number generator. Call the
 * {@link #sample()} method to generate samples.
 * 
 * @version 2.0
 */
public class DDUniform extends DiscreteDistribution {

    /**
     * The minimum
     */
    private double min = 0;
    /**
     * The maximum
     */
    private double max = 0;

    /**
     * Constructor.
     */
    protected DDUniform() {
    }

    /**
     * Constructor. No random {@link #engine engine} is set, and one should be
     * provided manually, via the {@link #setMersenneTwister setMersenneTwister}
     * method.
     * 
     * @param min
     *            the lower bound for the uniform distribution
     * @param max
     *            the upper bound for the uniform distribution
     */
    public DDUniform(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    /**
     * Constructor. A SimBaseSimulator object is used to provide the random
     * {@link #engine engine}.
     * 
     * @param sim
     *            the SimBaseSimulator object associated with this instance.
     *            Will use the engine from that object.
     * @param min
     *            the lower bound for the uniform distribution
     * @param max
     *            the upper bound for the uniform distribution
     */
    public DDUniform(SimBaseSimulator sim, long min, long max) {
        super(sim);
        this.min = min;
        this.max = max;
    }

    /**
     * Returns a sample from the discrete uniform distribution.
     * 
     * @return a sample from the discrete uniform distribution
     */
    public long sample() {
        return (long) ((max - min) * (engine.nextDouble()) + min);
    }

    public double sampleDouble() {
        return (max - min) * engine.nextDouble() + min;
    }

    public String toString() {
        return this.getClass().toString().substring(11) + " - " + min + " - " + max;
    }
}
