/*
 * This stopper wil create an OCS circuit in the simulation on time 10.
 */

package simbase.Stop;

import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import simbase.SimBaseSimulator;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OCSSetupFailStopper extends StopEntity{

    private boolean done = false;
    
    public OCSSetupFailStopper(String id, SimBaseSimulator simulator, Time offset) {
        super(id, simulator, offset);
    }
    
    
    
    @Override
    public boolean checkCondition() {
        if (simulator.getMasterClock().getTime() > 10 && !done) {
            ResourceNode res = (ResourceNode)simulator.getEntityWithId("resource");
            ClientNode client = (ClientNode)simulator.getEntityWithId("client");
            Grid.Utilities.Util.createOCSCircuit(res, client, (GridSimulator)simulator,true);
            done = true;
            return false;
        } else {
            if (simulator.getMasterClock().getTime() > 100) {
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
