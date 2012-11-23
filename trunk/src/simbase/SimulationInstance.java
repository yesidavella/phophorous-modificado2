package simbase;

import Grid.Utilities.Config;
import simbase.Stop.StopMessage;
import simbase.Stop.StopEvent;
import simbase.Stop.StopEntity;
import simbase.Stats.DefaultPrinter;
import simbase.Stats.Printer;

import Grid.Utilities.Units;
import java.io.Serializable;
import simbase.Exceptions.StopException;
import simbase.Exceptions.TimeException;
import simbase.Port.SimBaseInPort;
import simbase.Port.SimBaseOutPort;
import simbase.Stats.Logger;
import simbase.Stop.TimeStopper;

/**
 * A SimulationInstance is an instance of a simulation. To run a simulation it
 * is desired to subclass this. It provides the basic functionality for running
 * a simulation. Hooks for subclassing are provided.
 * 
 * @author Joachim Vermeir
 * @version 1.0
 */
public class SimulationInstance implements Units, Serializable {

    /**
     * The simulator
     */
    protected SimBaseSimulator simulator;
    /**
     * The configuration file.
     */
    public static Config configuration;
    /**
     * The total event count
     */
    protected long eventCount = 0;
    /**
     * The end time for the simulation
     */
    protected Time endTime;
    /**
     * The printer to print the results of the simulation
     */
    protected Printer printer = new DefaultPrinter();
    /**
     * The entity which is responsible for stopping the simulator.
     */
    protected StopEntity stopEntity;

    /**
     * Runs the simulation
     * 
     */
    public boolean stopEvent= false; 
    public void run() {
        initialiseStopEvent();
        try {
            while (!stopEvent && simulator.runNextEvent()) {
                eventCount++;
            }
        } catch (StopException e) {
            
            System.out.println("Cantidad de ocs´s vivos al final de la simulacion:"+((Grid.GridSimulator)simulator).getEstablishedCircuits().size());
            simulator.putLog(simulator.getMasterClock(), e.getMessage(),Logger.BLACK,0,0);
            simulator.putLogClose(simulator.getMasterClock(), "Simulation finished",Logger.BLACK,0,0);  
        }
    }

    public StopEntity getStopEntity() {
        return stopEntity;
    }

    public void setStopEntity(StopEntity stopEntity) {
        this.stopEntity = stopEntity;
    }
    
    

    protected void printInformation() {
        printer.printInformation(simulator);
    }

    public void initialiseStopEvent() {
        try {
            if (stopEntity == null) {
                stopEntity = new TimeStopper("STOPENTITY", simulator,
                        new Time(SimulationInstance.configuration.getDoubleProperty(Config.ConfigEnum.stopEventOffSetTime)));
            }
            StopMessage message = new StopMessage("STOPMESSAGE");
            StopEvent stopEvent = new StopEvent(message, stopEntity);
            SimBaseInPort stopInPort = new SimBaseInPort("STOPINPORT", stopEntity);
            SimBaseOutPort stopOutPort = new SimBaseOutPort("STOPOUTPORT", stopEntity);
            stopEvent.setSource(stopOutPort);
            stopEvent.setTarget(stopInPort);
            simulator.addStopEvent(stopEvent, new Time(0));
        } catch (TimeException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Hook for initializing all entities. Here all entities are started once
     * all configuration is done.
     */
    protected void initEntities() {
        simulator.initEntities();
    }

    /**
     * Hook for finishing all entities at the end of the simulation. Here all entities are started once
     * all configuration is done.
     */
    protected void finishEntities() {
        simulator.finishEntities();
    }

    /**
     * Sets the simulator.
     * @param simulator
     */
    public void setSimulator(SimBaseSimulator simulator) {
        this.simulator = simulator;
    }

    public SimBaseSimulator getSimulator() {
        return simulator;
    }
    
}
