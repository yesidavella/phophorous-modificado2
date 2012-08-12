package Grid.Sender.OCS;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.*;
import Grid.Nodes.LinkWavelengthPair;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import Grid.Sender.Sender;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import simbase.Port.SimBaseOutPort;
import simbase.Stats.Logger;
import simbase.Stats.SimBaseStats.Stat;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OCSEndSender extends Sender {

    /**
     * The mapping between entities and a  list with possible
     * OCS routes they can take.
     */
    protected Map<Entity, List<LinkWavelengthPair>> linkMapping;
    /**
     * The message queue. When a message has to be transimitted
     * but there is no OCs-circuit been setup between the requesting
     * noded, the messages get buffered here.
     * 
     * TODO: put a time constraint on the buffering time of a message.
     */
    private Queue<GridMessage> messageQueue;
    private double OCSSetupHandleTime;

    /**
     * Default constructor
     */
    public OCSEndSender(GridSimulator simulator, Entity owner, double OCSSetupHandleTime) {
        super(owner, simulator);
        linkMapping = new TreeMap<Entity, List<LinkWavelengthPair>>();
        messageQueue = new ArrayBlockingQueue<GridMessage>(10);
        this.OCSSetupHandleTime = OCSSetupHandleTime;
    }

    /**
     * Constructor
     * @param linkMapping The linkmapping of this OCS Sender
     */
    public OCSEndSender(Map<Entity, List<LinkWavelengthPair>> linkMapping, GridSimulator simulator, Entity owner) {
        super(owner, simulator);
        this.linkMapping = linkMapping;
        this.simulator = simulator;
        messageQueue = new ArrayBlockingQueue<GridMessage>(10);
    }

    @Override
    public boolean send(GridMessage message, Time t, boolean outputFail) {
        message.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);
        //CONTROL PLANE?
        if (message.getWavelengthID() == -1) {
            return owner.sendNow(message.getDestination(), message);
        } else {

            //get the list with possible routes
            List<LinkWavelengthPair> routeList = linkMapping.get(message.getDestination());
            // If no entry is found no OCS setup has yet been done
            if (routeList == null || routeList.isEmpty()) {
                if (outputFail) {
                    simulator.putLog(simulator.getMasterClock(), "FAIL: Sending failed because no OCS-circuit has been setup " +
                            owner.getId() + "-->" + message.getDestination().getId() + " : " + message.getId(), Logger.RED, message.getSize(), message.getWavelengthID());
                }
                messageQueue.offer(message);
                Grid.Utilities.Util.createOCSCircuit(owner, message.getDestination(), simulator, false);
                return false;
            } else {
                //find a route TODO: this is randow now, change this.
                int routeSize = routeList.size();
                Random random = new Random();
                int pick = random.nextInt(routeSize);
                LinkWavelengthPair outGoingPair = routeList.get(pick);
                message.setWavelengthID(outGoingPair.getWavelength());
                GridOutPort theOutPort = (GridOutPort) outGoingPair.getPort();
                int theOutgoingWavelength = outGoingPair.getWavelength();
                message.setWavelengthID(theOutgoingWavelength);
                //We can send, the link is free.
                return putMsgOnLink(message, theOutPort, t);
            }
        }
    }

    /**
     * Handles the setup of OCS Circuits
     * @param message The message with information about the OCS circuit.
     * @param newHop The enity which is being added to the circuit.
     * @return True is message needs to be forwarded, false if not.
     */
    public boolean handleOCSSetup(OCSRequestMessage msg, Entity newHop) {
        msg.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);
        //END OF CIRCUIT REACHED?
        OCSRoute ocsRoute = msg.getOCSRoute();
        if (ocsRoute.getDestination().equals(newHop)) {
            simulator.putLog(simulator.getMasterClock(), "<u>OCS: end of OCS Path reached" + ocsRoute +
                    " " + ocsRoute.getWavelength() + "</u>",
                    Logger.ORANGE, msg.getSize(), msg.getWavelengthID());
            if (msg.isPermanent()) {
                //only permanent circuits get a confirm message
                simulator.confirmRequestedCircuit(ocsRoute);
            } else {
                Time addedTime = new Time(owner.getCurrentTime().getTime() + OCSSetupHandleTime);
                OCSConfirmSetupMessage confirm = new OCSConfirmSetupMessage("confirm:" + ocsRoute.getSource() + "-" + ocsRoute.getDestination(), addedTime, ocsRoute);
                confirm.setSource(owner);
                confirm.setDestination(ocsRoute.getSource());
                simulator.putLog(simulator.getMasterClock(), owner + " send out a confirmation for " + ocsRoute +
                        " " + ocsRoute.getWavelength(),
                        Logger.ORANGE, confirm.getSize(), confirm.getWavelengthID());
                owner.sendNow(ocsRoute.getSource(), confirm, addedTime);
            }
            simulator.addStat(newHop, Stat.OCS_CIRCUIT_SET_UP);
            return false; //nothing should be done, end of circuit has been reached
        } else {
            //THIS IS A BEGINNING  HOP
            int index = ocsRoute.indexOf(newHop);
            Iterator<SimBaseOutPort> it = newHop.getOutPorts().iterator();
            GridOutPort outPort = null;
            Entity newHopOnCircuit = ocsRoute.get(index + 1);
            //Search for the outport for the next hop
            while (it.hasNext()) {
                outPort = (GridOutPort) it.next();
                if (outPort.getID().startsWith(newHop.getId()) &&
                        outPort.getID().endsWith(newHopOnCircuit.getId())) {
                    break;
                }
            }
            //Have we found the correct Outport?
            if (outPort == null) {
                simulator.putLog(simulator.getMasterClock(), "FAIL: OCS Setup failed because no OCS-link exist to send to" +
                        newHopOnCircuit, Logger.RED, msg.getSize(), msg.getWavelengthID());
                simulator.addStat(newHop, Stat.OCS_CIRCUIT_SETUP_DID_NOT_WORK);
                if (msg.isPermanent()) {
                    simulator.circuitTearDown(ocsRoute);
                }
                return false;
            } else {
                //FOUND THE OUTPORT
                //Find an outgoing wavelengths
                int newWavelenghth = outPort.getNexFreeWavelength();
                if (newWavelenghth != -1) {
                    LinkWavelengthPair outGoingPair = new LinkWavelengthPair(outPort, newWavelenghth);
                    outPort.addWavelength(newWavelenghth);
                    ocsRoute.setWavelength(newWavelenghth);
                    List destinationList;
                    //Search for the list of the destination newHop
                    if (!linkMapping.containsKey(msg.getDestination())) {
                        destinationList = new ArrayList<LinkWavelengthPair>();
                        linkMapping.put(msg.getDestination(), destinationList);
                    } else {
                        destinationList = linkMapping.get(msg.getDestination());
                    }
                    destinationList.add(outGoingPair);
                    msg.setWavelengthID(newWavelenghth);
                    simulator.putLog(simulator.getMasterClock(),
                            "OCS: OCS link setup between <b>" + newHop.getId() + "</b> and <b>" + newHopOnCircuit + "</b> on " + newWavelenghth +
                            ocsRoute, Logger.ORANGE, msg.getSize(), msg.getWavelengthID());
                    Time addedTime = new Time(owner.getCurrentTime().getTime() + OCSSetupHandleTime);
                    int newHopIndex = ocsRoute.indexOf(owner) + 1;
                    Entity nextHop = ocsRoute.get(newHopIndex);
                    owner.sendNow(nextHop, msg, addedTime);
                    simulator.addStat(owner, Stat.OCS_PART_OF_CIRCUIT_SET_UP);
                    return true;
                } else {
                    //No free wavelength found
                    simulator.putLog(simulator.getMasterClock(), "FAIL: OCS setup failed, no free wavelength for " + outPort, Logger.RED, msg.getSize(), msg.getWavelengthID());
                    return false;
                }
            }
        }
    }

    /**
     * Will send an OCSRequestMessage to setup an OCS-circuit. 
     * @param ocsRoute The hops used on the path.
     */
    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ocsRequest ");
        buffer.append(Entity.OCSRequestID);
        Entity.OCSRequestID++;
        buffer.append(":");
        buffer.append(ocsRoute.getSource().getId());
        buffer.append("-");
        buffer.append(ocsRoute.getDestination().getId());

        //what's the position of this entity on the Circuit
        int position = ocsRoute.indexOf(owner);
        //find the next hop
        int nextHopPos = position + 1;

        List<SimBaseOutPort> outports = owner.getOutPorts();
        Iterator<SimBaseOutPort> outportIt = outports.iterator();
        GridOutPort theOutport = null;
        while (outportIt.hasNext()) {
            SimBaseOutPort port = outportIt.next();
            if (port.getID().startsWith(owner.getId()) && port.getID().endsWith(ocsRoute.get(nextHopPos).getId())) {
                theOutport = (GridOutPort) port;
            }
        }
        //found the outport, now find an approriate wavelength


        //Find a free wavelength on which we can construct a OCS circuit.
        int wavelength = theOutport.getNexFreeWavelength();

        if (wavelength == -1) {
            simulator.putLog(owner.getCurrentTime(), owner.getId() + " could not find a free wavelength for a new circuit..." +
                    ocsRoute, Logger.RED, -1, -1);

        } else {
            Time addedTime = new Time(time.getTime() + OCSSetupHandleTime);
            OCSRequestMessage request = new OCSRequestMessage(buffer.toString(), addedTime, ocsRoute, permanent);

            request.setWavelengthID(wavelength);
            //sets the wavelengths on which this circuit begins
            request.getOCSRoute().setWavelength(wavelength);
            //Sends the OCS circuit request to the source (could be this 
            //entity) and lets the receive method handle the OCS request.
            simulator.putLog(owner.getCurrentTime(), owner.getId() + " made an OCS setup request for " +
                    ocsRoute, Logger.ORANGE, -1, -1);

            //TODO:Calculate timings
            owner.sendNow(ocsRoute.getSource(), request, addedTime);
        }
    }

    /**
     * Will try to tear down a OCS circuit. The OCSTeardownMessage get forwarded
     * on the circuit and with each hop the circuit get's torn down.
     * @param destination The end destination of the circuit.
     * @param wavelength The wavelength on which we want to tear down the circuit.
     * @param outport The @link{GridOutPort} which represents the physical link 
     * on which the circuits lies on.
     * @param time The time this circuit has to be torn down
     * @return true if tear down worked, false if not.
     */
    public boolean tearDownOCSCircuit(Entity destination, int wavelength, GridOutPort outport, Time time) {

        //find the circuits which have been used by this entity
        List routes = linkMapping.get(destination);
        //find the route on that wavelength
        if (routes != null && !routes.isEmpty()) {
            Iterator<LinkWavelengthPair> routeIt = routes.iterator();
            boolean found = false;
            while (routeIt.hasNext() && !found) {
                LinkWavelengthPair pair = routeIt.next();
                if (pair.getPort().equals(outport) && pair.getWavelength() == wavelength) {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("OCSTearDown:");
                    buffer.append(owner);
                    buffer.append("-");
                    buffer.append(destination);
                    OCSTeardownMessage teardownMsg = new OCSTeardownMessage(buffer.toString(), time, wavelength);
                    teardownMsg.setOutport(outport);
                    teardownMsg.setDestination(destination);
                    teardownMsg.setSource(owner);
                    owner.sendSelf(teardownMsg, time);
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * Will handle the @link{OCSTeardownMessage}. If this owner is the end
     * of the circuit then an appropriate message is logged, else a mistake happened.
     * @param msg The OCSTeardownMessage.
     */
    public void handleOCScircuitTearDown(OCSTeardownMessage msg) {
        if (msg.getDestination().equals(owner)) {
            simulator.putLog(owner.getCurrentTime(), "OCS circuit has been tear down between " + msg.getSource() +
                    " and " + msg.getDestination(), Logger.ORANGE, msg.getSize(), msg.getWavelengthID());
        } else {
            //find the circuits which have been used by this entity
            List routes = linkMapping.get(msg.getDestination());
            //find the route on that wavelength
            Iterator<LinkWavelengthPair> routeIt = routes.iterator();
            boolean found = false;
            while (routeIt.hasNext() && !found) {
                LinkWavelengthPair pair = routeIt.next();
                if (pair.getPort().equals(msg.getOutport()) && pair.getWavelength() == msg.getWavelenght()) {
                    //remove the mapping from the routing table
                    found = true;
                    routes.remove(pair);
                    if (routes.isEmpty()) {
                        linkMapping.remove(msg.getDestination());
                    }
                    msg.getOutport().removeWavelength(msg.getWavelenght());
                    simulator.putLog(owner.getCurrentTime(), "OCS Circuit teardown message send from " + owner +
                            " to " + msg.getOutport().getTarget().getOwner(), Logger.ORANGE, msg.getSize(), msg.getWavelenght());
                    simulator.addStat(owner, Stat.OCS_CIRCUIT_TEAR_DOWN);
                    Time addedTime = new Time(owner.getCurrentTime().getTime() + OCSSetupHandleTime);
                    owner.send(msg.getOutport(), msg, addedTime);
                }
            }
        }
    }

    /**
     * Return the routing table of the owner of this Sender.
     * @return the routing table of the owner of this Sender
     */
    public Map<Entity, List<LinkWavelengthPair>> getLinkMapping() {
        return linkMapping;
    }

//    public void setLinkMapping(Map<Entity, List<LinkWavelengthPair>> linkMapping) {
//        this.linkMapping = linkMapping;
//    }

    /**
     * Will handle an OCSSetupFailMessage.
     * @param msg The OCS Setup Fail Message.
     * @return True if tear down worked, false if not. 
     */
    public boolean handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        OCSRoute route = msg.getOcsRoute();
        int index = route.indexOf(owner);
        Entity formerHop = route.get(index + 1);

        //Find the appropratie outport
        GridOutPort port = owner.findOutPort(formerHop, msg.getWavelength());

        Entity destination = route.get(route.size() - 1);
        List<LinkWavelengthPair> destinationList = linkMapping.get(destination);
        if (destinationList.remove(new LinkWavelengthPair(port, msg.getWavelength()))) {
            simulator.addStat(owner, Stat.OCS_CIRCUIT_PART_OF_CONFLICT);
            simulator.putLog(owner.getCurrentTime(), "End of tear down reached " + route, Logger.ORANGE, msg.getSize(), msg.getWavelength());
            return true;
        } else {

        }
        simulator.putLog(owner.getCurrentTime(), "End of tear down reached, but tear down did not work..." + route, Logger.RED, msg.getSize(), msg.getWavelength());
        return false;
    }

    /**
     * Handle routine for when the owner setup an OCS circuit. The confirmation is send
     * by the last hop on the path. When this confirmation is received, pending messages 
     * for this circuit can be send.
     * @param msg The confirmation message of the OCS circuit setup. 
     */
    public void handleConfirmMessage(OCSConfirmSetupMessage msg) {
        Iterator<GridMessage> it = messageQueue.iterator();
        GridMessage m = null;
        while (it.hasNext()) {
            m = it.next();
            if (m.getDestination().equals(msg.getOcsRoute().getDestination())) {
                this.send(m, owner.getCurrentTime(), true);
                //TODO : Check time constraints
                messageQueue.remove(m);
            //Maybe other messages are in the queue to be send, so do not tear down this circuit yet
            }
        }
        if (m != null) {
            Entity nextHop = msg.getOcsRoute().get(msg.getOcsRoute().indexOf(this) + 1);
            owner.teardDownOCSCircuit(msg.getOcsRoute().getSource(), msg.getOcsRoute().getWavelength(), owner.findOutPort(nextHop,
                    msg.getOcsRoute().getWavelength()), new Time(owner.getCurrentTime().getTime() + 51));
        }
    }
}