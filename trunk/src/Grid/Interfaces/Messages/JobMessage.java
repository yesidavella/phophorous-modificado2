package Grid.Interfaces.Messages;

import Grid.Nodes.PCE;
import Grid.OCS.OCSRoute;
import java.util.ArrayList;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class JobMessage extends GridMessage {

    /**
     * The job's run length (in MFLOPS). 0 if no processing is needed
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
     * The estimated network cost.
     */
    private double estimatedNetworkCost = 0;
    /**
     * The estimated grid cost.
     */
    private double estimatedGridCost = 0;
    /**
     * The PCE of the domain that has estimated the network cost.
     */
    private PCE domainPCE;
    /**
     * Contains a list of ocss than have to be created in the network.
     */
    private ArrayList<OCSRoute> OCS_Instructions;
    /**
     * The real network cost.
     */
    private double realNetworkCost = 0;

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

    {
        OCS_Instructions = new ArrayList();
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
     * Get the estimated network cost.
     *
     * @return estimatedMarkovianCost
     */
    public double getEstimatedNetworkCost() {
        return estimatedNetworkCost;
    }

    /**
     * Set the estimated network cost.
     */
    public void setEstimatedNetworkCost(double estimatedNetworkCost) {
        this.estimatedNetworkCost = estimatedNetworkCost;
    }

    /**
     * Returns the Grid cost estimated by the AG2 resource selector.
     *
     * @return estimatedGridCost.
     */
    public double getEstimatedGridCost() {
        return estimatedGridCost;
    }

    /**
     * Set the Grid cost estimated by the AG2 resource selector.
     *
     * @return estimatedGridCost.
     */
    public void setEstimatedGridCost(double estimatedGridCost) {
        this.estimatedGridCost = estimatedGridCost;
    }

    /**
     * Get the PCE than has estimated the network cost.
     *
     * @return PCE
     */
    public PCE getDomainPCE() {
        return domainPCE;
    }

    /**
     * Set the PCE than has estimated the network cost.
     */
    public void setDomainPCE(PCE domainPCE) {
        this.domainPCE = domainPCE;
    }

    /**
     * Get a list of ocs that have to be created in the network.
     *
     * @return OCS_Instructions
     */
    public ArrayList<OCSRoute> getOCS_Instructions() {
        return OCS_Instructions;
    }

    /**
     * Get the real network cost evalueted.
     *
     * @return realNetworkCost.
     */
    public double getRealNetworkCost() {
        return realNetworkCost;
    }

    /**
     * Set the real network cost evalueted.
     * @param realNetworkCost
     */
    public void setRealNetworkCost(double realNetworkCost) {
        this.realNetworkCost = realNetworkCost;
    }
}
