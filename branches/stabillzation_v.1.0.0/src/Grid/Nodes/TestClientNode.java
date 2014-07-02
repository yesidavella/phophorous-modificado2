package Grid.Nodes;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.JobMessage;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import Grid.Sender.Hybrid.Parallel.HyrbidEndSender;
import Grid.Sender.OBS.OBSEndSender;
import Grid.Sender.Sender;
import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class TestClientNode extends Entity {

    static int jobNr = 0;
    private Sender sender;

    public TestClientNode(String id, GridSimulator simulator) {
        super(id, simulator);
        sender = new HyrbidEndSender(this, simulator);
    }

    public void sendJobAtTime(Time t, int size, Entity destination) {
        jobNr++;
        JobMessage job = new JobMessage("JobMessage_" + jobNr, t);
        job.setDestination(destination);
        job.setSize(size);
        job.addHop(this);
        job.setSource(this);

        sender.send(job, t, true);
        simulator.putLog(simulator.getMasterClock(), "Job has been scheduled to be send at : " + t, Logger.BLUE, (double) job.getWavelengthID(), (int) job.getSize());
    }

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean supportSwitching() {
        return false;
    }

    @Override
    public boolean supportsOBS() {
        return true;
    }

    @Override
    public boolean supportsOCS() {
        return true;
    }

    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port, Time time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        if (!inited) {
            super.init();
        }
        route();
    }

    public void route() {
        //sets the routingmap for this object
        OBSEndSender obs = (OBSEndSender) ((HyrbidEndSender) sender).getObsSender();
        obs.setRoutingMap(gridSim.getRouting().getRoutingTable(this));
    }
}
