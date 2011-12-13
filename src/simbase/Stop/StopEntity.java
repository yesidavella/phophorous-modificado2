/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simbase.Stop;

import simbase.*;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.Stats.Logger;

/**
 *
 * @author Jens Buysse
 */
public abstract class StopEntity extends SimBaseEntityImpl {

    /**
     * The offset between two consecutive stopevents.
     */
    protected Time offset;

    /**
     * Constructors
     * @param id The id of this entity.
     * @param simulator The simulator
     * @param offset The time of creation.
     */
    public StopEntity(String id, SimBaseSimulator simulator, Time offset) {
        super(id, simulator);
        simulator.putLog(simulator.getMasterClock(), id+ " succesfully created", Logger.BLACK, 0, 0);
        this.offset = offset;
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        if (checkCondition()) {
            simulator.putLog(currentTime, "events still in queue : " + simulator.getEvents().size(),Logger.BLACK,0,0);        
            throw new StopException("The simulation can stop, criteria reached");
        } else {
            Time newStopEventTime = (simulator.getMasterClock()).addTime(offset);
            sendSelf(m, newStopEventTime);
            StringBuffer buffer = new StringBuffer("New stop event update inserted. New check : ");
            buffer.append(newStopEventTime);
            System.gc();
            simulator.putLog(simulator.getMasterClock(), buffer.toString(),Logger.BLACK,0,0);
        }
    }

    /**
     * Return true if the simulation can stop, false if it has to continue.
     * 
     * @return True if simulation can end, false if it has to continue.
     */
    public abstract boolean checkCondition();
}
