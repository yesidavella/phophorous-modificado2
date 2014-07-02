/*
 * The Client class provides a framework for {@link SimBaseEntity} objects
 * that generate new events, with arrival times following a given distribution
 * (specified in the distribution property of the Client object). The actual
 * event generation is done in an abstract method, which must obviously be
 * filled in by the derived class. The Client generates bursts by sending an
 * event to itself. Incoming events can therefor be split in two types: events
 * originating from itself and events coming from other entities. The first type
 * will be handled in this class (calling the abstract method and sending a new
 * reminder event), while the second type will be dealt with using the receive()
 * method from the derived class. 
 */
package Grid.Nodes.OBS;

import Grid.Entity;
import Grid.Nodes.*;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GeneratorMessage;
import Grid.Interfaces.Messages.JobAckMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.JobResultMessage;
import Grid.Interfaces.ServiceNode;
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
public class OBSClientImpl extends AbstractClient {

    /**
     * Constructor of the client class.
     * @param id The id of this Entity (OBSClientImpl)
     * @param simulator The simulator to which is belongs.
     */
    public OBSClientImpl(String id, GridSimulator simulator) {
        super(id, simulator);
        sender = new OBSEndSender(simulator, this);
    }

    /**
     * Constructor of the client class.
     * @param id The id of this Entity (OBSClientImpl)
     * @param simulator The simulator to which is belongs.
     */
    public OBSClientImpl(String id, GridSimulator simulator, ServiceNode broker) {
        super(id, simulator, broker);
        sender = new OBSEndSender(simulator, this);

    }

    /**
     * The receive method. Will call the appropriate handler routine as it
     * receives incoming messages.
     * @param inPort The inport on which the client receives a message
     * @param m The message that was send.
     * @throws simbase.Exceptions.StopException
     */
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);
        if (m instanceof GeneratorMessage) {
            handleGeneratorMessage(inPort, (GeneratorMessage) m);
        } else if (m instanceof JobAckMessage) {
            handleJobAckMessage(inPort, (JobAckMessage) m);
        } else if (m instanceof JobResultMessage) {
            handleJobResultMessage(inPort, (JobResultMessage) m);
        } else {
            throw new StopException(this.getId() + " received an unknown message");
        }
    }

    @Override
    public void init() {
        if (!inited) {
            super.init();
        }

    }
    
    public void route(){
        ((OBSEndSender) sender).setRoutingMap(gridSim.getRouting().getRoutingTable(this));
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
    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        simulator.putLog(currentTime, id + " is an OBS Node and cannot request an OCS circuit", Logger.RED, -1, -1);
    }

    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port, Time time) {
        simulator.putLog(currentTime, id + " is an OBS Node and cannot tear down an OCS circuit ", Logger.RED, -1, -1);
    }
}
