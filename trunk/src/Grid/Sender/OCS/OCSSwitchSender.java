/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Sender.OCS;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.OCSConfirmSetupMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.Switch;
import Grid.Nodes.AbstractServiceNode;
import Grid.Nodes.Hybrid.Parallel.HybridClientNodeImpl;
import Grid.Nodes.Hybrid.Parallel.HybridResourceNode;
import Grid.Nodes.Hybrid.Parallel.HybridSwitchImpl;
import Grid.Nodes.LambdaChannelGroup;
import Grid.Nodes.LinkWavelengthPair;
import Grid.Nodes.PCE;
import Grid.Nodes.PCE.OpticFlow;
import Grid.OCS.OCSRoute;
import Grid.OCS.stats.ManagerOCS;
import Grid.Port.GridInPort;
import Grid.Port.GridOutPort;
import Grid.Route;
import Grid.Sender.Hybrid.Parallel.HybridSwitchSender;
import Grid.Sender.Hybrid.Parallel.HyrbidEndSender;
import Grid.Sender.OBS.OBSSender;
import Grid.Sender.Sender;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import simbase.Port.SimBaseInPort;
import simbase.Port.SimBaseOutPort;
import simbase.Port.SimBasePort;
import simbase.SimBaseEntity;
import simbase.Stats.Logger;
import simbase.Stats.SimBaseStats.Stat;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OCSSwitchSender extends Sender {

    /**
     * The mapping between <Link,wavelength> pairs.
     */
    private Map<LinkWavelengthPair, LinkWavelengthPair> linkMapping;
    private double OCSSetupHandle;
    public static final int PERCENTAGE_TO_DROP_OCS = 1;

    /**
     * Constructor
     */
    public OCSSwitchSender(GridSimulator simulator, Entity owner, double OCSSetupHandle) {
        super(owner, simulator);
        linkMapping = new TreeMap<LinkWavelengthPair, LinkWavelengthPair>();
        this.OCSSetupHandle = OCSSetupHandle;
    }

    /**
     * Sends a gridmessage through an OCS Circuit.
     *
     * @param msg The message to send
     * @param gridInPort The inport used.
     * @param source The requester that sends the message
     * @param t the time the message entered the switch
     * @return True if sending worked, false if not.
     */
    public boolean send(GridMessage msg, SimBasePort inPort, Time t, boolean outputFail) {

        msg.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);
        //The wavelength on which the message came
        int wavelength = msg.getWavelengthID();

        //CONTROL PLANE?
        if (wavelength == -1) {
            //Find the next hop on the path
            Route route = msg.getRoute();
            int index = route.indexOf(owner);
            if (index == route.size() - 1) {
                return true; //This is the end on the path
            } else {
                int nextHopIndex = index + 1;
                Entity nextHop = route.get(nextHopIndex);
                return owner.sendNow(nextHop, msg);
            }
        } else {
            //NOT THE CONTROL PLANE

            //find the appropriate outgoing link-wavelength pair.
            LinkWavelengthPair incomingPair = new LinkWavelengthPair(inPort, wavelength);
            LinkWavelengthPair outgoingPair = linkMapping.get(incomingPair);
            if (outgoingPair == null) {
                if (outputFail) {
                    simulator.putLog(simulator.getMasterClock(), "FAIL: Sending failed because no reservation is made for "
                            + owner.getId() + " --> " + msg.getDestination().getId(), Logger.RED, msg.getSize(), msg.getWavelengthID());
                }
                return false;
            } else {
//                 System.out.println("#################### tiene outgoingpir  ##################### " );
//                System.out.println(" Switch via OCS  Msg  "+msg +" inPort "+inPort+ " incomingPair " + incomingPair + " outgoingPair " + outgoingPair);
                msg.setWavelengthID(outgoingPair.getWavelength());

                return putMsgOnLink(msg, (GridOutPort) outgoingPair.getPort(), t);
            }
        }
    }

    /**
     * Will send a message into the network. For this method the route object in
     * the GridMessage must be correct, otherwise this method will output false
     * results.
     *
     * @param message The message which is to be send
     * @param t The time of sending
     * @param destination The destination of this message
     * @return true if sending worker, false if not.
     */
    @Override
    public boolean send(GridMessage message, Time t, boolean outputFail) {
        //find the inport on which this message was received
        Route route = message.getRoute();

        Entity previousHop = route.getLastHopAddedToPath();
        //previoushop cannot be null because a switch cannot send on it own
        List inports = owner.getInPorts();
        Iterator<GridInPort> gridInportIterator = inports.iterator();
        GridInPort port = null;
        while (gridInportIterator.hasNext()) {
            port = gridInportIterator.next();
            if (port.getID().startsWith(previousHop.getId()) && port.getID().endsWith(owner.getId())) {
                break;
            }
        }
        if (port == null) {
            if (outputFail) {
                simulator.putLog(t, "FAIL: Could not find inport ...", Logger.RED, message.getSize(), message.getWavelengthID());
            }
            return false;
        } else {
            return send(message, port, t, outputFail);
        }
    }

    /**
     * Handles the setup of OCS circuits.
     *
     * @param ocsReqMsg The OCSRequestmessage which inited the circuit setup.
     * @param requester The requester for which the setup is needed.
     * @param inport The inport on which the OCSRequestmessage entered the
     * requester.
     * @return True if forwarding is needed, false if not
     */
    public boolean handleOCSPathSetupMessage(OCSRequestMessage ocsReqMsg, SimBaseInPort inport) {

        ocsReqMsg.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);
        OCSRoute ocsRoute = ocsReqMsg.getOCSRoute();
        Time addedTime = new Time(owner.getCurrentTime().getTime() + OCSSetupHandle);

        //Check if this hop is the last on the circuit
        if (ocsRoute.getDestination().equals(owner)) {

            simulator.putLog(simulator.getMasterClock(), "<u>OCS: end of OCS Path reached" + ocsReqMsg.getOCSRoute() + "</u>", Logger.ORANGE, ocsReqMsg.getSize(), ocsReqMsg.getWavelengthID());
            simulator.addStat(owner, Stat.OCS_CIRCUIT_SET_UP);

            ManagerOCS.getInstance().confirmInstanceOCS(ocsReqMsg, addedTime.getTime());
            if (ocsReqMsg.isPermanent()) {
                simulator.confirmRequestedCircuit(ocsRoute);
            }
            if (!ocsRoute.getSource().supportSwitching()) {
                //only send confirmation to edge nodes

                OCSConfirmSetupMessage confirm = new OCSConfirmSetupMessage("confirm:" + ocsRoute.getSource() + "-" + ocsRoute.getDestination(), addedTime, ocsRoute);

                confirm.setSource(owner);
                confirm.setWavelengthID(-1);
                confirm.setDestination(ocsRoute.getSource());

                owner.sendNow(confirm.getDestination(), confirm, addedTime);

            }
            return true; //nothing should be done, end of circuit has been reached
        } else {

            //this is not the destination where on the path we are
            //search for outport to send to the next hop
            int ownerIndex = ocsRoute.indexOf(owner);
            Entity nextHopOnPath = ocsRoute.findNextHop(owner);

            GridOutPort ownerOutPort = null;
            Iterator<SimBaseOutPort> ownerOutPortsIt = owner.getOutPorts().iterator();

            while (ownerOutPortsIt.hasNext()) {

                ownerOutPort = (GridOutPort) ownerOutPortsIt.next();
                if (ownerOutPort.getID().startsWith(owner.getId()) && ownerOutPort.getID().endsWith(nextHopOnPath.getId())) {
                    break;
                } else {
                    continue;
                }
            }

            //Search for the incoming port, because OCS Setup messages are send through the 
            //control plane (self-gridInPort)
            Entity previousHopOnPath = null;

            if (!ocsReqMsg.getSource().equals(owner)) {
                //OJO SOLO entra en los nodos intermedios de la formacion del Circuito
                previousHopOnPath = ocsRoute.get(ownerIndex - 1);
            } else {
                //OJO SOLO entra cuando es la CABEZA del circuito en formacion
                //The owner made a request, this is a switch so the request just needs to be forwarded. And a wavelengths needs to be reserved
                // This can only be done in case of hybrid switching of course
                if (owner.supportsOBS() && owner.supportsOCS()) {
                    //Find a free wave length for the beginning of the path
                    int beginningWavelength = ownerOutPort.getNexFreeWavelength();

                    if (beginningWavelength != -1) {

                        ocsRoute.setWavelength(beginningWavelength);
                        ocsReqMsg.setWavelengthID(beginningWavelength);
                        ownerOutPort.addWavelength(beginningWavelength);

                        if (owner.sendNow(nextHopOnPath, ocsReqMsg, addedTime)) {
                            if (ocsReqMsg.isPermanent()) {
                                simulator.addRequestedCircuit(ocsRoute);
                            }

                            simulator.putLog(simulator.getMasterClock(), "OCS: OCS requestmessage send from <b>" + owner.getId() + "</b> to <b>" + nextHopOnPath + "</b> " + "for <b>" + ocsRoute.getDestination() + "</b> reserving wavelength <b>" + beginningWavelength + " </b>", Logger.ORANGE, ocsReqMsg.getSize(), ocsReqMsg.getWavelengthID());
                            return true;
                        } else {
                            simulator.putLog(simulator.getMasterClock(), "OCS: OCS Requestmessage could not be send <b>" + owner.getId() + "</b> to <b>" + nextHopOnPath + "</b>", Logger.ORANGE, ocsReqMsg.getSize(), ocsReqMsg.getWavelengthID());
                            ManagerOCS.getInstance().notifyError(ocsReqMsg, addedTime.getTime(), owner, "OCS Requestmessage could not be send");
                            return false;
                        }
                    } else {
                        simulator.putLog(simulator.getMasterClock(), "OCS: OCS setup could not be realized because no free wavelength could be found on </b>" + owner.getId() + "</b> to <b>" + nextHopOnPath + "</b>", Logger.RED, ocsReqMsg.getSize(), ocsReqMsg.getWavelengthID());
                        ManagerOCS.getInstance().notifyError(ocsReqMsg, addedTime.getTime(), owner, "OCS setup could not be realized because no free wavelength could be found ");
                        return false;
                    }
                }
            }

            ///#########Por aca continua en los nodos intermedios de formacion del Circuito###########///
            GridInPort ownerInPort = null;
            Iterator<SimBaseInPort> ownerInPortsIt = owner.getInPorts().iterator();

            while (ownerInPortsIt.hasNext() && previousHopOnPath != null) {
                ownerInPort = (GridInPort) ownerInPortsIt.next();
                if (ownerInPort.getID().startsWith(previousHopOnPath.getId())) {
                    break;
                } else {
                    continue;
                }
            }

            // There is no match between inport and outport
            if (ownerOutPort == null || ownerInPort == null) {
                simulator.putLog(simulator.getMasterClock(), "FAIL: OCS setup failed because of outport/inport mismatch "
                        + owner.getId(), Logger.RED, ocsReqMsg.getSize(), ocsReqMsg.getWavelengthID());
                simulator.addStat(owner, Stat.OCS_CIRCUIT_SETUP_DID_NOT_WORK);
                if (ocsReqMsg.isPermanent()) {
                    simulator.cancelRequestedCircuit(ocsRoute);
                }
                //Undo all changes made in previous steps (ONLY when there are steps done)
                if (!ocsReqMsg.getSource().equals(owner)) {
                    rollBackOCSSetup(ocsRoute);
                }
                ManagerOCS.getInstance().notifyError(ocsReqMsg, addedTime.getTime(), owner, "OCS setup failed because of outport/inport mismatch ");
                return false;
            } else {
                //Found outport, know the wavelength -->update linktable (wavelength here is wavelengthId)
                //because wavelength of the OCSRoute is the beginning wavelength which could already 
                //have been changed earlier
                //First find earlier connections if there are 

                LinkWavelengthPair incomingPair = new LinkWavelengthPair(ownerInPort, ocsReqMsg.getWavelengthID());

                if (linkMapping.containsKey(incomingPair)) {

                    simulator.putLog(owner.getCurrentTime(), "FAIL: OCS " + owner.getId() + " got a OCS setup message for a part of an "
                            + "already existing circuit... [route : " + ocsRoute + "] " + incomingPair, Logger.RED, ocsRoute.getWavelength(), (int) ocsReqMsg.getSize());

                    rollBackOCSSetup(ocsRoute);
                    simulator.addStat(owner, Stat.OCS_CIRCUIT_CONFLICT);
                    if (ocsReqMsg.isPermanent()) {
                        simulator.cancelRequestedCircuit(ocsRoute);
                    }
                    ManagerOCS.getInstance().notifyError(ocsReqMsg, addedTime.getTime(), owner, " got a OCS setup message for a part of an "
                            + "already existing circuit... [route : " + ocsRoute + "] ");
                    return false;
                }

                int newWaveLength;
                //can we use the same wavelength on which the message got in?
//                if (!ownerOutPort.isWaveUsedInCircuit(ocsReqMsg.getWavelengthID())) {
//                    newWaveLength = ocsReqMsg.getWavelengthID();
//                } 
//                else 
//                {
//                    //Wavelength already in use, try to find a new one
//                    newWaveLength = ownerOutPort.getNexFreeWavelength();
//                }

                newWaveLength = ownerOutPort.getNexFreeWavelength();
                if (newWaveLength != -1) {
                    ocsReqMsg.setWavelengthID(newWaveLength);
                    LinkWavelengthPair outGoingPair = new LinkWavelengthPair(ownerOutPort, newWaveLength);
                    ownerOutPort.addWavelength(newWaveLength);
                    linkMapping.put(incomingPair, outGoingPair);
                    simulator.putLog(simulator.getMasterClock(), "OCS: OCS link setup between <b>" + owner.getId()
                            + "</b> and <b>" + nextHopOnPath + "</b> on " + newWaveLength + " " + ocsRoute, Logger.ORANGE, ocsReqMsg.getSize(), ocsReqMsg.getWavelengthID());
                    simulator.addStat(owner, Stat.OCS_PART_OF_CIRCUIT_SET_UP);

                    if (owner.sendNow(nextHopOnPath, ocsReqMsg, addedTime)) {
                        //simulator.putLog(simulator.getMasterClock(), "OCS: OCS requestmessage send from <b>" + owner.getId() + "</b> to <b>" + newHopOnPath + "</b>", Logger.ORANGE, m.getSize(), m.getWavelengthID());
                        return true;
                    } else {
                        simulator.putLog(simulator.getMasterClock(), "OCS: OCS Requestmessage could not be send <b>" + owner.getId() + "</b> to <b>" + nextHopOnPath + "</b>", Logger.RED, ocsReqMsg.getSize(), ocsReqMsg.getWavelengthID());
                        ManagerOCS.getInstance().notifyError(ocsReqMsg, addedTime.getTime(), owner, "OCS Requestmessage could not be send ");
                        return false;
                    }
                } else {
                    //No new wavelengths could be found. Undo all changes.
                    simulator.putLog(simulator.getMasterClock(), "OCS: OCS setup could not be realized because no free wavelength could be found on </b>" + owner.getId() + "</b> to <b>" + nextHopOnPath + "</b>", Logger.RED, ocsReqMsg.getSize(), ocsReqMsg.getWavelengthID());
                    ManagerOCS.getInstance().notifyError(ocsReqMsg, addedTime.getTime(), owner, " OCS setup could not be realized because no free wavelength could be found on ");
                    rollBackOCSSetup(ocsRoute);
                    return false;
                }
            }
        }
    }

    /**
     * This methodes is executed when somehting went wrong during the setup of
     * the OCS circuit.
     *
     * @param ocsRoute The ocs route which failed.
     */
    public void rollBackOCSSetup(OCSRoute ocsRoute) {
        OCSSetupFailMessage setupFailMsg = new OCSSetupFailMessage("OCS-SetupFailMessage " + ocsRoute.getSource() + "--" + ocsRoute.getDestination(),
                owner.getCurrentTime(), ocsRoute.getWavelength(), ocsRoute);
        owner.sendNow(ocsRoute.get(ocsRoute.indexOf(owner) - 1), setupFailMsg);
    }

    /**
     * Will send an OCSRequestMessage to setup an OCS-circuit.
     *
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

        OCSRequestMessage request = new OCSRequestMessage(buffer.toString(), time, ocsRoute, permanent);
        //must be -1 --> control plane
        request.setWavelengthID(-1);
        //Sends the OCS circuit request to the source (could be this 
        //entity) and lets the receive method handle the OCS request.
        owner.sendNow(ocsRoute.getSource(), request, time);

    }

    /**
     * Will handle an @link{OCSTeardownMessage}. It will forward the message and
     * if succesfull it will remove the mapping from the routing table.
     *
     * @param teardownMsg The OCSTeardownMessage
     * @param inport The inport on which we received the OCSTeardownMessage.
     * @return True if tear down worker, false if not.
     */
    public boolean handleTearDownOCSCircuit(OCSTeardownMessage teardownMsg, SimBaseInPort inport) {

        double timeSetFreeResources = OCSSetupHandle / 3;
        Time addedTime = new Time(owner.getCurrentTime().getTime() + timeSetFreeResources);
        OCSRoute ocsRouteMsg = teardownMsg.getOcsRoute();

        //Check if this entity is the head of OCS
        if (owner.equals(teardownMsg.getSource())) {

            int lambdaToSetFree = teardownMsg.getWavelengthID();

            if (lambdaToSetFree != ocsRouteMsg.getWavelength()) {
//                There is a error. The first OCS´s lambda is not the first lambda set to msg
                return false;
            }

            //Find the outPort to set free the wavelenght
            GridOutPort outPortMsgGoes = teardownMsg.getOutport();

            if (owner.getOutPorts().contains(outPortMsgGoes)) {

                GridOutPort ownerOutPortMsgGoes = ((GridOutPort) owner.getOutPorts().get(owner.getOutPorts().indexOf(outPortMsgGoes)));

                if (ownerOutPortMsgGoes.removeWavelength(lambdaToSetFree)) {

                    teardownMsg.setWavelenght(lambdaToSetFree);
                    simulator.putLog(simulator.getMasterClock(), "<u>OCS Teardown: HEAD of OCS Path " + teardownMsg.getOcsRoute() + "</u>", Logger.GRAY, teardownMsg.getSize(), teardownMsg.getWavelengthID());

                    return owner.send(ownerOutPortMsgGoes, teardownMsg, addedTime);
                }
            }

            return false;

        } else if (owner.equals(teardownMsg.getDestination())) {//Check if this entity is the END of OCS

            simulator.putLog(owner.getCurrentTime(), "<u>OCS Teardown: END of OCS Path reached " + teardownMsg.getOcsRoute() + "</u>", Logger.GRAY, teardownMsg.getSize(), teardownMsg.getWavelengthID());
            simulator.addStat(owner, Stat.OCS_CIRCUIT_TEAR_DOWN);
            ManagerOCS.getInstance().confirmTearDownOCS(teardownMsg, owner.getCurrentTime().getTime());
            return simulator.circuitTearDown(ocsRouteMsg);

        } else {

            int arriveMsgWavelength = teardownMsg.getWavelengthID();

            //find the appropriate outgoing link-wavelength pair.
            LinkWavelengthPair incomingPair = new LinkWavelengthPair(inport, arriveMsgWavelength);
            LinkWavelengthPair outgoingPair = linkMapping.get(incomingPair);

            if (outgoingPair == null) {

                simulator.putLog(simulator.getMasterClock(), "Deleting OCS failed because no incomin<->outcoming wavelenght not found between "
                        + owner.getId() + " --> " + teardownMsg.getDestination().getId(), Logger.RED, teardownMsg.getSize(), teardownMsg.getWavelengthID());

                return false;
            } else {

                int wavelenghToNextHop = outgoingPair.getWavelength();
                GridOutPort outPortToNextHop = (GridOutPort) outgoingPair.getPort();
                teardownMsg.setWavelengthID(wavelenghToNextHop);

                if (owner.send(outPortToNextHop, teardownMsg, addedTime)) {
                    //Set free resources
                    if (outPortToNextHop.removeWavelength(wavelenghToNextHop) && linkMapping.remove(incomingPair) != null) {
                        simulator.putLog(simulator.getMasterClock(), "OCS Circuit torn down between " + inport.getSource().getOwner() + " and " + owner, Logger.GREEN, teardownMsg.getSize(), teardownMsg.getWavelenght());
                        return true;
                    } else {
                        simulator.putLog(simulator.getMasterClock(), "Can NOT set free resources. Problem inport:" + inport + " Arrive wavelength:" + arriveMsgWavelength + " and "
                                + " outPortToNextHop:" + outPortToNextHop + " wavelenghToNextHop:" + wavelenghToNextHop
                                + owner, Logger.RED, teardownMsg.getSize(), teardownMsg.getWavelenght());
                    }
                } else {
                    simulator.putLog(simulator.getMasterClock(),"Can NOT send Teardown message between "+owner.getId()+" and "+outPortToNextHop.getTarget().getOwner().getId(),
                            Logger.RED, teardownMsg.getSize(), teardownMsg.getWavelengthID());
                }

                return false;
            }
        }
    }

    public Map<LinkWavelengthPair, LinkWavelengthPair> getLinkMapping() {
        return linkMapping;
    }

    /**
     * Handles a OCSSetupFailMessage. This message is send when, at some part of
     * the OCS setup the setup fails and needs to be roll back.
     *
     * @param msg The OCSSetupFailMessage that has been send
     * @return true if it could be undone, false if not.
     */
    public boolean handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        OCSRoute route = msg.getOcsRoute();
        int index = route.indexOf(owner);
        Iterator<SimBaseOutPort> outPortsIterator = owner.getOutPorts().iterator();
        GridOutPort outPort = null;
        Entity lastHopOnPath = route.get(index + 1);
        while (outPortsIterator.hasNext()) {
            outPort = (GridOutPort) outPortsIterator.next();
            if (outPort.getID().startsWith(owner.getId())
                    && outPort.getID().endsWith(lastHopOnPath.getId())) {
                break;
            } else {
                continue;
            }
        }

        //now we have to undo the setting, so we have to find the incomingpair...
        Set<LinkWavelengthPair> keySet = linkMapping.keySet();
        Iterator<LinkWavelengthPair> keyIt = keySet.iterator();

        while (keyIt.hasNext()) {
            LinkWavelengthPair pair = keyIt.next();
            if (linkMapping.get(pair).equals(new LinkWavelengthPair(outPort, msg.getWavelength()))) {
                // we have found the entry to this outport
                linkMapping.remove(pair);
                //Reset the wavelength, they do not necessarily have to be the same
                msg.setWavelength(pair.getWavelength());
                simulator.putLog(owner.getCurrentTime(), "Circuit-part has been torn down between " + owner.getId() + " and " + lastHopOnPath, Logger.RED, msg.getSize(), msg.getWavelength());
                simulator.addStat(owner, Stat.OCS_CIRCUIT_PART_OF_CONFLICT);
                break;
            }
        }
        //Forward the message
        if (route.getSource().equals(owner)) {
            simulator.putLog(owner.getCurrentTime(), "End of tear down reached " + route, Logger.RED, msg.getSize(), msg.getWavelength());
            return true;
        } else {
            return owner.sendNow(route.get(index - 1), msg);
        }
    }

    @Override
    public boolean putMsgOnLink(GridMessage message, GridOutPort port, Time t) {
        //XXX: Esto puede significar q se esta haciendo en el plano de control
        if (message.getSize() == 0) {
            return owner.send(port, message, owner.getCurrentTime());
        }

        double bandwidthFree = owner.getFreeBandwidth(port, message.getWavelengthID(), t);
        int channelSize = owner.getChannelsSize(port, message.getWavelengthID(), t);

        int trafficPriority = 1;
        Entity source = message.getSource();
        Entity destination = message.getDestination();

        if (source instanceof ClientNode) {
            trafficPriority = ((ClientNode) source).getState().getTrafficPriority();
        } else if (destination instanceof ClientNode) {
            trafficPriority = ((ClientNode) destination).getState().getTrafficPriority();
        } else {
            System.out.println("Esto es un error en la asignacion de la prioridad del trafico del cliente.");
        }

        double b = getBandwidthToGrant(bandwidthFree, trafficPriority, channelSize);

        double linkSpeed = port.getLinkSpeed();

        if ((100 * (bandwidthFree / linkSpeed)) <= PERCENTAGE_TO_DROP_OCS) {
            return false;
        }

//        if (owner.isOutPortFree(port, message.getWavelengthID(), t)) {   
        if (owner.isAnyChannelFree(b, port, message.getWavelengthID(), t)) {

            double messageSize = message.getSize();
            double speed = port.getSwitchingSpeed();
            double sendTime = messageSize / speed;

            //Calculate the portFreeAgainTime, the time the link will be free again
            Time portFreeAgainTime = new Time(0);
            Time reachingTime = new Time(0);

            Entity entitySource = message.getSource();
            Entity entityDestination = message.getDestination();


            if (entitySource instanceof HybridClientNodeImpl) {
                HybridClientNodeImpl clientNodeImpl = (HybridClientNodeImpl) entitySource;
                OBSSender obsSender = (OBSSender) ((HyrbidEndSender) clientNodeImpl.getSender()).getObsSender();
                Map<String, GridOutPort> routingMap2 = ((OBSSender) obsSender).getRoutingMap();
                GridOutPort gridOutPort = routingMap2.get(entityDestination.getId());
                entitySource = (HybridSwitchImpl) gridOutPort.getTarget().getOwner();
            }
            if (entityDestination instanceof HybridResourceNode) {
                HybridResourceNode hybridResourceNode = (HybridResourceNode) entityDestination;
                OBSSender obsSender = (OBSSender) ((HyrbidEndSender) hybridResourceNode.getSender()).getObsSender();
                Map<String, GridOutPort> routingMap2 = ((OBSSender) obsSender).getRoutingMap();
                GridOutPort gridOutPort = routingMap2.get(entitySource.getId());
                entityDestination = (HybridSwitchImpl) gridOutPort.getTarget().getOwner();
            }

            LambdaChannelGroup.Channel channel = owner.reserve(entitySource, entityDestination, b, port, message.getWavelengthID(), t, message);

            if (speed > 0) {
                portFreeAgainTime.addTime(sendTime);
                reachingTime.addTime(messageSize / channel.getChannelSpeed());
//               channel.setFreeAgainTime(reachingTime.getTime());
            }
            portFreeAgainTime.addTime(t);
            reachingTime.addTime(t);
//
//            System.out.println("*EN OCSSWISENDER* Puerto:" + port.toString() + " Mensaje:" + message + " Lamda:" + message.getWavelengthID());
//            System.out.println("  Tamaño:" + messageSize + " Vel.Comutacion:" + speed + " Vel.Canal:" + linkSpeed + " Vel.Channel:" + channel.getChannelSpeed());
//            System.out.println("  tiempo para libre channel:" + reachingTime +" Prioridad del trafico:"+trafficPriority+ ". b:" + b + ". Cantidad de canales:" + channelSize + ". BandwidthFree:" + bandwidthFree);

            //update linkusage mappings

            Map<Integer, Time> map = owner.getPortUsage().get(port);
            map.put(new Integer(message.getWavelengthID()), portFreeAgainTime);
            owner.getPortUsage().put(port, map);

            return owner.send(port, message, reachingTime);
        } else {
            return false;
        }
    }

    /**
     * @author sid
     * Will try to tear down a OCS circuit. The OCSTeardownMessage
     * get forwarded on the circuit and with each hop the circuit get's torn
     * down.
     *
     * @param destination The end destination of the circuit.
     * @param wavelength The first wavelength which is in the begining of the 
     * circuit that we want to teardown.
     * @param outport The @link{GridOutPort} which represents the physical link
     * on which the circuits lies on.
     * @param time The time this circuit has to be torn down
     * @return true if tear down worked, false if not.
     */
    public boolean tearDownOCSCircuit(Entity destination, int wavelength, GridOutPort outport, Time time) {

        List OCSsFound = simulator.returnOcsCircuit(owner, destination);

        if (OCSsFound != null && OCSsFound.size() > 0) {

            Iterator<OCSRoute> OCSsFoundIt = OCSsFound.iterator();

            while (OCSsFoundIt.hasNext()) {

                OCSRoute OCSRoute = OCSsFoundIt.next();
                ArrayList<SimBaseOutPort> ownerOutPorts = owner.getOutPorts();

                for (SimBaseOutPort ownerOutPort : ownerOutPorts) {

                    if (ownerOutPort.equals(outport) && ((GridOutPort) ownerOutPort).isWaveUsedInCircuit(wavelength) && OCSRoute.getWavelength() == wavelength) {

                        StringBuffer OCSTeardownMsgId = new StringBuffer();
                        OCSTeardownMsgId.append("OCSTearDown:").append(owner).append("-").append(destination);
                        OCSTeardownMessage teardownMsg = new OCSTeardownMessage(OCSTeardownMsgId.toString(), time, -1);

                        teardownMsg.setWavelengthID(wavelength);
                        teardownMsg.setSource(owner);
                        teardownMsg.setDestination(destination);
                        teardownMsg.setOcsRoute(OCSRoute);
                        teardownMsg.getRoute().clear();
                        teardownMsg.setOutport(outport);
//                        teardownMsg.initRoute();
                        //return owner.sendSelf(teardownMsg, time);
                        return owner.sendNow(owner, teardownMsg, time);
                    }
                }
            }
        }

        return false;
    }
    
}
