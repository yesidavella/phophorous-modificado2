/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Sender.OBS;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Port.GridOutPort;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OBSWavConSwitchSender extends OBSSender {

    public OBSWavConSwitchSender(Entity owner, GridSimulator simulator) {
        super(owner, simulator);
    }

    /**
     * Send a message 
     * @param message The message
     * @param t The time when destination send
     * @param owner The entity which wants destination send.
     * @param destination The entity destination which the message has te be send.
     * @return True if sending worked, false if not.
     */
    public boolean send(GridMessage message, Time t, boolean outputFail) {
        message.setTypeOfMessage(GridMessage.MessageType.OBSMESSAGE);
        GridOutPort port = routingMap.get(message.getDestination().getId());
        int wavelength = message.getWavelengthID();

        if (wavelength == -1) {
            //Control message like generatormessage
            return owner.send(port, message, simulator.getMasterClock());
        } else {
            if (this.putMessageOnLink(message, port, t)) {
                return true;
            } else {
                //Find another wavelength which is free
                int newWavelength = owner.findWaveLength(port, t);
                if (newWavelength == -1) {
                    return false;
                } else {
                    message.setWavelengthID(newWavelength);
                    return this.putMessageOnLink(message, port, t);
                }
            }
        }
    }
}
