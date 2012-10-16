package Grid.Nodes.OCS;

import Distributions.DiscreteDistribution;
import Grid.Entity;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.JobCompletedMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.OCSConfirmSetupMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Interfaces.ServiceNode;
import Grid.Nodes.*;
import Grid.Nodes.Selector.FCFSCPUSelector;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import Grid.Sender.OCS.OCSEndSender;
import Grid.Utilities.Config;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class OCSResourceNodeImpl extends AbstractResourceNode {

    /**
     * Constructor
     * @param id The name of this resource node.
     * @param gridSim The gridsimulator.
     */
    public OCSResourceNodeImpl(String id, GridSimulator gridSim, DiscreteDistribution resultSizeDistribution) {
        super(id, gridSim, resultSizeDistribution);
        selector = new FCFSCPUSelector();
        sender = new OCSEndSender(gridSim, this, 5*GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OCSSetupHandleTime));
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage message)
            throws StopException {
        super.receive(inPort, message);
        if (message instanceof JobMessage) {
            handleJobMessage(inPort, (JobMessage) message);
        } else if (message instanceof JobCompletedMessage) {
            handleJobCompletedMessage((JobCompletedMessage) message);
        } else if (message instanceof OCSRequestMessage) {
            handleOCSSetupMessage(inPort, (OCSRequestMessage) message);
        } else if (message instanceof OCSTeardownMessage) {
            handleTeardownMessage((OCSTeardownMessage) message);
        } else if (message instanceof OCSSetupFailMessage) {
            handleOCSSetupFailMessage((OCSSetupFailMessage) message);
        } else if (message instanceof OCSConfirmSetupMessage) {
            handleOCSConfirmSetupMessage((OCSConfirmSetupMessage) message);
        } else {
            throw new StopException(this.getId() + " received an unknown message");
        }

    }
    
    private void handleOCSSetupMessage(SimBaseInPort inport, OCSRequestMessage m) {
        //check if this is the last hop on the path
        OCSRoute ocsRoute = m.getOCSRoute();
        if (((OCSEndSender)sender).handleOCSSetup(m, this)) {
            simulator.putLog(currentTime, "OCS: OCS requestmessage send from <b>" + this.getId() + "</b> to <b>" + ocsRoute.get(ocsRoute.indexOf(this) + 1) + "</b>", Logger.BLACK, m.getSize(), m.getWavelengthID());
        } else {
            simulator.putLog(currentTime, "OCS: OCS Requestmessage could not be send <b>" + this.getId() + "</b> to <b>" + ocsRoute.get(ocsRoute.indexOf(this) + 1) + "</b>", Logger.BLACK, m.getSize(), m.getWavelengthID());
        }
    }

    public void handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        updateTime(simulator.getMasterClock());
        ((OCSEndSender)sender).handleOCSSetupFailMessage(msg);
    }

    public void handleTeardownMessage(OCSTeardownMessage msg) {
        updateTime(simulator.getMasterClock());
        ((OCSEndSender)sender).handleOCScircuitTearDown(msg);
    }

    @Override
    public boolean supportsOBS() {
        return false;
    }

    @Override
    public boolean supportsOCS() {
        return true;
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void route() {
        
    }
    
    

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        ((OCSEndSender)sender).requestOCSCircuit(ocsRoute, permanent, time);
    }

    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port, Time time) {
        ((OCSEndSender)sender).tearDownOCSCircuit(ent, wavelength, port, time);
    }

    /**
     * This handle is triggered when this client inited a ocs circuit setup for a message.
     * It will send all messages which are in need of this circuit and then tear it down.
     * @param msg The OCSConfirmSetupMessage which has been send to notify this 
     *              client of an OCS circuit setup.
     */
    public void handleOCSConfirmSetupMessage(OCSConfirmSetupMessage msg) {
        simulator.putLog(currentTime, id + " : Confirmation of OCS Setup between " + msg.getOcsRoute().getSource() + " and " +
                msg.getOcsRoute().getDestination() + " has been received.", -1, -1, -1);
        ((OCSEndSender)sender).handleConfirmMessage(msg);
    }

    @Override
    public void removeServiceNode(ServiceNode node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
