/*
 * This is an OBS Sender for switches who only support OBS. This sender does
 * not support wavelengthconversion. It receives a messages, is looks for a free
 * port to the next hop on the path (calculated in advance by the routing
 * protocol) and tries to send it. If the port is not free, the messages will
 * be dropped.
 */
package Grid.Sender.OBS;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Port.GridOutPort;
import Grid.Sender.Sender;
import java.util.Map;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public abstract class OBSSender extends Sender {

    /**
     * The routingmap for eacht entity.
     */
    protected Map<String, GridOutPort> routingMap;

    public OBSSender(Entity owner, GridSimulator simulator) {
        super(owner, simulator);
    }

    public Map<String, GridOutPort> getRoutingMap() {
        return routingMap;
    }

    public void setRoutingMap(Map<String, GridOutPort> routingMap) {
        this.routingMap = routingMap;
    }

    /**
     * Will send a message with the specified destination, thus overriding 
     * the destination which has been given to the message. This method can
     * be used to implement hybrid sending. When one is not able to put this 
     * message on a switch one could op to send it via OBS to the next hop
     * on the path.
     * @param message The message to send
     * @param t The time when to reception is needed.
     * @param outputFail Whether to output the logs
     * @param destination The destination for this message, bypassing the destination given
     * to the message.
     * @return true if sending worked, false if not.
     */
    public boolean send(GridMessage message, Time t, boolean outputFail, Entity destination) {
        message.setTypeOfMessage(GridMessage.MessageType.OBSMESSAGE);
        if (owner == null) {
            throw new IllegalArgumentException("(" + this.owner.getId() + ") From is null");

        }
        try {
            GridOutPort port = null;
            port = routingMap.get(destination.getId());

            //CONTROL PLANE ?
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
