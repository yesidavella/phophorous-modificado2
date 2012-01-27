package simbase;

import Grid.Utilities.Config;
import simbase.Stop.StopEvent;
import simbase.Port.SimBaseInPort;
import simbase.Port.SimBaseOutPort;
import simbase.Port.SimBasePort;
import simbase.Stats.Logger;
import simbase.Stats.SimBaseStats;
import simbase.Stats.StatProvider;
import java.util.*;

import cern.jet.random.engine.*;
import java.text.DecimalFormat;
import simbase.Exceptions.StopException;
import simbase.Exceptions.TimeException;

/**
 * The base simulator class. Responsible for keeping a sorted list of events,
 * and executing them. Keeps track of the layout of the simulation (entities and
 * their interaction), and statistics.
 * 
 * @version 1.3
 */
public class SimBaseSimulator implements SimBaseStats {

    /**
     * The maximum number of events in the queue.
     */
    protected long maxEventCount = 0;
    /**
     * The stats tracker
     * 
     * @since 1.2
     */
    private StatProvider stats = new StatProvider();
    /**
     * Stats for the current batch
     * 
     * @since 1.2
     */
    private StatProvider batchStats = new StatProvider();
    /**
     * The list of events
     */
    protected  TreeSet<SimBaseEvent> events = new TreeSet<SimBaseEvent>();
    /**
     * The master clock of the simulation
     */
    protected  Time masterClock = new Time(0);
    /**
     * The number of events processed
     */
    protected long eventCount = 0;
    /**
     * Entities in the simulation
     */
    protected ArrayList<SimBaseEntity> entities = new ArrayList<SimBaseEntity>();
    /**
     * The random generator
     */
    //private MersenneTwister engine = new MersenneTwister(new Date(System.currentTimeMillis()));
    
    private MersenneTwister engine = new MersenneTwister((int)System.currentTimeMillis());
    /**
     * The logger
     * 
     */
    protected Logger logger;
    /**
     * Formatter for the decimals.
     */
    private DecimalFormat format = new DecimalFormat();

    /* **************************************************************** */
    /* EVENT HANDLING */
    /* **************************************************************** */
    /**
     * Adds a new event to the event queue
     * 
     * @param port
     *            the port
     * @param m
     *            the message
     * @param t
     *            the time of arrival at the receiving end
     */
    public void addEvent(SimBasePort port, SimBaseMessage m, Time t) throws TimeException {
        if (t.getTime() >= masterClock.getTime()) {
            SimBaseEvent e = new SimBaseEvent(port, m, t);
            events.add(e);
        } else {
            throw new TimeException("Events cannot be added before the masterclock. Time: " + masterClock);

        }
    }

    /**
     * Adds a stopevent to the queue.
     * @param stop The stopEvent to add.
     * @param t The time when the stopEvent has to be executed.
     * 
     * @throws simbase.Exceptions.TimeException
     */
    public void addStopEvent(StopEvent stop, Time t) throws TimeException {
        if (t.getTime() >= masterClock.getTime()) {
            stop.setTime(t);
            events.add(stop);
        } else {
            throw new TimeException("Events cannot be added before the masterclock. Time: " + masterClock);

        }
    }

    /**
     * Executes the next event in the queue. Will return true if executed, false
     * if there was no next event
     * 
     * @return true if the event was executed, false if there was no event
     */
    public boolean runNextEvent() throws StopException {
        if (events.isEmpty()) {
            throw new StopException("There are no events to execute anymore");
        }
        eventCount++;
        SimBaseEvent nextEvent = events.first();
        events.remove(nextEvent);
        masterClock = nextEvent.getTime();
        nextEvent.getTarget().getOwner().updateTime(masterClock);
        SimBaseInPort port = nextEvent.getTarget();
        SimBaseMessage msg = nextEvent.getMessage();
        SimBaseEntity entity = nextEvent.getTarget().getOwner();
        entity.receive(port, msg);
        //System.out.println("Tiempo: "+masterClock.getTime());
        return true;

    }

    /**
     * Prints out the event queue
     */
    public void printEvents() {
        if (!events.isEmpty()) {
            SimBaseEvent nextEvent;
            Iterator event = events.iterator();
            while (event.hasNext()) {
                nextEvent = (SimBaseEvent) event.next();
                System.out.println(nextEvent);
            }
        }
    }

    /* **************************************************************** */
    /* GETTERS / SETTERS */
    /* **************************************************************** */
    /**
     * Returns the event count
     * 
     * @return the event count.
     */
    public long getEventCount() {

        return eventCount;
    }

    /**
     * Return the treeset with the events.
     * 
     * @return The TreeSet with the events.
     */
    public TreeSet<SimBaseEvent> getEvents() {
        return events;
    }

    /**
     * Returns the maximum number of simultaneous events in the eventlist.
     * 
     * @return the maximum number of simultaneous events in the eventlist.
     */
    public long getMaxEventCount() {
        return maxEventCount;
    }

    /**
     * Returns the master clock; this is the time in the simulation
     * 
     * @return the master clock
     */
    public Time getMasterClock() {
        return new Time(masterClock);
    }

    /**
     * Returns the random engine
     * 
     * @return the random engine
     */
    public MersenneTwister getEngine() {
        return engine;
    }

    /**
     * Sets a new MersenneTwister.
     * 
     * @param engine
     *            the new MersenneTwister
     * @since 1.1
     */
    public void setEngine(MersenneTwister engine) {
        this.engine = engine;
    }

    public ArrayList<SimBaseEntity> getEntities() {
        return entities;
    }

    /**
     * Returns all the entities of the given class
     * 
     * @param type
     *            the class the entities have to belong to
     * @return all the entities of the given class
     * @since 1.2
     */
    public ArrayList<SimBaseEntity> getEntitiesOfType(Class type) {
        ArrayList<SimBaseEntity> res = new ArrayList<SimBaseEntity>();
        for (SimBaseEntity entity : entities) {
            if (type.isInstance(entity)) {
                res.add(entity);
            }
        }
        return res;
    }

    /**
     * Returns the entity with the given ID
     * 
     * @param id
     *            the ID to search for
     * @return the entity with the given ID
     * @since 1.2
     */
    public SimBaseEntity getEntityWithId(String id) {
        for (SimBaseEntity entity : entities) {
            if (entity.getId().equals(id)) {
                return entity;
            }
        }
        return null;

    }

    /**
     * Resets the whole simulation
     */
    public void resetSimulation() {
        resetAllStats();
        events.clear();
        masterClock = new Time(0);
        System.gc();
    }


    /* **************************************************************** */
    /* LOGGING */
    /* **************************************************************** */
    /**
     * Logs an event
     * 
     * @param time
     *            the time the event occurs
     * @param log
     *            the event description to log
     * @param color 
     *            the color of this message
     */
    
    public void putLogClose(Time time, String log, int color, double size, int wavelength) {
                 
         putLog(time, log, color, size, wavelength);
         logger.close();
    }
    
    public void putLog(Time time, String log, int color, double size, int wavelength) {
        if (SimulationInstance.configuration.getBooleanProperty(Config.ConfigEnum.output)) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<FONT COLOR=");
            switch (color) {
                case Logger.BLACK:
                    buffer.append("BLACK");
                    break;
                case Logger.BLUE:
                    buffer.append("BLUE");
                    break;
                case Logger.GREEN:
                    buffer.append("GREEN");
                    break;
                case Logger.ORANGE:
                    buffer.append("ORANGE");
                    break;
                case Logger.YELLOW:
                    buffer.append("YELLOW");
                    break;
                case Logger.RED:
                    buffer.append("RED");
                    break;
                case Logger.BROWN:
                    buffer.append("BROWN");
                    break;
            }
            buffer.append(">");
            buffer.append(log);
            buffer.append(" (");
            buffer.append(wavelength);
            buffer.append(")");
            buffer.append(" (");
            buffer.append(format.format(size));
            buffer.append(")");

            buffer.append("</FONT>");
            logger.putLog(time, buffer.toString());
            
           
               
            
        }

    }

    /**
     * Immediately logs an events, no matter if ouput was choses no to be shown.
     * 
     * @param time
     *            the time the event occurs
     * @param log
     *            the event description to log
     */
    public void putLogImmediately(Time time, String log) {
        logger.logToStdOutput(time, log);
    }

    /* **************************************************************** */
    /* STATS HANDLING */
    /* **************************************************************** */
    /**
     * Update the stats
     * 
     * @param statID
     *            the ID of the stat to be updated
     * 
     * @deprecated use {@link #addStat(SimBaseEntity, simbase.SimBaseStats.Stat)} instead
     */
    public void addStat(Stat statID) {
        stats.addStat(statID);
        batchStats.addStat(statID);
    }

    /**
     * Update the stats
     * 
     * @param entity
     *            the entity to inspect
     * @param statID
     *            the ID of the stat to be updated
     * @param add
     *            amount to be added (defaults to 1)
     */
    public void addStat(SimBaseEntity entity, Stat statID, double add) {
        stats.addStat(entity, statID, add);
        batchStats.addStat(entity, statID, add);
    }

    /**
     * Update the stats
     * 
     * @param entity
     *            the entity to inspect
     * @param statID
     *            the ID of the stat to be updated
     */
    public void addStat(SimBaseEntity entity, Stat statID) {
        stats.addStat(entity, statID);
        batchStats.addStat(entity, statID);
    }

    /**
     * Returns a string containing all stats
     * 
     * @param separator
     *            seperator character(s) between the stats
     * @return the list of all stats
     */
    public String getAllStats(String separator) {
        String res = "";
        double[] stats = this.stats.getStats();
        for (int i = 0; i < stats.length; i++) {
            res += stats[i] + separator;
        }

        return res.trim();
    }

    /**
     * Returns a string containing all stats
     * 
     * @param entity
     *            the entity to inspect
     * @param separator
     *            seperator character(s) between the stats
     * @return the list of all stats
     */
    public String getAllStats(SimBaseEntity entity, String separator) {
        String res = "";
        double[] stats = this.stats.getStats(entity);
        for (int i = 0; i < stats.length; i++) {
            res += stats[i] + separator;
        }

        return res.trim();
    }

    /**
     * Returns a given stat
     * 
     * @param stat
     *            the ID of the stat
     * @return the stat count
     */
    public double getStat(Stat stat) {
        return stats.getStat(stat);
    }

    /**
     * Returns a given stat, in the current batch
     * 
     * @param stat
     *            the ID of the stat
     * @return the stat count in the current batch
     */
    public double getBatchStats(Stat stat) {
        return batchStats.getStat(stat);
    }

    /**
     * Resets all stats.
     * 
     * @since 1.1
     */
    public void resetAllStats() {
        stats.reset();
    }

    /**
     * Returns the value of the given stat and entity
     * 
     * @param entity
     *            the entity to inspect
     * @param stat
     *            the stat to retrieve
     * @return the value of the given stat and entity
     * @since 1.2
     */
    public double getStat(SimBaseEntity entity, Stat stat) {
        return stats.getStat(entity, stat);
    }

    /* **************************************************************** */
    /* OTHER */
    /* **************************************************************** */
    /**
     * Registers a SimBaseEntity with the simulator
     * 
     * @param entity
     *            the entity to be registered
     */
    public void register(SimBaseEntity entity) {
        entities.add(entity);
    }

    public void setEntities(ArrayList<SimBaseEntity> entities) {
        this.entities = entities;
    }
    

    /**
     * Calls the init method on every entity.
     */
    public void initEntities() {
         format.setMaximumFractionDigits(3);
        for (SimBaseEntity ent : entities) {
          try{
            ent.init();
          }
          catch(Exception e)
          {
              System.out.print(e.toString());
          }  
        }
    }

    /**
     * Calls the init method on every entity.
     */
    public void finishEntities() {

        for (SimBaseEntity ent : entities) {
            ent.endSimulation();
        }
    }

    /**
     * Replaces an entity by another entity. Removes the ports from the old entity and places them on the new one.
     * @param old the entity to replace
     * @param replace the new entity
     * @since 1.3
     */
    public void replace(SimBaseEntity old, SimBaseEntity replace) {

        for (SimBaseOutPort port : old.getOutPorts()) {
            replace.addOutPort(port);
            port.setOwner(replace);
        }
        for (SimBaseInPort port : old.getInPorts()) {
            replace.addInPort(port);
            port.setOwner(replace);
        }

        old.removePorts();
    }

    /**
     * Sets the finish time of the simulator.
     * @param simulationTime the finish time
     */
    public void setFinishedTime(Time simulationTime) {
        masterClock = new Time(simulationTime);
    }

    /**
     * Adds an entity to the simulation.
     * 
     * @param entity The new entity to add to the simulation.
     */
    public void addEntity(SimBaseEntity entity) {
        //if(!entities.contains(entity))
        entities.add(entity);
    }
     public void unRegister(SimBaseEntity entity) 
    {
        entities.remove(entity);
    }
}
