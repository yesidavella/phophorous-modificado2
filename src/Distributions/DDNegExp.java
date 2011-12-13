
package Distributions;



import simbase.SimBaseSimulator;

/**
 * Discrete value negative exponential distribution number generator. Call the
 * {@link #sample()} method to generate samples.
 * 
 * @version 2.0
 */
public class DDNegExp extends DiscreteDistribution {

	/**
	 * The average
	 */
	private double avg = 0;

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

	/**
	 * Constructor.
	 */
	protected DDNegExp() {
	}

	/**
	 * Constructor. A SimBaseSimulator object is used to provide the random
	 * {@link #engine engine}.
	 * 
	 * @param sim
	 *            the SimBaseSimulator object associated with this instance.
	 *            Will use the engine from that object.
	 * @param average
	 *            the average for the negative exponential distribution
	 */
	public DDNegExp(SimBaseSimulator sim, double average) {
		super(sim);
		avg = average;
	}



	/**
	 * Constructor. No random {@link #engine engine} is set, and one should be
	 * provided manually, via the {@link #setMersenneTwister setMersenneTwister}
	 * method.
	 * 
	 * @param average
	 *            the average for the negative exponential distribution
	 */
	public DDNegExp(double average) {
		avg = average;
	}

	/**
	 * Returns a sample from the discrete negative exponential distribution.
	 * 
	 * @return a sample from the discrete negative exponential distribution
	 */
	public long sample() {
		return (long) (-avg * Math.log(engine.nextDouble()));
	}

	public double sampleDouble() {
		return -avg * Math.log(engine.nextDouble());

	}

	public String toString() {
		return this.getClass().toString().substring(11) + " - " + avg;
	}


}