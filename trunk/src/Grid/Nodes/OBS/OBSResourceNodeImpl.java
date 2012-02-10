/*
 * The ResourceNode class represents a resource node in the grid network – both
 * computational and data/storage resources are represented by this class.
 * ResourceNode objects will receive jobs from the clients, process them and
 * return the results.  Like the ClientNode class, the ResourceNode contains 
 * inPort and outPort fields, and a receive method which,
 * depending on the type of the incoming message, will call one of the handler
 * methods. 
 */
package Grid.Nodes.OBS;

import Grid.Entity;
import Grid.Nodes.*;
import Grid.GridSimulator;
import Grid.Interfaces.CPU;
import Grid.Interfaces.Messages.JobCompletedMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.JobResultMessage;
import Grid.Interfaces.Messages.ResourceRegistrationMessage;
import Grid.Interfaces.ServiceNode;
import Grid.Jobs.QueuedJob;
import Grid.Nodes.Selector.FCFSCPUSelector;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import Grid.Sender.OBS.OBSEndSender;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class OBSResourceNodeImpl extends AbstractResourceNode {

    /**
     * Constructor
     */
    public OBSResourceNodeImpl(String id, GridSimulator gridSim) {
        super(id, gridSim);
        selector = new FCFSCPUSelector();
        sender = new OBSEndSender(gridSim, this);
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage message)
            throws StopException {
        super.receive(inPort, message);
        currentTime = new Time(simulator.getMasterClock());

        if (message instanceof JobMessage) {
            this.fireStateChanged();
            handleJobMessage(inPort, (JobMessage) message);
        } else if (message instanceof JobCompletedMessage) {
            handleJobCompletedMessage((JobCompletedMessage) message);
        } else {
            throw new StopException(this.getId() + " received an unknown message");
        }

    }

    @Override
    public boolean supportsOBS() {
        return true;
    }

    @Override
    public boolean supportsOCS() {
        return false;
    }

    @Override
    public void init() {
        if (!inited) {
            super.init();
        }

    }

    public void route() {
        ((OBSEndSender) sender).setRoutingMap(gridSim.getRouting().getRoutingTable(this));
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        simulator.putLog(currentTime, id + " is an OBS Node and cannot request an OCS circuit " + ocsRoute, Logger.RED, -1, -1);
    }

    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port, Time time) {
        simulator.putLog(currentTime, id + " is an OBS Node and cannot tear down an OCS circuit ", Logger.RED, -1, -1);
    }

    @Override
    public void removeServiceNode(ServiceNode node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
