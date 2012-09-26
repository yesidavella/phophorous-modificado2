/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simbase.Stop;

import Grid.Entity;
import Grid.Utilities.Config;
import java.util.List;
import simbase.SimBaseEntity;
import simbase.SimBaseSimulator;
import simbase.SimulationInstance;
import simbase.Time;

/**
 *
 * @author Eothein
 */
public class StopperWhenOCSIsOk extends StopEntity {

    private boolean routed = false;

    public StopperWhenOCSIsOk(String id, SimBaseSimulator simulator, Time offset) {
        super(id, simulator, offset);
    }

    @Override
    public boolean checkCondition() {
        if (simulator.getMasterClock().getTime() > 5 && !routed) {
            List<SimBaseEntity> entities = simulator.getEntities();
            for (SimBaseEntity ent : entities) {
                ((Entity) ent).route();
            }
            routed = true;
            simulator.resetAllStats();
            return false;
        } else {
            if (simulator.getMasterClock().getTime() >=
                    SimulationInstance.configuration.getDoubleProperty(Config.ConfigEnum.simulationTime)) {
                return true;
            } else {

                return false;
            }
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
