/*
 * This test test whether the swithing capabilities of the OBS Switch comply with
 * the ErlangB analysis.
 */
package Grid.Nodes;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.JobRequestMessage;
import Grid.Interfaces.ServiceNode;
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
public class DummyServiceNode extends ServiceNode {

    public DummyServiceNode(String id, GridSimulator gridSim) {
        super(id, gridSim);

    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    protected void handleJobRequestMessage(SimBaseInPort inPort, JobRequestMessage msg) {
        simulator.putLog(currentTime, "Message received", Logger.BLACK, 0, 0);
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);
        if (m instanceof JobRequestMessage) {
            handleJobRequestMessage(inPort, (JobRequestMessage) m);
        } else {
            return;
        }
    }

    @Override
    public boolean supportsOBS() {
        return true;
    }

    @Override
    public boolean supportsOCS() {
        return false;
    }

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute,boolean permanent,Time time) {
        simulator.putLog(currentTime, id + " is an OBS Node and cannot request an OCS circuit", Logger.RED, -1, -1);
    }

    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port,Time time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void route() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
