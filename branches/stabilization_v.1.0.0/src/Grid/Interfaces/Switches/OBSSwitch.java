/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Interfaces.Switches;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public abstract class OBSSwitch extends AbstractSwitch {

    public OBSSwitch(String id, GridSimulator simulator) {
        super(id, simulator);
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);
    }
    

    public boolean supportsOBS() {
        return true;
    }

    public boolean supportsOCS() {
        return false;
    }

    @Override
    public void endSimulation() {
    }

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute,boolean permanent,Time time) {
        simulator.putLog(currentTime, id + " is an OBS Node and cannot request an OCS circuit", Logger.RED, -1, -1);
    }

    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port,Time time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
