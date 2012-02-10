/*
 * Changelog
 * ---------
 * Version 1.1
 *  - added remove in/out ports
 *   
 */
package simbase;

import Grid.Port.GridOutPort;
import java.io.Serializable;
import simbase.Port.SimBaseInPort;
import java.util.*;
import simbase.Exceptions.StopException;
import simbase.Exceptions.TimeException;
import simbase.Port.SimBaseOutPort;

/**
 * SimBaseEntity objects are the base nodes in the simulator; each one
 * represents an object, capable of interacting with others of its kind (by
 * sending SimBaseMessage messages through the in and out ports).
 * 
 * @version 1.1
 */
public abstract class SimBaseEntityImpl implements SimBaseEntity, Comparable, Serializable{

    /**
     * Entity ID
     */
    protected String id = "";
    /**
     * The current time
     */
    protected Time currentTime = new Time(0);
    /**
     * List of inports of the object
     */
    protected ArrayList<SimBaseInPort> inPorts = new ArrayList<SimBaseInPort>();
    /**
     * List of outports of the object
     */
    protected ArrayList<SimBaseOutPort> outPorts = new ArrayList<SimBaseOutPort>();
    /**
     * The loopback out port
     */
    protected SimBaseOutPort selfOut = null;
    /**
     * The loopback in port
     */
    protected SimBaseInPort selfIn = null;
    /**
     * The simulator in which this object is used
     */
    protected SimBaseSimulator simulator = null;

    public SimBaseSimulator getSimulator() {
        return simulator;
    }
    /**
     * Is this entity inited
     */
    protected boolean inited = false;
    /**
     * Is this entity registered
     */
    protected boolean registered = false;

    /**
     * Constructor
     * 
     * @param id
     *            entity ID
     * @param simulator
     *            the associated simulator
     */
    public SimBaseEntityImpl(String id, SimBaseSimulator simulator) {
        this.id = id;
        this.simulator = simulator;

        selfOut = new SimBaseOutPort(id + "-self_out", this);
        selfIn = new SimBaseInPort(id + "-self_in", this);
        selfOut.setTarget(selfIn);
        selfIn.setSource(selfOut);
    }

    /**
     * Returns ID
     * 
     * @return ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the entity's ID
     * 
     * @param id
     *            the new ID
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Adds an outport to the object
     * 
     * @param p
     *            the outport to be added
     */
    public void addOutPort(SimBaseOutPort p) {
        outPorts.add(p);
    }

    /**
     * Adds an inport to the object
     * 
     * @param p
     *            the inport to be added
     */
    public void addInPort(SimBaseInPort p) {
        inPorts.add(p);
    }

    /**
     * Updates the local time
     * 
     * @param t
     */
    public void updateTime(Time t) {
        currentTime.setTime(t);
    }

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
    public boolean sendNow(SimBaseEntity e, SimBaseMessage m) {
        try {
            if (simulator.entities.contains(e)) {
                simulator.addEvent(e.getSelfIn(), m, currentTime);
                return true;
            }
            return false;
        } catch (TimeException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

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
     * 
     * @return true if sent; false on error
     */
    public boolean sendNow(SimBaseEntity e, SimBaseMessage m, Time t) {
        try {
            if (simulator.entities.contains(e)) {
                simulator.addEvent(e.getSelfIn(), m, t);
               
                return true;
            }
            return false;
        } catch (TimeException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Will send out a message on a given outgoing port.
     * 
     * @param p
     *            the {@link GridEntityOutPort} on which to send out the message
     * @param m
     *            the outgoing message
     * @return true if sending succeeded, false if an error occured
     */
    public boolean send(SimBaseOutPort p, SimBaseMessage m) {
        return send(p, m, currentTime);
    }

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
    public boolean send(SimBaseOutPort p, SimBaseMessage m, Time t) {
        try {
            simulator.addEvent(p, m, t); // absolute time!
            return true;
        } catch (TimeException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Sends a message to itself
     * 
     * @param m
     *            the message
     * @param t
     *            the time of arrival
     * @return whether the sending was successfull or not
     */
    public boolean sendSelf(SimBaseMessage m, Time t) {
        send(selfOut, m, t);
        return true;
    }

    /**
     * The receive method which handles incoming messages. Abstract method
     * 
     * @param inPort
     *            the inport on which the message is received
     * @param m
     *            the message
     */
    public abstract void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException;

    /**
     * Return a string representation.
     * @return A string representation. 
     */
    public String toString() {
        String result = "<name>" + id + "/<name>";
        result += "<inputports>";
        Iterator i = inPorts.iterator();
        while (i.hasNext()) {
            result += "<inputport>" + ((SimBaseInPort) i.next()) + "</inputport>";
        }
        result += "</inputports><outputports>";
        Iterator j = outPorts.iterator();
        while (j.hasNext()) {
            result += "<outputport>" + ((GridOutPort) j.next()) + "</outputport>";
        }
        result += "</outputports>";
        return id;
    }

    /**
     * Returns a list of inports
     * 
     * @return a list of inports
     */
    public ArrayList<SimBaseInPort> getInPorts() {
        return inPorts;
    }

    /**
     * Returns a list of outports
     * 
     * @return a list of outports
     */
    public ArrayList<SimBaseOutPort> getOutPorts() {
        return outPorts;
    }

    /**
     * Reset all stats kept in this entity
     */
    public void resetStats() {
    }

    /**
     * Reset all stats for the current batcj kept in this entity
     */
    public void resetBatchStats() {
    }

    public void setSimulator(SimBaseSimulator simulator) {
        this.simulator = simulator;
        simulator.register(this);
    }

    public SimBaseInPort getSelfIn() {
        return selfIn;
    }

    public SimBaseOutPort getSelfOut() {
        return selfOut;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public double getStat(Stat stat) {
        return simulator.getStat(this, stat);
    }

    public void removePorts() {
        inPorts = new ArrayList<SimBaseInPort>();
        outPorts = new ArrayList<SimBaseOutPort>();
    }

    /**
     * Initialises this entity.
     */
    public abstract void init();

    /**
     * Method which is called when the simulation ends. This can be helpfull
     * if you want somehething done just at the end of the simulation.
     */
    public abstract void endSimulation();

    /**
     * Comares to SimBaseEntitys
     * @param o The SimbaseEntity to compare with
     * @return -1,0,+1
     */
    public int compareTo(Object o) {
        return this.id.compareTo(((SimBaseEntityImpl) o).getId());
    }

    public Time getCurrentTime() {
        return currentTime;
    }

    public boolean isInited() {
        return inited;
    }

    public boolean isRegistered() {
        return registered;
    }
}
