/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes.OCS;

import Grid.Entity;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.OCSConfirmSetupMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Nodes.AbstractServiceNode;
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
public class OCSServiceNodeImpl extends AbstractServiceNode {


    /**
     * Constructor. Will generate default in- and outports
     * 
     * @param id
     *            the internal id of the object, used by the code
     * @param sim
     *            the {@link SimBaseSimulator} object this object is registered
     *            with
     */
    public OCSServiceNodeImpl(String id, GridSimulator sim) {
        super(id, sim);
        sender = new OCSEndSender(sim, this,5*GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OCSSetupHandleTime));

    }

   

    private void handleOCSSetupMessage(SimBaseInPort inport, OCSRequestMessage m) {
        OCSRoute ocsRoute = m.getOCSRoute();
        if (((OCSEndSender)sender).handleOCSSetup(m, this)) {
            simulator.putLog(currentTime, "OCS: OCS requestmessage send from <b>" + this.getId() + "</b> to <b>" + ocsRoute.get(ocsRoute.indexOf(this) + 1) + "</b>", Logger.BLACK, m.getSize(), m.getWavelengthID());
        } else {
            simulator.putLog(currentTime, "OCS: OCS Requestmessage could not be send <b>" + this.getId() + "</b> to <b>" + ocsRoute.get(ocsRoute.indexOf(this) + 1) + "</b>", Logger.BLACK, m.getSize(), m.getWavelengthID());
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
        ((OCSEndSender)sender).handleOCSSetupFailMessage(msg);
    }

    public void handleTeardownMessage(OCSTeardownMessage msg) {
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
    public void route() {
        throw new UnsupportedOperationException("Not supported yet.");
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
        simulator.putLog(currentTime, id+ " : Confirmation of OCS Setup between " + msg.getOcsRoute().getSource() + " and " +
                msg.getOcsRoute().getDestination() + " has been received.", -1, -1, -1);
        ((OCSEndSender)sender).handleConfirmMessage(msg);
    }
}
