/*
 * This entity represents a Servicenode in the network, the broker. It receives
 * requests from clients and sends them ACK-messages stating which resource they
 * can use or null if none of the resources for which it is responsible is free.
 */
package Grid.Interfaces;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.JobRequestMessage;
import simbase.Port.SimBaseInPort;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public abstract class ServiceNode extends Entity {

    public ServiceNode(String id, GridSimulator gridSim) {
        super(id, gridSim);
    }

    /**
     * Will handle a @link{JobRequestMessage}. It will find a resource and send 
     * it to the requester.
     * @param inPort The inport on which this JobRequestMessage was received.
     * @param msg The JobRequestMessage.
     */
    protected abstract void handleJobRequestMessage(SimBaseInPort inPort, JobRequestMessage msg);

    /**
     * Returns a string represenatation.
     * @return A string representation
     */
    public abstract String getID();

    @Override
    public boolean supportsOBS() {
        return true;
    }

    @Override
    public boolean supportsOCS() {
        return false;
    }

    @Override
    public boolean supportSwitching() {
        return false;
    }
}
