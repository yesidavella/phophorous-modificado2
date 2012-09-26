/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes.Hybrid.Parallel;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.JobCompletedMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.OCSConfirmSetupMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Interfaces.ServiceNode;
import Grid.Nodes.AbstractResourceNode;
import Grid.Nodes.Selector.FCFSCPUSelector;
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
public class HybridResourceNode extends AbstractResourceNode {

    public HybridResourceNode(String id, GridSimulator gridSim) {
        super(id, gridSim);
        selector = new FCFSCPUSelector();
        sender = new HyrbidEndSender(this, gridSim);
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
            handleOCSSetupMessage((OCSRequestMessage) message);
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

    @Override
    public boolean supportsOBS() {
        return true;
    }

    @Override
    public boolean supportsOCS() {
        return true;
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
            simulator.putLog(currentTime, "OCS: OCS requestmessage send from <b>" + this.getId() + "</b> to <b>" + ocsRoute.get(ocsRoute.indexOf(this) + 1) + "</b>", Logger.BLACK, m.getSize(), m.getWavelengthID());
        }
    }

    public void handleTeardownMessage(OCSTeardownMessage msg) {
        ((HyrbidEndSender) sender).handleOCScircuitTearDown(msg);
    }

    public void handleOCSConfirmSetupMessage(OCSConfirmSetupMessage msg) {
        simulator.putLog(currentTime, id + " : Confirmation of OCS Setup between " + msg.getOcsRoute().getSource() + " and " +
                msg.getOcsRoute().getDestination() + " has been received.", -1, -1, -1);
        ((HyrbidEndSender) sender).handleConfirmMessage(msg);
    }

    public void handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        ((HyrbidEndSender) sender).handleOCSSetupFailMessage(msg);
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public void removeServiceNode(ServiceNode node)
    {
       serviceNodes.remove(node);
    }
}
