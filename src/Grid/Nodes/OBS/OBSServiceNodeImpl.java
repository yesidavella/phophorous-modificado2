/*
 * The ServiceNode contains the scheduling algorithms for the grid. Incoming job
 * requests are processed, and a resource is chosen for executing the job,
 * providing the data, and storing the results.  While the ServiceNode will
 * always pick one resource only, using anycast algorithms is possible by using
 * one global resource which is connected to all switches that normally have a
 * resource connected: routing algorithms will then take care of choosing the
 * closest/best/… resource to execute the job.
 */
package Grid.Nodes.OBS;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Nodes.AbstractServiceNode;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import Grid.Sender.OBS.OBSEndSender;
import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OBSServiceNodeImpl extends AbstractServiceNode {

    /**
     * Constructor. Will generate default in- and outports
     * 
     * @param id
     *            the internal id of the object, used by the code
     * @param sim
     *            the {@link SimBaseSimulator} object this object is registered
     *            with
     */
    public OBSServiceNodeImpl(String id, GridSimulator sim) {
        super(id, sim);
        sender = new OBSEndSender(sim, this);
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
    public void init() {
        if (!inited) {
            super.init();
        }

    }
    
    public void route(){
        ((OBSEndSender) sender).setRoutingMap(gridSim.getRouting().getRoutingTable(this));
    }

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time t) {
        simulator.putLog(currentTime, id + " is an OBS Node and cannot request an OCS circuit", Logger.RED, -1, -1);
    }

    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port, Time time) {
        simulator.putLog(currentTime, id + " is an OBS Node and cannot tear down an OCS circuit ", Logger.RED, -1, -1);
    }
}

