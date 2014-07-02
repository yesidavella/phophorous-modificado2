/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simbase.Stop;

import Grid.Interfaces.ServiceNode;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.SimBaseSimulator;
import simbase.Stats.SimBaseStats.Stat;
import simbase.Time;

/**
 *
 * This Stopper will stop when a certain number of jobs have been send or when 
 * the simulator reaches the time 1E10.
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class LoadStopper extends StopEntity {

    /**
     * The total number of jobs that need to be send.
     */
    private double jobs_that_need_to_be_send = 5000000;
    /**
     * The Broker of the simulator.
     */
    private ServiceNode broker;

    /**
     * Constructor. Will construct this stopper.
     * @param id The id of the stopper.
     * @param simulator The simulator for which this stopper is responsible.
     * @param offset The offset time for this stopper.
     * @param neededJobs The total number of jobs that need to be send.
     * @param broker The broker for this simulation.
     */
    public LoadStopper(String id, SimBaseSimulator simulator, Time offset, double neededJobs, ServiceNode broker) {
        super(id, simulator, offset);
        this.jobs_that_need_to_be_send = neededJobs;
        this.broker = broker;
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);

    //double jobs_send = simulator.getStat(broker,Stat.SERVICENODE_NO_FREE_RESOURCE );
    //simulator.putLogImmediately(currentTime, (jobs_send / jobs_that_need_to_be_send) * 100 + "% " + (jobs_send));
    }

    @Override
    public boolean checkCondition() {
        double jobs_send = simulator.getStat(Stat.CLIENT_JOB_SENT);
        if (jobs_send >= jobs_that_need_to_be_send || simulator.getMasterClock().getTime() >= 1E10 ) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported");
    }
}
