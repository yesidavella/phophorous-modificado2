/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simbase.Stop;

import Grid.Interfaces.ResourceNode;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.SimBaseSimulator;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class ErlangStopper extends StopEntity {

    private double neededFail = 100000;
    private ResourceNode resource;

    public ErlangStopper(String id, SimBaseSimulator simulator, Time offset, double neededFail, ResourceNode resource) {
        super(id, simulator, offset);
        this.neededFail = neededFail;
        this.resource = resource;
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);
        
        double fail = simulator.getStat(resource, Stat.RESOURCE_FAIL_NO_FREE_PLACE);
        //simulator.putLogImmediately(currentTime, (fail / neededFail) * 100 + "%");
    }

    @Override
    public boolean checkCondition() {
        double fail = simulator.getStat(resource, Stat.RESOURCE_FAIL_NO_FREE_PLACE);
        if (fail >= neededFail) {
            return true;
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
