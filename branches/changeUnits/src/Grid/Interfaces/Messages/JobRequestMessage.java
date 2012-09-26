/**
 * The JobRequestMessage is sent out by the ClientNode to the ServiceNode in
 * order to reserve resources for the requested job.
 */
package Grid.Interfaces.Messages;

import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class JobRequestMessage extends GridMessage {

    /**
     * Static variable for making ID's for the jobs.
     */
    public static int jobCounter = 0;
    /**
     * Amount of flops of this job.
     */
    protected double flops;
    /**
     * Job size (the acutal amount of job data) -  difference with size is 
     * that size is the size of the ack message.
     */
    protected double jobSize;

    public double getJobSize() {
        return jobSize;
    }

    public void setJobSize(double jobSize) {
        this.jobSize = jobSize;
    }
    
    
    
    
    /**
     * Constructor
     * 
     * @param id
     *            message ID
     * @param time
     *            creation time
     */
    public JobRequestMessage(String id, Time time) {
        super(id, time);
    }

    public double getFlops() {
        return flops;
    }

    public void setFlops(double flops) {
        this.flops = flops;
    }
    
    
    
}
