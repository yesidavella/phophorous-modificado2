/**
 * The ClientState class contains all information needed to generate jobs.
 *
 * @version 1.1
 */
package Grid.Nodes.State;

import Distributions.DiscreteDistribution;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.Messages.JobRequestMessage;
import java.io.Serializable;
import simbase.SimBaseSimulator;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class ClientState implements Serializable {

    public enum StateType {

        ERLANG, HYPEREXP, NEGEXP, NORMAL, POISSOIN, UNIFORM
    }
    /**
     * The ID for the state
     */
    private String id = "";
    /**
     * The job interval distribution
     */
    private DiscreteDistribution jobInterArrival = null;
    /**
     * Distribution of the job processing requirements (in processing units.
     *
     * @see ResourceNode
     */
    protected DiscreteDistribution flops = null;
    /**
     * The max delay distribution
     */
    private DiscreteDistribution maxDelayInterval = null;
    /**
     * The datasize distribution.
     */
    private DiscreteDistribution sizeDistribution = null;
    /**
     * The distribution of the ACK/NACK packet.
     */
    private DiscreteDistribution ackSizeDistribution = null;
    /**
     * Constructor
     *
     * @param id ID of the state
     */
    /**
     * Added to give trafic priority for the AG2 project. It must be a natural
     * number between 1 and 10.
     */
    private int trafficPriority = 5;

    public ClientState(String id, SimBaseSimulator simulator) {
        this.id = id;
    }

    /**
     * Returns the ID of the state
     *
     * @return the ID of the state
     */
    public String getID() {
        return id;
    }

    /**
     * Returns an interarrival time for jobs from this state, sampled from the
     * distribution
     *
     * @return an interarrival time
     */
    public double getJobIntervalSample() {
        return jobInterArrival.sampleDouble();
    }

    /**
     * Returns a string representation of this ClientState.
     *
     * @return
     */
    @Override
    public String toString() {
        return id;
    }

    public DiscreteDistribution getFlops() {
        return flops;
    }

    public DiscreteDistribution getAckSizeDistribution() {
        return ackSizeDistribution;
    }

    public void setAckSizeDistribution(DiscreteDistribution ackSizeDistribution) {
        this.ackSizeDistribution = ackSizeDistribution;
    }

    public void setFlops(DiscreteDistribution flops) {
        this.flops = flops;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DiscreteDistribution getJobInterArrival() {
        return jobInterArrival;
    }

    public void setJobInterArrival(DiscreteDistribution jobInterArrival) {
        this.jobInterArrival = jobInterArrival;
    }

    public DiscreteDistribution getMaxDelayInterval() {
        return maxDelayInterval;
    }

    public void setMaxDelayInterval(DiscreteDistribution maxDelayInterval) {
        this.maxDelayInterval = maxDelayInterval;
    }

    public DiscreteDistribution getSizeDistribution() {
        return sizeDistribution;
    }

    public void setSizeDistribution(DiscreteDistribution sizeDistribution) {
        this.sizeDistribution = sizeDistribution;
    }

    /**
     * Will generate a new job request. If the size given in the parameters than
     * this message has to be send on the control plane on wavelengths -1.
     *
     * @param source the originating client node
     * @param jobID the ID of the job request
     * @param time the time at which this job request will be generated
     * @return a new job request
     */
    public JobRequestMessage generateJob(ClientNode source, String jobID, Time time) {
        JobRequestMessage requestMsg = new JobRequestMessage(jobID + "-req", time);
        requestMsg.setSource(source);
        requestMsg.setDestination(source.getServiceNode());
        requestMsg.setFlops((double) flops.sampleDouble());
//        requestMsg.setMaxDelay(maxDelayInterval.sampleDouble());
        requestMsg.setSize(ackSizeDistribution.sampleDouble());
        requestMsg.setJobSize(this.sizeDistribution.sample());

        if (requestMsg.getSize() == 0) {
            requestMsg.setWavelengthID(-1);
        }
        return requestMsg;
    }

    public int getTrafficPriority() {
        return trafficPriority;
    }

    public void setTrafficPriority(int trafficPriority) {
        this.trafficPriority = trafficPriority;
    }
}
