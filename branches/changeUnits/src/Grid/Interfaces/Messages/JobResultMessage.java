/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Grid.Interfaces.Messages;

import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class JobResultMessage extends GridMessage {
    
    /**
	 * The job description
	 */
	private JobMessage job = null;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            job id
	 */
	public JobResultMessage(String id,Time generationTime) {
		super(id,generationTime);
	}

	/**
	 * Constructor, starting from the corresponding JobCompletedMessage
	 * 
	 * @param jobMessage
	 *            the job info
	 */
	public JobResultMessage(JobCompletedMessage jobMessage,Time generationTime) {
		super(jobMessage.getId()+" - completed",generationTime);
		job = jobMessage.getJob();
		this.source = job.getDestination();
		this.destination = job.getSource();
	}

	/**
	 * Returns the job info
	 * 
	 * @return the job info
	 */
	public JobMessage getJob() {
		return job;
	}

	public String toString() {
		return "Job results returning to client: " + job.getId();
	}


}
