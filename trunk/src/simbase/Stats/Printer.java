package simbase.Stats;

import simbase.*;
import simbase.Stats.SimBaseStats;
import java.io.PrintStream;

/**
 * This interface is used for printing information about the gathered statistics
 * from the simulation
 * 
 * @author Joachim Vermeir
 * @version 1.0
 */
public interface Printer extends SimBaseStats {
	/**
	 * Print the necessary/wanted information from the simulation
	 * 
	 * @param sim
	 *            the simulator
	 */
	public void printInformation(SimBaseSimulator sim);
	/**
	 * Print the necessary/wanted information from the simulation
	 * 
	 * @param sim
	 *            the simulator
	 */
	public void printInformation(SimBaseSimulator sim, PrintStream out);
}
