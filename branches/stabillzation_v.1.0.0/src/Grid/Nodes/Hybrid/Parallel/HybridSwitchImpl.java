package Grid.Nodes.Hybrid.Parallel;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.*;
import Grid.Interfaces.Switches.AbstractSwitch;
import Grid.OCS.OCSRoute;
import Grid.OCS.stats.ManagerOCS;
import Grid.Port.GridOutPort;
import Grid.Sender.Hybrid.Parallel.HybridSwitchSender;
import Grid.Sender.Hybrid.Parallel.HyrbidEndSender;
import Grid.Sender.OBS.OBSSender;
import Grid.Sender.Sender;
import java.util.Map;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class HybridSwitchImpl extends AbstractSwitch {

    private Sender sender;

    public HybridSwitchImpl(String id, GridSimulator simulator) {
        super(id, simulator);
        sender = new HybridSwitchSender(this, simulator, wavelengthConversion);
    }

    public HybridSwitchImpl(String id, GridSimulator simulator, double costFindCommonWavelenght, double costAllocateWavelenght) {
        super(id, simulator);
        sender = new HybridSwitchSender(this, simulator, wavelengthConversion, costFindCommonWavelenght, costAllocateWavelenght);
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);
        if (m instanceof OCSRequestMessage) {
            OCSRequestMessage oCSRequestMessage = (OCSRequestMessage) m;
            ManagerOCS.getInstance().addInstaceOCS(oCSRequestMessage, getCurrentTime().getTime());
            handleOCSSetupMessage(inPort, (OCSRequestMessage) m);
        } else if (m instanceof OCSTeardownMessage) {
            handleTeardownMessage((OCSTeardownMessage) m, inPort);
        } else if (m instanceof OCSSetupFailMessage) {
            handleOCSSetupFailMessage((OCSSetupFailMessage) m);
        } else if (m instanceof OCSConfirmSetupMessage) {
            handleOCSConfirmSetupMessage((OCSConfirmSetupMessage) m);
        } else if (m instanceof OCSRequestTeardownMessage) {
            handleOCSRequestTeardownMessage((OCSRequestTeardownMessage) m);
        } else {
            handleGridMessage(inPort, (GridMessage) m);
        }
    }

    public void handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        ((HybridSwitchSender) sender).handleOCSSetupFailMessage(msg);
    }

    public void handleTeardownMessage(OCSTeardownMessage msg, SimBaseInPort port) {
        ((HybridSwitchSender) sender).handleTearDownOCSCircuit(msg, port);
    }

    public void handleOCSConfirmSetupMessage(OCSConfirmSetupMessage msg) {
        simulator.putLog(currentTime, id + " : Confirmation of OCS Setup between " + msg.getOcsRoute().getSource() + " and "
                + msg.getOcsRoute().getDestination() + " has been received.", -1, -1, -1);
        ((HybridSwitchSender) sender).handleConfirmMessage(msg, currentTime);
    }

    private void handleOCSRequestTeardownMessage(OCSRequestTeardownMessage requestTeardownMsg) {
        ((HybridSwitchSender) sender).handleOCSRequestTeardownMessage(requestTeardownMsg, currentTime);
    }

    /**
     * Will handle incoming grid messages which have nothing to do with OCS Path
     * setup.
     *
     * @param inport
     * @param m the message to forward.
     */
    private void handleGridMessage(SimBaseInPort inport, GridMessage m) {

        if (((HybridSwitchSender) sender).send(m, inport, currentTime)) {

            if (m.getTypeOfMessage() == GridMessage.MessageType.OBSMESSAGE) {
                simulator.putLog(currentTime, this.getId() + " OBS switched " + m.getId(), Logger.BLACK, m.getSize(), m.getWavelengthID());
            } else if (m.getTypeOfMessage() == GridMessage.MessageType.OCSMESSAGE) {
                simulator.putLog(currentTime, this.getId() + " OCS switched " + m.getId(), Logger.BLACK, m.getSize(), m.getWavelengthID());
            }

            if (m instanceof JobMessage) {
                simulator.addStat(this, Stat.SWITCH_JOBMESSAGE_SWITCHED);
                simulator.addStat(this, Stat.SWITCH_MESSAGE_SWITCHED);

            } else if (m instanceof JobResultMessage) {
                simulator.addStat(this, Stat.SWITCH_JOBRESULTMESSAGE_SWITCHED);
                simulator.addStat(this, Stat.SWITCH_MESSAGE_SWITCHED);
            } else if (m instanceof Grid.Interfaces.Messages.JobRequestMessage) {

                simulator.addStat(this, Stat.SWITCH_REQ_MESSAGE_SWITCHED);
                simulator.addStat(this, Stat.SWITCH_MESSAGE_SWITCHED);
//            //System.out.println(" drop "+m+" clas "+m.getClass() );
            }
        } else {

            if (m instanceof MultiCostMessage) {
                MultiCostMessage multiCostMessage = (MultiCostMessage) m;
                if (multiCostMessage.isReSent()) {
                    return;
                }
            }
            dropMessage(m);
        }
    }

    private void handleOCSSetupMessage(SimBaseInPort inport, OCSRequestMessage m) {
        ((HybridSwitchSender) sender).handleOCSPathSetupMessage(m, inport);

    }

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        ((HybridSwitchSender) sender).requestOCSCircuit(ocsRoute, permanent, currentTime);
    }

    /**
     * @param ent Entity that orders to teard down a OCS.
     * @param wavelength The OCS´s wavelength that will be tearddown.
     * @param port The port that is head of the ocs.
     * @param time The time that the tear down is going to start.
     */
    @Override
    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port, Time time) {
        //System.out.println("Solicito eliminar OCS Nodo:" + ent + " Puerto:" + port + " Lambda:" + wavelength + " Tiempo:" + time);
        ((HybridSwitchSender) sender).teardDownOCSCircuit(ent, wavelength, port, time);
//        simulator.putLog(currentTime, id + " is an OCS Switch and cannot tear down an OCS circuit on its own ", Logger.RED, -1, -1);
    }

    public Sender getSender() {
        return sender;
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
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        if (!inited) {
            super.init();
        }
    }

    public void route() {
        //sets the routingmap for this object
        OBSSender obs = (OBSSender) ((HybridSwitchSender) sender).getObsSender();
        obs.setRoutingMap(gridSim.getRouting().getRoutingTable(this));
    }

    public HybridSwitchImpl getEdgeRouterByEndNode(Entity source, Entity destination) {

        OBSSender OBSS_ender = null;
        if (source instanceof HybridClientNodeImpl) {
            HybridClientNodeImpl clientNodeImpl = (HybridClientNodeImpl) source;
            OBSS_ender = (OBSSender) ((HyrbidEndSender) clientNodeImpl.getSender()).getObsSender();

        } else if (source instanceof HybridResourceNode) {
            HybridResourceNode hybridResourceNode = (HybridResourceNode) source;
            OBSS_ender = (OBSSender) ((HyrbidEndSender) hybridResourceNode.getSender()).getObsSender();
        } else if (source instanceof HybridServiceNode) {
            HybridServiceNode hybridServiceNode = (HybridServiceNode) source;
            OBSS_ender = (OBSSender) ((HyrbidEndSender) hybridServiceNode.getSender()).getObsSender();
        }

        Map<String, GridOutPort> routingMapResourceNode = ((OBSSender) OBSS_ender).getRoutingMap();
        GridOutPort resourceOutportToClient = routingMapResourceNode.get(destination.getId());
        HybridSwitchImpl firstSwicth = (HybridSwitchImpl) resourceOutportToClient.getTarget().getOwner();

        return firstSwicth;
    }
}