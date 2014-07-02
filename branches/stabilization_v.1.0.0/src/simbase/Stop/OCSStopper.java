/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simbase.Stop;

import Grid.GridSimulator;
import Grid.Interfaces.Switches.OCSSwitch;
import Grid.Nodes.OCS.OCSClientNodeImpl;
import Grid.Nodes.OCS.OCSResourceNodeImpl;
import Grid.Port.GridOutPort;
import simbase.SimBaseSimulator;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class OCSStopper extends StopEntity {

    private boolean ocsTeardownDone = false;

    public OCSStopper(String id, GridSimulator simulator, Time offset) {
        super(id, simulator, offset);
    }

    @Override
    public boolean checkCondition() {
        if (simulator.getMasterClock().getTime() > 10 && !ocsTeardownDone) {
            OCSClientNodeImpl client = (OCSClientNodeImpl) simulator.getEntityWithId("client");
            OCSSwitch sw1 = (OCSSwitch) simulator.getEntityWithId("switch1");
            GridOutPort port = client.findOutPort(sw1, 0);
            OCSResourceNodeImpl resource = (OCSResourceNodeImpl) simulator.getEntityWithId("resource");
            client.teardDownOCSCircuit(resource, 0, port,simulator.getMasterClock());
            ocsTeardownDone = true;
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
