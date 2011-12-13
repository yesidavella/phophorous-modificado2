package simbase;

/**
 * The base class from which specific messages are derived. A message represents
 * whatever communication is to take place between entities, and is included in
 * each SimBaseEvent. The base class does not have any properties. For example,
 * if a client in the grid creates a job, the corresponding SimBaseMessage that
 * is sent to a resource for processing will contain the job details.
 * 
 * @version 1.0
 */
public class SimBaseMessage {

	/**
	 * The message ID
	 */
	protected String id = "";

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the message ID
	 */
	public SimBaseMessage(String id) {
		this.id = id;
	}

	/**
	 * Returns the message ID
	 * 
	 * @return the message ID
	 */
	public String getId() {
		return id;
	}

	public String toString() {
		return id;
	}

}
