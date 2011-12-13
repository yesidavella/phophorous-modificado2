
package Distributions;



import simbase.SimBaseSimulator;

/**
 * Discrete value hyper exponential distribution number generator. Call the
 * {@link #sample()} or {@link #sampleDouble()} method to generate samples.
 * 
 * @version 2.0
 */
public class DDHyperExp extends DiscreteDistribution {

	/**
	 * The desired precision
	 */
	public static double PRECISION = 1e-10;

	/**
	 * The mean values for the different phases
	 */
	private double[] lambdas;

	/**
	 * The chances for the different phases
	 */
	private double[] chances;

	/**
	 * Constructor.
	 */
	protected DDHyperExp() {
	}



	/**
	 * Constructor. A SimBaseSimulator object is used to provide the random
	 * {@link #engine engine}.
	 * 
	 * @param sim
	 *            the SimBaseSimulator object associated with this instance.
	 *            Will use the engine from that object.
	 * @param lambdas
	 *            the averages for the different phases of the hyper exponential
	 *            distribution
	 * @param chances
	 *            the chances for the different phases of the hyper exponential
	 *            distribution
	 */
	public DDHyperExp(SimBaseSimulator sim, double[] lambdas, double[] chances) {
		super(sim);
		this.lambdas = lambdas;
		this.chances = chances;
		if (chances.length != lambdas.length)
			throw new IllegalStateException(
					"Array lengths are not the same !!!");
	}

    public double[] getChances() {
        return chances;
    }

    public void setChances(double[] chances) {
        this.chances = chances;
    }

    public double[] getLambdas() {
        return lambdas;
    }

    public void setLambdas(double[] lambdas) {
        this.lambdas = lambdas;
    }

	/**
	 * Searches an interval in which the desired value is situated
	 * 
	 * @param p
	 *            the chance we want to invert
	 * @param low
	 *            the lower bound
	 * @param high
	 *            the upper bound
	 * @return the inverse of the given chance p
	 */
        
	private double calculate(double p, double low, double high) {
		double max = 0;
		for (int i = 0; i < lambdas.length; i++) {
			max += chances[i] * Math.exp(-high / lambdas[i]);
		}
		if (Math.abs(p - max) < PRECISION)
			return high;
		else if (p > max)
			return calculate2(p, low, high);
		else
			return calculate(p, high, high * 10);
	}

	/**
	 * Calculates the inverse of p in the given interval
	 * 
	 * @param p
	 *            the chance to invert
	 * @param low
	 *            the lower bound
	 * @param high
	 *            the upper bound
	 * @return the inverse of the given chance p
	 */
	private double calculate2(double p, double low, double high) {
		if (low <= 0)
			low = Double.MIN_VALUE;
		if (Math.abs(low - high) < PRECISION)
			return low;
		double mid = Math.exp((Math.log(high) - Math.log(low)) / 2
				+ Math.log(low)), mean = 0;
		for (int i = 0; i < lambdas.length; i++)
			mean += chances[i] * Math.exp(-mid / lambdas[i]);
		if (Math.abs(p - mean) < PRECISION)
			return mid;
		else if (p < mean)
			return calculate2(p, mid, high);
		else
			return calculate2(p, low, mid);

	}

	/**
	 * Constructor. No random {@link #engine engine} is set, and one should be
	 * provided manually, via the {@link #setMersenneTwister setMersenneTwister}
	 * method.
	 * 
	 * @param lambdas
	 *            the averages for the negative exponential distributions
	 * @param chances
	 *            the chances for the negative exponential distributions
	 */
	public DDHyperExp(double[] lambdas, double[] chances) {
		this.lambdas = lambdas;
		this.chances = chances;
	}

	/**
	 * Returns a sample from the discrete hyper exponential distribution.
	 * 
	 * @return a sample from the discrete hyper exponential distribution
	 */
	public long sample() {
		return (long) calculate(engine.nextDouble(), Double.MIN_VALUE, 0.01);
	}

	/**
	 * Returns a sample from the discrete hyper exponential distribution.
	 * 
	 * @return a sample from the discrete hyper exponential distribution
	 */
	public double sampleDouble() {
		double val = engine.nextDouble();
		return calculate(val, Double.MIN_VALUE, 0.01);
	}

	public String toString() {
		return this.getClass().toString().substring(11) + " - " + lambdas[0];
	}

}