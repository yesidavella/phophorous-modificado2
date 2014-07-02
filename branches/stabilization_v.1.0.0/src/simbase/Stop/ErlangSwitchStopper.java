/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simbase.Stop;

import Grid.GridSimulator;
import Grid.Interfaces.Switch;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class ErlangSwitchStopper extends StopEntity {

    private double neededFail;
    private Switch sw;

    public ErlangSwitchStopper(String id, GridSimulator simulator, Time offset, String switchName, int neededFail){
        super(id, simulator, offset);
        sw = (Switch) simulator.getEntityWithId(switchName);
        if(sw == null){
            throw new NullPointerException("The entity does not exist : " + switchName);
        }
        this.neededFail =neededFail;
    }


    @Override
    public boolean checkCondition() {
       
        double fail = simulator.getStat(sw, Stat.SWITCH_MESSAGE_DROPPED);
        if (fail >= neededFail) {
            return true;
//        } else if (simulator.getMasterClock().getTime() >=
//                SimulationInstance.configuration.getDoubleProperty(Config.ConfigEnum.simulationTime)) {
//            return true;
        } else {
            return false;
        }
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

