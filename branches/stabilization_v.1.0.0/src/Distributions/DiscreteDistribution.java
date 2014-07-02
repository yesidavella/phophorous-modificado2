

package Distributions;

import simbase.SimBaseSimulator;
import cern.jet.random.engine.MersenneTwister;
import java.io.Serializable;

/**
 * This is the base class for discrete distributions
 * 
 * @version 2.0
 */
public abstract class DiscreteDistribution implements Serializable{

	/**
	 * The random number generator engine. Used instead of Java's default one,
	 * for better performance.
	 */
	protected MersenneTwister engine = null;

	/**
	 * Constructor. A SimBaseSimulator object is used to provide the random
	 * {@link #engine engine}.
	 * 
	 * @param sim
	 *            the SimBaseSimulator object associated with this instance.
	 *            Will use the engine from that object.
	 */
	public DiscreteDistribution(SimBaseSimulator sim) {
		engine = sim.getEngine();
	}

	/**
	 * Constructor. No random {@link #engine engine} is set, and one should be
	 * provided manually, via the {@link #setMersenneTwister setMersenneTwister}
	 * method.
	 */
	protected DiscreteDistribution() {
	}

	/**
	 * Method to manually set (or override) the default MersenneTwister object.
	 * Should always be called if the constructor is called without referencing
	 * a {@link SimBaseSimulator} object.
	 * 
	 * @param twister
	 *            the new MersenneTwister
	 */
	public void setMersenneTwister(MersenneTwister twister) {
		engine = twister;
	}

	/**
	 * Returns a sample from the distribution. Abstract method.
	 * 
	 * @return a sample from the distribution
	 */
	public abstract long sample();

	/**
	 * Returns a sample from the distribution. Abstract method.
	 * 
	 * @return a sample from the distribution
	 * @since 1.1
	 */
	public abstract double sampleDouble();

	public abstract String toString();



}
