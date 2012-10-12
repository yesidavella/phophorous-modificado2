/*
 * The JobReqAckMesage is sent back to the ClientNode from the ServiceNode, and
 * contains the ResourceNode(s) which will be accessed for the execution of the
 * job. Upon receipt of this message, the ClientNode will send out the actual
 * job.
 */
package Grid.Interfaces.Messages;

import Grid.Interfaces.ResourceNode;
import Grid.Nodes.PCE;

/**
 *
 * @author Jens Buysse
 */
public class JobAckMessage extends GridMessage {

    /**
     * The resource the job will be sent to
     */
    private ResourceNode resource = null;
    /**
     * The JobRequestMessage from which this JobAckMessage originated.
     */
    private JobRequestMessage requestMessage;
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
     * Constructor, from the JobRequestMessage
     *
     * @param jobreq the job request message
     */
    public JobAckMessage(JobRequestMessage jobreq) {
        super(jobreq.getId().substring(0, jobreq.getId().indexOf("-req")) + "-ACK ", null);
        requestMessage = jobreq;
        size = jobreq.getSize();
    }

    /**
     * Returns the resource which will process the job
     *
     * @return the resource which will process the job
     */
    public ResourceNode getResource() {
        return resource;
    }

    /**
     * Sets the resource which will process the job
     *
     * @param resource the resource which will process the job
     */
    public void setResource(ResourceNode resource) {
        this.resource = resource;
    }

    /**
     * Return the JobRequestMessage from wich this ACKMessage originated.
     *
     * @return the JobRequestMessage from wich this ACKMessage originated.
     */
    public JobRequestMessage getRequestMessage() {
        return requestMessage;
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
     * Returns the Grid cost estimated by the AG2 resource selector.
     * @return estimatedGridCost.
     */
    public double getEstimatedGridCost() {
        return estimatedGridCost;
    }

    /**
     * Set the Grid cost estimated by the AG2 resource selector.
     * @return estimatedGridCost.
     */
    public void setEstimatedGridCost(double estimatedGridCost) {
        this.estimatedGridCost = estimatedGridCost;
    }
}
