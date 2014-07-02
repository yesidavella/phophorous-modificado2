package Grid.Interfaces.Messages;

import Grid.Nodes.PCE;
import Grid.OCS.OCSRoute;
import java.util.ArrayList;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class JobMessage extends MultiCostMessage {

    
    
      protected double resultSize; 
    /**
     * The job's run length (in MFLOPS). 0 if no processing is needed
     */
    private double flops = 0;
    /**
     * The exection time of this job.
     */
    private double executionTime;

    /**
     * Constructor.
     *
     * @param id The id of this message.
     */
    public JobMessage(String id, Time generationTime) {
        super(id, generationTime);

    }

    /**
     * Constructor, starting from a JobReqAckMessage
     *
     * @param ackMsg the job request acknowledgement message, received from a
     * ServiceNode
     */
    public JobMessage(JobAckMessage ackMsg, Time generationTime) {
        super(ackMsg.getId().substring(0, ackMsg.getId().indexOf("-ACK")), generationTime);
        JobRequestMessage req = ackMsg.getRequestMessage();
        source = ackMsg.destination;
        destination = ackMsg.getResource();
        size = (long) req.getJobSize();
        flops = req.getFlops();
        maxDelay = req.getMaxDelay();
    }

    /**
     * Returns the processing time of this job.
     *
     * @return
     */
    public double getFlops() {
        return flops;
    }

    /**
     * Sets the amount of flops for this job.
     *
     * @param flops The amount of flops for this job.
     */
    public void setFlops(double flops) {
        this.flops = flops;
    }

    /**
     * Returns the execution time of this resource.
     *
     * @return The execution time of this job.
     */
    public double getExecutionTime() {
        return executionTime;
    }

    /**
     * Set the execution time of this job.
     *
     * @param exectionTime The execution time of this job.
     */
    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    @Override
    public boolean equals(Object obj) {
        return id.equals(obj.toString());
    }

    @Override
    public String toString() {
        return id;
    }

    public double getResultSize() {
        return resultSize;
    }

    public void setResultSize(double resultSize) {
        this.resultSize = resultSize;
    }
    

}
