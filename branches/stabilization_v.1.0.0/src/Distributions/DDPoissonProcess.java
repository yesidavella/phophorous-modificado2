package Distributions;

import cern.jet.random.Poisson;
import cern.jet.random.engine.MersenneTwister;



import simbase.SimBaseSimulator;

/**
 * Discrete value Poisson process distribution number generator. Call the
 * {@link #sample()} method to generate samples.
 * 
 */
public class DDPoissonProcess extends DiscreteDistribution {

    /**
     * The poisson generator
     */
    private Poisson poisson = null;
    /**
     * The average
     */
    private double average = 0;

    /**
     * Constructor.
     */
    protected DDPoissonProcess() {
    }

    /**
     * Constructor. A SimBaseSimulator object is used to provide the random
     * {@link #engine engine}.
     * 
     * @param sim
     *            the SimBaseSimulator object associated with this instance.
     *            Will use the engine from that object.
     * @param average
     *            the average for the poisson process distribution
     */
    public DDPoissonProcess(SimBaseSimulator sim, double average) {

        super(sim);
        this.average = average;
        poisson = new Poisson(average, engine);
    }

    public void setMersenneTwister(MersenneTwister twister) {
        super.setMersenneTwister(twister);
        poisson = new Poisson(average, engine);
    }

    /**
     * Constructor. No random {@link #engine} is set, and one should be provided
     * manually, via the {@link #setMersenneTwister setMersenneTwister} method.
     * 
     * @param d
     *            the average for the poisson process distribution
     */
    public DDPoissonProcess(double d) {
        this.average = d;
        poisson = new Poisson(d, engine);
    }

    /**
     * Returns a sample from the discrete poisson process distribution.
     * 
     * @return a sample from the discrete poisson process distribution
     */
    public long sample() {
        return poisson.nextInt();
    }

    public double sampleDouble() {
        return poisson.nextDouble();
    }

    public String toString() {
        return this.getClass().toString().substring(11) + " - " + average;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public Poisson getPoisson() {
        return poisson;
    }

    public void setPoisson(Poisson poisson) {
        this.poisson = poisson;
    }
    
}
