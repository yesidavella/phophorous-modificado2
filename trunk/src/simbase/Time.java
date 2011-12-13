/*
 * Changelog
 * ---------
 * Version 1.1 
 *  - changed timing parameter from long to double (for better precision)
 */
package simbase;

/**
 * The Time class contains timestamps of events, and other time-related
 * quantities. The time is internally represented by a variable of the type
 * long.
 * 
 * @version 1.1
 */
public class Time implements Comparable {

	/**
	 * The time
	 */
	protected double time = 0;

	/**
	 * Constructor
	 */
	public Time() {
		time = 0;
	}

	/**
	 * Constructor
	 * 
	 * @param t
	 *            timestamp
	 */
	public Time(double t) {
		time = t;
	}

	/**
	 * Copy constructor
	 * 
	 * @param t
	 *            timestamp
	 */
	public Time(Time t) {
		time = t.getTime();
	}

	public Time(String t) {
		try {
			time = Double.parseDouble(t);
		} catch (NumberFormatException nfe) {
		}
	}

	/**
	 * Offset constructor
	 * 
	 * @param t
	 *            timestamp
	 * @param offset
	 *            offset from the given timestamp
	 */
	public Time(Time t, double offset) {
		time = t.getTime() + offset;
	}

	/**
	 * Returns the time as long
	 * 
	 * @return the time as long
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Sets the time
	 * 
	 * @param t
	 *            the time
	 * @return the time
	 */
	public Time setTime(Time t) {
		time = t.getTime();
		return this;
	}

	/**
	 * Sets the time
	 * 
	 * @param t
	 *            the time
	 * @return the time
	 */
	public Time setTime(double t) {
		time = t;
		return this;
	}

	/**
	 * Adds a specified time
	 * 
	 * @param t
	 *            time to be added
	 * @return the time
	 */
	public Time addTime(Time t) {
		time = time + t.getTime();
		return this;
	}
	/**
	 * Substracts a specified time
	 * 
	 * @param t
	 *            time to be added
	 * @return the time
	 */
	public Time substractTime(Time t) {
		time = time - t.getTime();
		return this;
	}

	/**
	 * Adds a specified time
	 * 
	 * @param t
	 *            time to be added
	 * @return the time
	 */
	public Time addTime(double t) {
		time = time + t;
		return this;
	}

	// A > B => A.compareTo(B) == +1
	// A < B => A.compareTo(B) == -1
	public int compareTo(Object event) {
		if (this.time < ((Time) event).getTime())
			return -1;
		if (this.time > ((Time) event).getTime())
			return 1;
		return 0;
	}

	public String toString() {
		return ""+time;
	}

}
