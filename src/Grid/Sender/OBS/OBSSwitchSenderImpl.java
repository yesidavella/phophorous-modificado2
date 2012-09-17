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
public class OBSSwitchSenderImpl extends OBSSender {

    /**
     * Constructor
     * @param simulator The simulator
     * @param routingMap The routingmap for the entity to which this sender belongs to.
     */
    public OBSSwitchSenderImpl(GridSimulator simulator, Entity owner) {
        super(owner, simulator);
    }

    /**
     * Send a message 
     * @param message The message
     * @param t The time when to send
     * @param owner The entity which wants to send.
     * @param to The entity to which the message has te be send.
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
                return putMsgOnLink(message, port, t, false, 0);
        }
    }
}
