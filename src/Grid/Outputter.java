/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid;

import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.Switch;
import java.io.PrintStream;
import simbase.Stats.SimBaseStats.Stat;

/**
 *
 * @author Eothein
 */
public class Outputter {

    /**
     * The outstream to write to.
     */
    protected PrintStream out;
    /**
     * The GridSimulator
     */
    protected  GridSimulator sim;
    /**
     * The number of asterisx legnth
     */
    public static final int ASTERISKLENGTH = 10;

    /**
     * Constructor
     * @param out The outstream to write to.
     */
    public Outputter(PrintStream out) {
        this.out = out;
    }

    /**
     * Defaultconstructor, will take System.out als defaults Printstream
     * @param sim The Gridsimulator which contains the information.
     */
    public Outputter(GridSimulator sim) {
        out = System.out;
        this.sim = sim;
    }

    public Outputter(PrintStream out, GridSimulator sim) {
        this.out = out;
        this.sim = sim;
    }
    
    

    /**
     * Prints the req sent, the number of jobs send, the number of jobs which failed to send
     * and the number of results received.
     * @param client The client to output;
     */
    public void printClient(ClientNode client) {
        out.println(returnStringWithAsterix(client));
        out.println("CLIENT_REQ_SENT \t: " + sim.getStat(client, Stat.CLIENT_REQ_SENT));
        out.println("CLIENT_JOB_SENT \t: " + sim.getStat(client, Stat.CLIENT_JOB_SENT));
        out.println("CLIENT_RESULTS_RECEIVED \t: " + sim.getStat(client, Stat.CLIENT_RESULTS_RECEIVED));
        out.println("CLIENT_SENDING_FAILED \t: " + sim.getStat(client, Stat.CLIENT_SENDING_FAILED));
        out.println("%  CLIENT_RESULTS_RECEIVED   \t: " + sim.getStat(client, Stat.CLIENT_RESULTS_RECEIVED) / sim.getStat(client, Stat.CLIENT_JOB_SENT));
    }

    /**
     * Print the number of jobs received, the number of jobs executed, the number of 
     * results which failed to send.
     * @param res
     */
    public void printResource(ResourceNode res) {
        out.println(returnStringWithAsterix(res));
        out.println("RESOURCE_JOB_RECEIVED \t: " + sim.getStat(res, Stat.RESOURCE_JOB_RECEIVED));
        out.println("RESOURCE_FAIL_NO_FREE_PLACE \t: " + sim.getStat(res, Stat.RESOURCE_FAIL_NO_FREE_PLACE));
        out.println("RESOURCE_SENDING_FAILED \t: " + sim.getStat(res, Stat.RESOURCE_SENDING_FAILED));

    }

    /**
     * Prints the number of job messages switched and dropped, and the number of 
     * resultmessages switched and dropped.
     * @param sw The switcht to output.
     */
    public void printSwitch(Switch sw) {
        out.println(returnStringWithAsterix(sw));
        out.println("SWITCH_JOBMESSAGE_SWITCHED \t: " + sim.getStat(sw, Stat.SWITCH_JOBMESSAGE_SWITCHED));
        out.println("SWITCH_JOBMESSAGE_DROPPED \t: " + sim.getStat(sw, Stat.SWITCH_JOBMESSAGE_DROPPED));
        out.println("SWITCH_JOBRESULTMESSAGE_SWITCHED \t: " + sim.getStat(sw, Stat.SWITCH_JOBRESULTMESSAGE_SWITCHED));
        out.println("SWITCH_JOBRESULTMESSAGE_DROPPED \t: " + sim.getStat(sw, Stat.SWITCH_JOBRESULTMESSAGE_DROPPED));
        double fail_resultMessage = sim.getStat(sw, Stat.SWITCH_JOBRESULTMESSAGE_DROPPED);
        double switchedResultMessages = sim.getStat(sw, Stat.SWITCH_JOBRESULTMESSAGE_SWITCHED);
        double resultRelative = fail_resultMessage / (fail_resultMessage + switchedResultMessages);

        double switchedJobMessages = sim.getStat(sw, Stat.SWITCH_JOBMESSAGE_SWITCHED);
        double droppedJobMessages = sim.getStat(sw, Stat.SWITCH_JOBMESSAGE_DROPPED);
        double jobRelative = droppedJobMessages / (switchedJobMessages + droppedJobMessages);

        double messagesDropped = sim.getStat(sw, Stat.SWITCH_MESSAGE_DROPPED);
        double messagesSwitched = sim.getStat(sw, Stat.SWITCH_MESSAGE_SWITCHED);
        double relative = messagesDropped / (messagesDropped + messagesSwitched);
        out.println("Rel Drop Job \t:"+jobRelative);
        out.println("Rel Drop Res \t:"+resultRelative);
        out.println("Rel tot Drop\t:" + relative);
    }
    
    


    protected  String returnStringWithAsterix(Entity ent) {
        int entLength = (ASTERISKLENGTH / 2) - (ent.getId().length() / 2);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < entLength; i++) {
            buffer.append("*");
        }
        buffer.append(ent.getId());
        for (int i = 0; i < entLength; i++) {
            buffer.append("*");
        }
        return buffer.toString();

    }
}
