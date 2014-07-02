package Grid.Nodes.OCS;

import Grid.Entity;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.JobResultMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Interfaces.Switches.OCSSwitch;
import Grid.OCS.OCSRoute;
import Grid.Port.GridInPort;
import Grid.Port.GridOutPort;
import Grid.Sender.OCS.OCSSwitchSender;
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
public class OCSSwitchImpl extends OCSSwitch {

    /**
     * The Sender for this switch
     */
    private OCSSwitchSender sender;

    /**
     * Constructor
     * @param id The id of this switch
     * @param simulator The simulator to which it belongs.
     */
    public OCSSwitchImpl(String id, GridSimulator simulator) {
        super(id, simulator);
        sender = new OCSSwitchSender(simulator, this,GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OCSSetupHandleTime));

    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);
        if (m instanceof OCSRequestMessage) {
            handleOCSSetupMessage(inPort, (OCSRequestMessage) m);
        } else if (m instanceof OCSTeardownMessage) {
            handleTeardownMessage((OCSTeardownMessage) m, (GridInPort) inPort);
        } else if (m instanceof OCSSetupFailMessage) {
            handleOCSSetupFailMessage((OCSSetupFailMessage) m);
        } else {
            handleGridMessage(inPort, (GridMessage) m);
        }
    }

    public void handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        sender.handleOCSSetupFailMessage(msg);
    }

    public void handleTeardownMessage(OCSTeardownMessage msg, GridInPort port) {
        sender.handleTearDownOCSCircuit(msg, port);
    }

    /**
     * Will handle incoming grid messages which have nothing to do with 
     * OCS Path setup.
     * @param inport
     * @param m the message to forward.
     */
    private void handleGridMessage(SimBaseInPort inport, GridMessage m) {
        if (sender.send(m, inport,currentTime,true)) {
            simulator.addStat(this, Stat.SWITCH_MESSAGE_SWITCHED);

            simulator.putLog(currentTime, this.getId() + " switched " + m.getId() + m.getWavelengthID(), Logger.BLACK, m.getSize(), m.getWavelengthID());
            if (m instanceof JobMessage) {
                simulator.addStat(this, Stat.SWITCH_JOBMESSAGE_SWITCHED);

            }else if(m instanceof JobResultMessage){
                simulator.addStat(this, Stat.SWITCH_JOBRESULTMESSAGE_SWITCHED);

            }
        } else {
            dropMessage(m);
        }

    }

    private void handleOCSSetupMessage(SimBaseInPort inport, OCSRequestMessage m) {
        sender.handleOCSPathSetupMessage(m, inport);

    }

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute,boolean permanent,Time time) {
        sender.requestOCSCircuit(ocsRoute,permanent,time);
    }

    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port,Time time) {
        simulator.putLog(currentTime, id + " is an OCS Switch and cannot tear down an OCS circuit on its own ", Logger.RED, -1, -1);
    }

    public OCSSwitchSender getSender() {
        return sender;
    }

    @Override
    public void route() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
