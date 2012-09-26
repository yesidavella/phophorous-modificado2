/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes.OCS;

import Grid.Nodes.*;
import Grid.Entity;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GeneratorMessage;
import Grid.Interfaces.Messages.JobAckMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.JobResultMessage;
import Grid.Interfaces.Messages.OCSConfirmSetupMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Interfaces.ServiceNode;
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
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OCSClientNodeImpl extends AbstractClient {


    /**
     *  Constructor 
     * @param id The id of this client.
     * @param gridSim The simulator.
     */
    public OCSClientNodeImpl(String id, GridSimulator gridSim) {
        super(id, gridSim);
        sender = new OCSEndSender(gridSim, this,5*GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OCSSetupHandleTime));

    }

    public OCSClientNodeImpl(String id, GridSimulator gridSim, ServiceNode broker) {
        super(id, gridSim, broker);
        sender = new OCSEndSender(gridSim, this,5*GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OCSSetupHandleTime));
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

    public void handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        ((OCSEndSender)sender).handleOCSSetupFailMessage(msg);
    }

    private void handleOCSSetupMessage(OCSRequestMessage m) {
        //check if this is the last hop on the path
        OCSRoute ocsRoute = m.getOCSRoute();
        if (((OCSEndSender)sender).handleOCSSetup(m, this)) {
            simulator.putLog(currentTime, "OCS: OCS requestmessage send from <b>" + this.getId() + "</b> to <b>" + ocsRoute.get(ocsRoute.indexOf(this) + 1) + "</b>", Logger.BLACK, m.getSize(), m.getWavelengthID());
        } else {
            simulator.putLog(currentTime, "OCS: OCS Requestmessage could not be send <b>" + this.getId() + "</b> to <b>" + ocsRoute.get(ocsRoute.indexOf(this) + 1) + "</b>", Logger.BLACK, m.getSize(), m.getWavelengthID());
        }
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
        
    }
    
    

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        ((OCSEndSender)sender).requestOCSCircuit(ocsRoute, permanent, time);
    }

    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port, Time time) {
        ((OCSEndSender)sender).tearDownOCSCircuit(ent, wavelength, port, time);
    }

    public void handleTeardownMessage(OCSTeardownMessage msg) {
        ((OCSEndSender)sender).handleOCScircuitTearDown(msg);
    }

    public OCSEndSender getSender() {
        return ((OCSEndSender)sender);
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
}
