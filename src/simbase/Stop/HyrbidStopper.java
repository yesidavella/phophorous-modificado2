/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simbase.Stop;

import Grid.Interfaces.Switch;
import Grid.Interfaces.Switch;
import Grid.Utilities.Config;
import java.util.ArrayList;
import java.util.Iterator;
import simbase.SimBaseSimulator;
import simbase.SimulationInstance;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class HyrbidStopper extends StopEntity {



    public HyrbidStopper(String id, SimBaseSimulator simulator, Time offset) {
        super(id, simulator, offset);
    }

    @Override
    public boolean checkCondition() {
        double failings = 1000;
        ArrayList list = simulator.getEntitiesOfType(Switch.class);
        if (returnFails() >=
                failings) {

            Iterator it = list.iterator();
            while (it.hasNext()) {
                Switch sw = (Switch) it.next();
                double fail_resultMessage = simulator.getStat(sw, Stat.SWITCH_JOBRESULTMESSAGE_DROPPED);
                double switchedMessages = simulator.getStat(sw, Stat.SWITCH_JOBMESSAGE_SWITCHED);
                double droppedMessages = simulator.getStat(sw, Stat.SWITCH_JOBMESSAGE_DROPPED);
                
                double totalMessages = switchedMessages + droppedMessages;
                System.out.println(sw.getId());
                System.out.println(fail_resultMessage);
                System.out.println(switchedMessages);
                System.out.println(droppedMessages);
                System.out.println(totalMessages);
            }
            return true;

        } else {
            return false;
        }

    }

    private double returnFails() {
        return simulator.getStat(Stat.SWITCH_MESSAGE_DROPPED);
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
