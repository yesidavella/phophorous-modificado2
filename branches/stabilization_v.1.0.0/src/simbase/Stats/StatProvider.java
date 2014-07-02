package simbase.Stats;

import java.io.Serializable;
import simbase.*;
import java.util.EnumMap;
import java.util.HashMap;

import simbase.Stats.SimBaseStats.Stat;

/**
 * The StatProvider provides a statistics gathering mechanism for the simulator.
 * 
 * @author Joachim Vermeir
 * @version 1.0
 */
public class StatProvider  implements Serializable{
	/**
	 * The table containing the mappings between entities and their stats
	 */
	private HashMap<SimBaseEntity, Statistics> entities = new HashMap<SimBaseEntity, Statistics>();

	/**
	 * The global stats
	 */
	private Statistics total = new Statistics();

	/**
	 * Resets all stats
	 */
	public void reset() {
		total = new Statistics();
		entities.clear();
	}

	/**
	 * Adds a given stat to the given entity
	 * 
	 * @param entity
	 *            the entity
	 * @param stat
	 *            the stat
	 */
	public void addStat(SimBaseEntity entity, Stat stat) {
		if (!entities.containsKey(entity))
			entities.put(entity, new Statistics());
		entities.get(entity).addStat(stat);
		total.addStat(stat);
	}
	
	/**
	 * Adds a given global stat
	 * 
	 * @param stat
	 *            the stat
	 */
	public void addStat(Stat stat) {
		total.addStat(stat);
	}

	/**
	 * Adds a given stat to the given entity
	 * 
	 * @param entity
	 *            the entity
	 * @param stat
	 *            the stat
	 * @param value
	 *            the amount to add
	 */
	public void addStat(SimBaseEntity entity, Stat stat, final double value) {
		if (!entities.containsKey(entity))
			entities.put(entity, new Statistics());
		entities.get(entity).addStat(stat, value);
		total.addStat(stat, value);
	}
	/**
	 * Resets the stat for a given entity
	 * 
	 * @param entity
	 *            the entity
	 * @param stat
	 *            the stat
	 */
	public void resetStat(SimBaseEntity entity, Stat stat) {
		if (!entities.containsKey(entity))
			entities.put(entity, new Statistics());
		entities.get(entity).resetStat(stat);
		total.resetStat(stat);
	}

	/**
	 * Resets the global stat
	 * 
	 * @param stat
	 *            the stat
	 */
	public void resetStat(Stat stat) {
		total.resetStat(stat);
	}
	
	/**
	 * Returns the given global stat
	 * 
	 * @param stat
	 *            the stat
	 * @return the given global stat
	 */
	public double getStat(Stat stat) {
		return total.getStat(stat);
	}
	/**
	 * Returns the given stat for a given entity
	 * 
	 * @param entity
	 *            the entity
	 * @param stat
	 *            the stat
	 * @return the given global stat
	 */
	public double getStat(SimBaseEntity entity, Stat stat) {
		if (!entities.containsKey(entity))
			return 0.0;
		return entities.get(entity).getStat(stat);
	}
	
	/**
	 * Returns a list of all stats for the given entity
	 * 
	 * @param entity
	 *            the entity
	 * @return a list of all stats for the given entity
	 */
	public double[] getStats(SimBaseEntity entity) {
		if (!entities.containsKey(entity))
			return new double[0];
		return entities.get(entity).getStats();
	}

	/**
	 * Returns a list of all global stats
	 * 
	 * @return a list of all global stats
	 */
	public double[] getStats() {
		return total.getStats();
	}

	/**
	 * A class providing stats handling
	 * 
	 * @author Joachim Vermeir
	 * @version 1.0
	 */
	protected class Statistics implements Serializable{
		/**
		 * The list of stats
		 */
		private EnumMap<Stat, Double> stats2 = new EnumMap<Stat, Double>(Stat.class);

		/**
		 * Raises the given stat with the given value
		 * 
		 * @param stat
		 *            the stat
		 * @param value
		 *            the value
		 */
		protected void addStat(Stat stat, final double value) {
			if(!stats2.containsKey(stat))
				stats2.put(stat,new Double(0));
			stats2.put(stat, stats2.get(stat).doubleValue() + value);
		}

		/**
		 * Returns a list with all stats
		 * 
		 * @return a list with all stats
		 */
		 
		protected double[] getStats() {
			//Iterator<Double> it = stats2.values().iterator();
			double res[] = new double[Stat.values().length];
			int i =0;
			for(Stat stat : Stat.values())
			{
				Double t = stats2.get(stat);
				res[i++] = t== null?0.0:t;
			}
			return res;
		}

		/**
		 * Raises the given stat by 1.0
		 * 
		 * @param stat
		 *            the stat
		 */
				protected void addStat(Stat stat) {
			if(!stats2.containsKey(stat))
				stats2.put(stat,new Double(0));
			stats2.put(stat, stats2.get(stat).doubleValue() + 1.0);
		}
		
		/**
		 * Resets the given stat
		 * 
		 * @param stat
		 *            the stat
		 */
		protected void resetStat(Stat stat) {
			stats2.put(stat, 0.0);
		}

		/**
		 * Returns the given stat
		 * 
		 * @param stat
		 *            the stat
		 * @return the given stat
		 */
		protected double getStat(Stat stat) {
			if (stats2.containsKey(stat))
				return stats2.get(stat).doubleValue();
				return 0.0;
			
		}
	}
}
