/*
 * The JobReqAckMesage is sent back to the ClientNode from the ServiceNode, and
 * contains the ResourceNode(s) which will be accessed for the execution of the
 * job. Upon receipt of this message, the ClientNode will send out the actual
 * job.
 */
package Grid.Interfaces.Messages;

import Grid.Interfaces.ResourceNode;

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
     * This is the estimated cost of the markovian process. This cost is based
     * on the nerwork state when the PCE evaluates it, but the real cost could
     * be different because the two costs are made in different moments and the
     * network state could change.
     */
    private double estimatedMarkovianCost = 0;

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
