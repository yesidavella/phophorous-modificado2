/*
 * This is the sender for pure OBS sending. This is and EndSender, so only 
 * entities which are at the beginning of a path (which send messages on their 
 * own) will use this kind of sender. This sender, will try to find a free
 * wavelength to send on, and forward the message to the next hop on the path
 * which is calculated in advance by the routing protocol.
 */
package Grid.Sender.OBS;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Port.GridOutPort;
import java.util.Map;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OBSEndSender extends OBSSender {



    public OBSEndSender(GridSimulator simulator, Entity owner) {
        super(owner, simulator);
    }
  
    /**
     * Send a message destination a destination.
     * @param p The port on which the message is being send.
     * @param m The message destination send.
     * @param t The time when the message should arrive.
     * @return true if send worked, false if not
     */
    @Override
    public boolean send(GridMessage message, Time t, boolean outputFail) {
        message.setTypeOfMessage(GridMessage.MessageType.OBSMESSAGE);
        if (owner == null) {
            throw new IllegalArgumentException("(" + this.owner.getId() + ") From is null");

        }
        try {
            GridOutPort port = null;
            port = routingMap.get(message.getDestination().getId());

            //CONTROL PLANE ? La comuunicacion del plano de control se hace en la wave=1.
            if (message.getWavelengthID() == -1) {
                return owner.send(port, message, simulator.getMasterClock());
            }

            int wavelength = owner.findWaveLength(port, t);
            if (wavelength == -1) {
                //NO FREE WAVELENGTH FOUND
                return false;
            } else {
                message.setWavelengthID(wavelength);
            }
            return this.putMessageOnLink(message, port, t);
        } catch (NullPointerException e) {
            System.err.println("Routing map not correctly initialised for : " + owner.getId() + " and " + message.getDestination().getId());
            //System.exit(1);
            return false;
        }
    }

}
