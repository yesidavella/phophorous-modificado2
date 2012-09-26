package Grid.Interfaces.Messages;

import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class JobMessage extends GridMessage {

    /**
     * The job's run length (in FLOPS). 0 if no processing is needed
     */
    private double flops = 0;
    /**
     * The exection time of this job.
     */
    private double executionTime;
    /**
     * This message have to be avaluated at its first arrived at a switch, not
     * in the latter switches
     */
    private boolean realMarkovCostEvaluated = false;
    /**
     * This is the estimated cost of the markovian process. This cost is based
     * on the nerwork state when the PCE evaluates it, but the real cost could
     * be different because the two costs are made in different moments and the
     * network state could change.
     */
    private double estimatedMarkovianCost = 0;

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
     * @param job the job request acknowledgement message, received from a
     * ServiceNode
     */
    public JobMessage(JobAckMessage job, Time generationTime) {
        super(job.getId().substring(0, job.getId().indexOf("-ACK")), generationTime);
        JobRequestMessage req = job.getRequestMessage();
        source = job.destination;
        destination = job.getResource();
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

    public boolean isRealMarkovCostEvaluated() {
        return realMarkovCostEvaluated;
    }

    public void setRealMarkovCostEvaluated(boolean realMarkovCostEvaluated) {
        this.realMarkovCostEvaluated = realMarkovCostEvaluated;
    }

    /**
     * Returns the estimated markovian cost made by the PCE of make the MDP over
     * the network in a specific moment.
     *
     * @return estimatedMarkovianCost
     */
    public double getEstimatedMarkovianCost() {
        return estimatedMarkovianCost;
    }

    /**
     * Sets the estimated markovian cost over the network in a specific moment.
     * This cost have to be made only by the PCE.
     */
    public void setEstimatedMarkovianCost(double estimatedMarkovianCost) {
        this.estimatedMarkovianCost = estimatedMarkovianCost;
    }
}
