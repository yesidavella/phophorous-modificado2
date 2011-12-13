/**
 * The ClientNode class provides a framework for {@link SimBaseEntity} objects
 * that generate new events, with arrival times following a given distribution
 * (specified in the distribution property of the ClientNode object). The actual
 * event generation is done in an abstract method, which must obviously be
 * filled in by the derived class. The ClientNode generates bursts by sending an
 * event to itself. Incoming events can therefor be split in two types: events
 * originating from itself and events coming from other entities. 
 * 
 * @version 2.0
 */
package Grid.Interfaces;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Nodes.State.ClientState;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public abstract class ClientNode extends Entity {

    /**
     * The state of this client. This state contains all necessary information
     * of the client to generate messages : generatormessages, JobRequests..
     */
    protected ClientState state;

    /**
     * Creates a clientnode.
     * @param id The id of this client.
     * @param gridSim The simulator to which it belongs.
     */
    public ClientNode(String id, GridSimulator gridSim) {
        super(id, gridSim);
        state = new ClientState(this.getId() + "state", simulator);
    }

    /**
     * Returns the {@link ServiceNode} object this client will use for its jobs
     * 
     * @return the {@link ServiceNode} object this client will use for its jobs
     */
    public abstract ServiceNode getServiceNode();

    /**
     * Sets the {@link ServiceNode} object this client will use for its jobs
     * 
     * @param serviceNode
     *            the {@link ServiceNode} object this client will use for its
     *            jobs
     */
    public abstract void setServiceNode(ServiceNode serviceNode);

    /**
     * Returns the {@link ServiceNode} name this client will use for its jobs
     * 
     * @return the {@link ServiceNode} name this client will use for its jobs
     * @since 2.0
     */
    public abstract String getServiceNodeName();

    /**
     * Sends out a job, from the current job state, to the resource broker
     * which task is to shedule the jobs.
     */
    public abstract void sendJob();

    /**
     * Returns the state of this client.
     * @return The state of the client.
     */
    public ClientState getState() {
        return state;
    }

    /**
     * Sets the state of the client.
     * @param state The new state.
     */
    public void setState(ClientState state) {
        this.state = state;
    }

    @Override
    public boolean supportSwitching() {
        return false;
    }
    
    
}
