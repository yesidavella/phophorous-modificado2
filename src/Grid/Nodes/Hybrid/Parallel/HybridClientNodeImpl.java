/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes.Hybrid.Parallel;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GeneratorMessage;
import Grid.Interfaces.Messages.JobAckMessage;
import Grid.Interfaces.Messages.JobResultMessage;
import Grid.Interfaces.Messages.OCSConfirmSetupMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Interfaces.ServiceNode;
import Grid.Nodes.AbstractClient;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import Grid.Sender.Hybrid.Parallel.HyrbidEndSender;
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
public class HybridClientNodeImpl extends AbstractClient {

    /**
     * Constructor
     * @param id The id of this Hybrid Client node
     * @param simulator The simulator to which this client node belongs to.
     * @param broker The broker to which this client sends his job request to.
     */
    public HybridClientNodeImpl(String id, GridSimulator simulator, ServiceNode broker) {
        super(id, simulator, broker);
        sender = new HyrbidEndSender(this, simulator);

    }

    public HybridClientNodeImpl(String id, GridSimulator simulator) {
        super(id, simulator);
        sender = new HyrbidEndSender(this, simulator);
    }
    
    
    
    



    @Override
    public boolean supportsOBS() {
        return true;
    }

    @Override
    public boolean supportsOCS() {
        return true;
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);
        if (m instanceof GeneratorMessage) {
            handleGeneratorMessage(inPort, (GeneratorMessage) m);
        } else if (m instanceof JobAckMessage) {
            handleJobAckMessage(inPort, (JobAckMessage) m);
        } else if (m instanceof JobResultMessage) {
            handleJobResultMessage(inPort, (JobResultMessage) m);
        } else if (m instanceof OCSRequestMessage) {
            handleOCSSetupMessage((OCSRequestMessage) m);
        } else if (m instanceof OCSTeardownMessage) {
            handleTeardownMessage((OCSTeardownMessage) m);
        } else if (m instanceof OCSSetupFailMessage) {
            handleOCSSetupFailMessage((OCSSetupFailMessage) m);
        } else if (m instanceof OCSConfirmSetupMessage) {
            handleOCSConfirmSetupMessage((OCSConfirmSetupMessage) m);
        } else {
            throw new StopException(this.getId() + " received an unknown message : " + m.getId());
        }

    }

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        ((HyrbidEndSender) sender).requestOCSCircuit(ocsRoute, permanent, time);
    }

    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port, Time time) {
        ((HyrbidEndSender) sender).tearDownOCSCircuit(ent, wavelength, port, time);
    }

    public void handleOCSSetupMessage(OCSRequestMessage m) {
        //check if this is the last hop on the path
        OCSRoute ocsRoute = m.getOCSRoute();
        if (((HyrbidEndSender) sender).handleOCSSetup(m, this)) {
            //simulator.putLog(currentTime, "OCS: OCS requestmessage send from <b>" + this.getId() + "</b> to <b>" + ocsRoute.get(ocsRoute.indexOf(this) + 1) + "</b>", Logger.ORANGE, m.getSize(), m.getWavelengthID());
        }
    }

    public void handleTeardownMessage(OCSTeardownMessage msg) {
        ((HyrbidEndSender) sender).handleOCScircuitTearDown(msg);
    }

    public void handleOCSConfirmSetupMessage(OCSConfirmSetupMessage msg) {
        simulator.putLog(currentTime, id + " : Confirmation of OCS Setup between " + msg.getOcsRoute().getSource() + " and " +
                msg.getOcsRoute().getDestination() + " has been received.", Logger.ORANGE, -1, -1);
        ((HyrbidEndSender) sender).handleConfirmMessage(msg);
    }

    public void handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        ((HyrbidEndSender) sender).handleOCSSetupFailMessage(msg);
    }

    @Override
    public void init() {
        if (!inited) {
            super.init();
        }

    }
    
    public void route(){
        //sets the routingmap for this object
        OBSEndSender obs = (OBSEndSender) ((HyrbidEndSender) sender).getObsSender();
        obs.setRoutingMap(gridSim.getRouting().getRoutingTable(this));
    }
}
