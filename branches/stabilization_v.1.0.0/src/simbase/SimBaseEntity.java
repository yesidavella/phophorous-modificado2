/*
 * Changelog
 * ---------
 * Version 1.1
 *  - added remove in/out ports
 *   
 */
package simbase;

import simbase.Port.SimBaseInPort;
import simbase.Stats.SimBaseStats;
import java.util.*;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseOutPort;

/**
 * SimBaseEntity objects are the base nodes in the simulator; each one
 * represents an object, capable of interacting with others of its kind (by
 * sending SimBaseMessage messages through the in and out ports).
 * 
 * @version 1.1
 */
public interface SimBaseEntity extends SimBaseStats {

    /**
     * Returns ID
     * 
     * @return ID
     */
    public String getId();

    /**
     * Returns the simulator.
     * @return the simulator
     */
    public SimBaseSimulator getSimulator();

    /**
     * Sets the entity's ID
     * 
     * @param id
     *            the new ID
     */
    public void setID(String id);

    /**
     * Returns the requested stat
     * 
     * @param stat
     *            the stat
     * @return the requested stat
     */
    public double getStat(Stat stat);

    /**
     * Adds an outport to the object
     * 
     * @param p
     *            the outport to be added
     */
    public void addOutPort(SimBaseOutPort p);

    /**
     * Adds an inport to the object
     * 
     * @param p
     *            the inport to be added
     */
    public void addInPort(SimBaseInPort p);

    /**
     * Updates the local time
     * 
     * @param t
     */
    public void updateTime(Time t);

    /**
     * Sends a message to any entity, on its SelfIn port, bypassing the normal
     * links
     * 
     * @param e
     *            the destination
     * @param m
     *            the message
     * @return true if sent; false on error
     */
    public boolean sendNow(SimBaseEntity e, SimBaseMessage m);

    /**
     * Sends a message to any entity, on its SelfIn port, bypassing the normal
     * links
     * 
     * @param e
     *            the destination
     * @param m
     *            the message
     * @param t
     *            the time of sending
     * @return true if sent; false on error
     */
    public boolean sendNow(SimBaseEntity e, SimBaseMessage m, Time t);

    /**
     * Sends a message out through an outport
     * 
     * @param p
     *            the outport through which to send the message
     * @param m
     *            the message to be sent
     * @param t
     *            the time at which the message should arrive at the receiving
     *            end
     * @return whether the sending was successfull or not
     */
    public boolean send(SimBaseOutPort p, SimBaseMessage m, Time t);

    /**
     * Sends a message to itself
     * 
     * @param m
     *            the message
     * @param t
     *            the time of arrival
     * @return whether the sending was successfull or not
     */
    public boolean sendSelf(SimBaseMessage m, Time t);

    /**
     * The receive method which handles incoming messages. Abstract method
     * 
     * @param inPort
     *            the inport on which the message is received
     * @param m
     *            the message
     */
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException;

    /**
     * Returns a list of inports
     * 
     * @return a list of inports
     */
    public ArrayList<SimBaseInPort> getInPorts();

    /**
     * Returns a list of outports
     * 
     * @return a list of outports
     */
    public ArrayList<SimBaseOutPort> getOutPorts();

    /**
     * Reset all stats kept in this entity
     */
    public void resetStats();

    /**
     * Reset all stats for the current batcj kept in this entity
     */
    public void resetBatchStats();

    /**
     * Sets the simulator instance
     * 
     * @param simulator
     *            the simulator instance
     */
    public void setSimulator(SimBaseSimulator simulator);

    /**
     * Returns the loopback in port
     * 
     * @return the loopback in port
     */
    public SimBaseInPort getSelfIn();

    /**
     * Returns the loopback out port
     * 
     * @return the loopback out port
     */
    public SimBaseOutPort getSelfOut();

    /**
     * Inits this entity
     */
    public void init();

    /**
     * Removes all ports.
     * @since 1.1
     */
    public void removePorts();

    /**
     * Hook for finishing calculations at the end of the simulation.
     *
     */
    public void endSimulation();
}
