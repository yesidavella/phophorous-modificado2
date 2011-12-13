/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes.Hybrid.Parallel;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.OCSConfirmSetupMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Nodes.AbstractServiceNode;
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
public class HybridServiceNode extends AbstractServiceNode {

    public HybridServiceNode(String id, GridSimulator sim) {
        super(id, sim);
        sender = new HyrbidEndSender(this, sim);
    }

    private void handleOCSSetupMessage(SimBaseInPort inport, OCSRequestMessage m) {
        OCSRoute ocsRoute = m.getOCSRoute();
        if (((HyrbidEndSender) sender).handleOCSSetup(m, this)) {
            simulator.putLog(currentTime, "OCS: OCS requestmessage send from <b>" + this.getId() + "</b> to <b>" + ocsRoute.get(ocsRoute.indexOf(this) + 1) + "</b>", Logger.BLACK, m.getSize(), m.getWavelengthID());
        }

    }

    /**
     * The receive method. Will call the appropriate handler routine as it
     * receives incoming messages.
     */
    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage message)
            throws StopException {
        if (message instanceof OCSRequestMessage) {
            handleOCSSetupMessage(inPort, (OCSRequestMessage) message);
        } else if (message instanceof OCSTeardownMessage) {
            handleTeardownMessage((OCSTeardownMessage) message);
        } else if (message instanceof OCSSetupFailMessage) {
            handleOCSSetupFailMessage((OCSSetupFailMessage) message);
        } else if (message instanceof OCSConfirmSetupMessage) {
            handleOCSConfirmSetupMessage((OCSConfirmSetupMessage) message);
        } else {
            super.receive(inPort, message);
        }
    }

    public void handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        ((HyrbidEndSender) sender).handleOCSSetupFailMessage(msg);
    }

    public void handleTeardownMessage(OCSTeardownMessage msg) {
        ((HyrbidEndSender) sender).handleOCScircuitTearDown(msg);
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

    /**
     * This handle is triggered when this client inited a ocs circuit setup for a message.
     * It will send all messages which are in need of this circuit and then tear it down.
     * @param msg The OCSConfirmSetupMessage which has been send to notify this 
     *              client of an OCS circuit setup.
     */
    public void handleOCSConfirmSetupMessage(OCSConfirmSetupMessage msg) {
        simulator.putLog(currentTime, id + " : Confirmation of OCS Setup between " + msg.getOcsRoute().getSource() + " and " +
                msg.getOcsRoute().getDestination() + " has been received.", -1, -1, -1);
        ((HyrbidEndSender) sender).handleConfirmMessage(msg);
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
