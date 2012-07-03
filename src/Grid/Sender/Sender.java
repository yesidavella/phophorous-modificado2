/*
 * The Interface Sender is responsible for putting messages on the links.
 */
package Grid.Sender;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Port.GridOutPort;
import java.io.Serializable;
import java.util.Map;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public abstract class Sender implements Serializable{

    /**
     * The owner of this sender.
     */
    protected Entity owner;
    /**
     * The Gridsimulator
     */
    protected GridSimulator simulator;

    /**
     * Constructor
     * @param owner
     */
    public Sender(Entity owner, GridSimulator simulator) {
        this.owner = owner;
        this.simulator = simulator;
    }

    /**
     * Send a message into the network. This method must be implemented by 
     * every class who extends Sender.
     * @param message The message to send
     * @param t The portFreeAgainTime when to send the message
     * @param source The source which sends the message
     * @param destination The destination of the message
     * @param outputFail Output the failures? This is set to false when hybrid switching is used
     * @return True if sending worked, False if not. 
     */
    public abstract boolean send(GridMessage message, Time t, boolean outputFail);

    /**
     * Returns the simulator this sender belongs to.
     * @return The simulator of this sender.
     */
    public GridSimulator getSimulator() {
        return simulator;
    }

    /**
     * Sets the simulator this sender belongs to.
     * @param simulator The new simulator to which this sender belongs to.
     */
    public void setSimulator(GridSimulator simulator) {
        this.simulator = simulator;
    }

    /**
     * Returns the owner of this class.
     * @return the owner of this class.
     */
    public Entity getOwner() {
        return owner;
    }

    /**
     * Set the owner of this sender. 
     * @param owner The new owner of this sender.
     */
    public void setOwner(Entity owner) {
        this.owner = owner;
    }

    protected boolean putMessageOnLink(GridMessage message, GridOutPort port, Time t) {
        //XXX: Esto puede significar q se esta haciendo en el plano de control
        if(message.getSize()==0){
            return owner.send(port, message, owner.getCurrentTime());
        }
        
        if (owner.isOutPortFree(port, message.getWavelengthID(), t)) {
            double messageSize = message.getSize();
            double speed = port.getSwitchingSpeed();
            double linkSpeed = port.getLinkSpeed();

            double sendTime = messageSize / speed;
            
            System.out.println("En Sender:  Puerto: "+port.toString()+" Mensaje: "+message+" Lamda "+message.getWavelengthID() );
            System.out.println(" Tamaño  "+messageSize+" Vel.Comutacion: "+speed+" Vel.Canal: "+linkSpeed  );

            //Calculate the portFreeAgainTime, the time the link will be free again
            Time portFreeAgainTime = new Time(0);
            Time reachingTime = new Time(0);
            if (speed > 0) {
                portFreeAgainTime.addTime(sendTime);
                reachingTime.addTime(messageSize/linkSpeed);
            }
            portFreeAgainTime.addTime(t);
            reachingTime.addTime(t);

            //update linkusage mappings

            Map<Integer, Time> map = owner.getLinkUsage().get(port);
            map.put(new Integer(message.getWavelengthID()), portFreeAgainTime);
            owner.getLinkUsage().put(port, map);

            return owner.send(port, message, reachingTime);
        } else {
            return false;
        }
    }
}
