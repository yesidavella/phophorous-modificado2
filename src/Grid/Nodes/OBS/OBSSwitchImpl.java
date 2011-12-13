/*
 * This represents an OBSSwitchImpl.
 */
package Grid.Nodes.OBS;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.JobResultMessage;
import Grid.Interfaces.Switches.OBSSwitch;

import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import Grid.Sender.OBS.OBSSwitchSenderImpl;
import Grid.Sender.OBS.OBSWavConSwitchSender;
import Grid.Sender.Sender;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OBSSwitchImpl extends OBSSwitch {

    /**
     * The sender of this switch.
     */
    private Sender sender;

    /**
     * The constructor
     * @param id The id of the OBS Switch
     * @param waveLengthConversion True if wavelengthconversion is used, false if not
     * @param simulator The simulator where it belongs to.
     */
    public OBSSwitchImpl(String id, GridSimulator simulator, boolean wavelengthConversion) {
        super(id, simulator);
        if (wavelengthConversion) {
            sender = new OBSWavConSwitchSender(this, simulator);
        } else {
            sender = new OBSSwitchSenderImpl(simulator, this);
        }
        this.wavelengthConversion = wavelengthConversion;
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);
        handleGridMessage(inPort, (GridMessage) m);

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

    public void route() {
        if (wavelengthConversion) {
            ((OBSWavConSwitchSender) sender).setRoutingMap(gridSim.getRouting().getRoutingTable(this));
        } else {
            ((OBSSwitchSenderImpl) sender).setRoutingMap(gridSim.getRouting().getRoutingTable(this));
        }
    }

    public void handleGridMessage(SimBaseInPort inPort, GridMessage m) {
        if (m.getTypeOfMessage() == GridMessage.MessageType.OBSMESSAGE) {
            //Calculate the time needed to handle an OBS message
            Time t = new Time(currentTime.getTime());
            t.addTime(handleDelay);
            m.setOffSet(m.getOffSet() - handleDelay.getTime());
            if (sender.send(m, t, true)) {
                simulator.addStat(this, Stat.SWITCH_MESSAGE_SWITCHED);

                simulator.putLog(currentTime, this.getId() +
                        " switched : " + m.getId() + " from " + m.getSource() + " to " + m.getDestination() +
                        " handledelay : " + this.getHandleDelay() + " on " +
                        m.getWavelengthID(), Logger.ORANGE, m.getSize(), m.getWavelengthID());
                if (m instanceof JobMessage) {
                    simulator.addStat(this, Stat.SWITCH_JOBMESSAGE_SWITCHED);

                } else if (m instanceof JobResultMessage) {
                    simulator.addStat(this, Stat.SWITCH_JOBRESULTMESSAGE_SWITCHED);

                }


            } else {
                dropMessage(m);

            }

        } else {
            simulator.putLog(currentTime, "FAIL: " + this.getId() +
                    " got a OCS-message :s : " + m.getId(), Logger.RED, m.getSize(), m.getWavelengthID());
            dropMessage(m);
        }

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
