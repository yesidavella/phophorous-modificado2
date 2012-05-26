/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Sender.OCS;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.OCSConfirmSetupMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Interfaces.Switch;
import Grid.Nodes.LinkWavelengthPair;
import Grid.OCS.OCSRoute;
import Grid.OCS.stats.ManagerOCS;
import Grid.Port.GridInPort;
import Grid.Port.GridOutPort;
import Grid.Route;
import Grid.Sender.Sender;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import simbase.Port.SimBaseInPort;
import simbase.Port.SimBaseOutPort;
import simbase.Port.SimBasePort;
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
                    simulator.putLog(simulator.getMasterClock(), "FAIL: Sending failed because no reservation is made for " +
                            owner.getId() + " --> " + msg.getDestination().getId(), Logger.RED, msg.getSize(), msg.getWavelengthID());
                }
                return false;
            } else {
//                 System.out.println("#################### tiene outgoingpir  ##################### " );
                System.out.println(" Switch via OCS  Msg  "+msg +" inPort "+inPort+ " incomingPair " + incomingPair + " outgoingPair " + outgoingPair);
                msg.setWavelengthID(outgoingPair.getWavelength());

                return this.putMessageOnLink(msg, (GridOutPort) outgoingPair.getPort(), t);
            }
        }
    }

    /**
     * Will send a message into the network. For this method the route object
     * in the GridMessage must be correct, otherwise this method will output
     * false results.
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
     * @param m The OCSRequestmessage which inited the circuit setup.
     * @param requester The requester for which the setup is needed.
     * @param inport The inport on which the OCSRequestmessage entered the requester.
     * @return True if forwarding is needed, false if not
     */
    public boolean handleOCSPathSetupMessage(OCSRequestMessage m,
            SimBaseInPort inport) {
        m.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);
        OCSRoute route = m.getOCSRoute();
        Time addedTime = new Time(owner.getCurrentTime().getTime() + OCSSetupHandle);
        //Check if this hop is the last on the circuit
        if (route.getDestination().equals(owner)) {
            simulator.putLog(simulator.getMasterClock(), "<u>OCS: end of OCS Path reached" + m.getOCSRoute() + "</u>", Logger.ORANGE, m.getSize(), m.getWavelengthID());
            simulator.addStat(owner, Stat.OCS_CIRCUIT_SET_UP);
            
          
            
            ManagerOCS.getInstance().confirmInstanceOCS(m, addedTime.getTime());
            if (m.isPermanent()) {
                simulator.confirmRequestedCircuit(route);
            }
            if (!route.getSource().supportSwitching()) {
                //only send confirmation to edge nodes
                
                OCSConfirmSetupMessage confirm = new OCSConfirmSetupMessage("confirm:" + route.getSource() + "-" + route.getDestination(), addedTime, route);               
                
                confirm.setSource(owner);
                confirm.setWavelengthID(-1);
                confirm.setDestination(route.getSource());              
                 
                owner.sendNow(confirm.getDestination(), confirm, addedTime);
                
            }
            return true; //nothing should be done, end of circuit has been reached
        } else {
            //this is not the destination where on the path we are
            //search for outport to send to the next hop
            int index = route.indexOf(owner);
            Iterator<SimBaseOutPort> outPortsIterator = owner.getOutPorts().iterator();
            GridOutPort outPort = null;
            Entity newHopOnPath = route.findNextHop(owner);
            while (outPortsIterator.hasNext()) {
                outPort = (GridOutPort) outPortsIterator.next();
                if (outPort.getID().startsWith(owner.getId()) &&
                        outPort.getID().endsWith(newHopOnPath.getId())) {
                    break;
                } else {
                    continue;
                }
            }


            //Search for the incoming port, because OCS Setup messages are send through the 
            //control plane (self-gridInPort)
            Iterator<SimBaseInPort> inPortsIterator = owner.getInPorts().iterator();
            GridInPort gridInPort = null;
            Entity previousHopOnPath = null;
            if (!m.getSource().equals(owner)) {
                previousHopOnPath = route.get(index - 1);
            } else {


                //The owner made a request, this is a switch so the request just needs to be forwarded. And a wavelengths needs to be reserved
                // This can only be done in case of hybrid switching of course
                if (owner.supportsOBS() && owner.supportsOCS()) {
                    //Find a free wave length for the beginning of the path
                    int beginningWavelength = outPort.getNexFreeWavelength();
                    
                    
                    if (beginningWavelength != -1) {
                        route.setWavelength(beginningWavelength);
                        m.setWavelengthID(beginningWavelength);
                        outPort.addWavelength(beginningWavelength);
                        if (owner.sendNow(newHopOnPath, m, addedTime)) {
                            if (m.isPermanent()) {
                                simulator.addRequestedCircuit(route);
                            }

                            simulator.putLog(simulator.getMasterClock(), "OCS: OCS requestmessage send from <b>" + owner.getId() + "</b> to <b>" + newHopOnPath + "</b> "+"for <b>"+route.getDestination()+"</b> reserving wavelength <b>" + beginningWavelength + " </b>", Logger.ORANGE, m.getSize(), m.getWavelengthID());
                            return true;
                        } else {
                            simulator.putLog(simulator.getMasterClock(), "OCS: OCS Requestmessage could not be send <b>" + owner.getId() + "</b> to <b>" + newHopOnPath + "</b>", Logger.ORANGE, m.getSize(), m.getWavelengthID());
                            ManagerOCS.getInstance().notifyError(m, addedTime.getTime(), owner, "OCS Requestmessage could not be send" );
                            return false;
                        }
                    } else {
                        simulator.putLog(simulator.getMasterClock(), "OCS: OCS setup could not be realized because no free wavelength could be found on </b>" + owner.getId() + "</b> to <b>" + newHopOnPath + "</b>", Logger.RED, m.getSize(), m.getWavelengthID());
                        ManagerOCS.getInstance().notifyError(m, addedTime.getTime(), owner, "OCS setup could not be realized because no free wavelength could be found " );
                        return false;
                    }
                }
            }
            while (inPortsIterator.hasNext() && previousHopOnPath != null) {
                gridInPort = (GridInPort) inPortsIterator.next();
                if (gridInPort.getID().startsWith(previousHopOnPath.getId())) {
                    break;
                } else {
                    continue;
                }
            }

            // There is no match between inport and outport
            if (outPort == null || gridInPort == null) {
                simulator.putLog(simulator.getMasterClock(), "FAIL: OCS setup failed because of outport/inport mismatch " +
                        owner.getId(), Logger.RED, m.getSize(), m.getWavelengthID());
                simulator.addStat(owner, Stat.OCS_CIRCUIT_SETUP_DID_NOT_WORK);
                if (m.isPermanent()) {
                    simulator.cancelRequestedCircuit(route);
                }
                //Undo all changes made in previous steps (ONLY when there are steps done)
                if (!m.getSource().equals(owner)) {
                    rollBackOCSSetup(route);
                }
                 ManagerOCS.getInstance().notifyError(m, addedTime.getTime(), owner, "OCS setup failed because of outport/inport mismatch " );
                return false;
            } else {
                //Found outport, know the wavelength -->update linktable (wavelength here is wavelengthId)
                //because wavelength of the OCSRoute is the beginning wavelength which could already 
                //have been changed earlier
                //First find earlier connections if there are 


                LinkWavelengthPair incomingPair = new LinkWavelengthPair(gridInPort, m.getWavelengthID());
                if (linkMapping.containsKey(incomingPair)) {


                    simulator.putLog(owner.getCurrentTime(), "FAIL: OCS " + owner.getId() + " got a OCS setup message for a part of an " +
                            "already existing circuit... [route : " + route + "] " + incomingPair, Logger.RED, route.getWavelength(), (int) m.getSize());

                    rollBackOCSSetup(route);
                    simulator.addStat(owner, Stat.OCS_CIRCUIT_CONFLICT);
                    if (m.isPermanent()) {
                        simulator.cancelRequestedCircuit(route);
                    }
                     ManagerOCS.getInstance().notifyError(m, addedTime.getTime(), owner,  " got a OCS setup message for a part of an " +
                            "already existing circuit... [route : " + route + "] " );
                    return false;
                }

                int newWaveLength;
                //can we use the same wavelength on which the message got in?
                if (!outPort.isWaveUsedInCircuit(m.getWavelengthID())) {
                    newWaveLength = m.getWavelengthID();
                } else {
                    //Wavelength already in use, try to find a new one
                    newWaveLength = outPort.getNexFreeWavelength();
                }
             

                if (newWaveLength != -1) {
                    m.setWavelengthID(newWaveLength);
                    LinkWavelengthPair outGoingPair = new LinkWavelengthPair(outPort, newWaveLength);
                    outPort.addWavelength(newWaveLength);
                    linkMapping.put(incomingPair, outGoingPair);
                    simulator.putLog(simulator.getMasterClock(), "OCS: OCS link setup between <b>" + owner.getId() +
                            "</b> and <b>" + newHopOnPath + "</b> on " + newWaveLength + " " + route, Logger.ORANGE, m.getSize(), m.getWavelengthID());
                    simulator.addStat(owner, Stat.OCS_PART_OF_CIRCUIT_SET_UP);

                    if (owner.sendNow(newHopOnPath, m, addedTime)) {
                        //simulator.putLog(simulator.getMasterClock(), "OCS: OCS requestmessage send from <b>" + owner.getId() + "</b> to <b>" + newHopOnPath + "</b>", Logger.ORANGE, m.getSize(), m.getWavelengthID());
                        return true;
                    } else {
                        simulator.putLog(simulator.getMasterClock(), "OCS: OCS Requestmessage could not be send <b>" + owner.getId() + "</b> to <b>" + newHopOnPath + "</b>", Logger.RED, m.getSize(), m.getWavelengthID());
                         ManagerOCS.getInstance().notifyError(m, addedTime.getTime(), owner, "OCS Requestmessage could not be send " );
                        return false;
                    }
                } else {
                    //No new wavelengths could be found. Undo all changes.
                    simulator.putLog(simulator.getMasterClock(), "OCS: OCS setup could not be realized because no free wavelength could be found on </b>" + owner.getId() + "</b> to <b>" + newHopOnPath + "</b>", Logger.RED, m.getSize(), m.getWavelengthID());
                     ManagerOCS.getInstance().notifyError(m, addedTime.getTime(), owner, " OCS setup could not be realized because no free wavelength could be found on " );
                    rollBackOCSSetup(route);
                    return false;
                }
            }
        }
    }

    /**
     * This methodes is executed when somehting went wrong during the setup of 
     * the OCS circuit.
     * @param ocsRoute The ocs route which failed.
     */
    public void rollBackOCSSetup(OCSRoute ocsRoute) {
        OCSSetupFailMessage setupFailMsg = new OCSSetupFailMessage("OCS-SetupFailMessage " + ocsRoute.getSource() + "--" + ocsRoute.getDestination(),
                owner.getCurrentTime(), ocsRoute.getWavelength(), ocsRoute);
        owner.sendNow(ocsRoute.get(ocsRoute.indexOf(owner) - 1), setupFailMsg);
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

        OCSRequestMessage request = new OCSRequestMessage(buffer.toString(), time, ocsRoute, permanent);
        //must be -1 --> control plane
        request.setWavelengthID(-1);
        //Sends the OCS circuit request to the source (could be this 
        //entity) and lets the receive method handle the OCS request.
        owner.sendNow(ocsRoute.getSource(), request, time);

    }

    /**
     * Will handle an @link{OCSTeardownMessage}. It will forward the message
     * and if succesfull it will remove the mapping from the routing table. 
     * @param msg The OCSTeardownMessage
     * @param inport The inport on which we received the OCSTeardownMessage.
     * @return True if tear down worker, false if not.
     */
    public boolean handleTearDownOCSCircuit(OCSTeardownMessage msg, GridInPort inport) {
        if (send(msg, inport, owner.getCurrentTime(), true)) {
            //forwarding of the msg worked
            try {
                LinkWavelengthPair incomingPair = new LinkWavelengthPair(inport, msg.getWavelenght());
                linkMapping.remove(incomingPair);
                simulator.putLog(owner.getCurrentTime(), "OCS Circuit torn down between " + inport.getSource().getOwner() +
                        " and " + owner, Logger.GREEN, msg.getSize(), msg.getWavelenght());
                simulator.addStat(owner, Stat.OCS_CIRCUIT_TEAR_DOWN);
                return true;

            } catch (NullPointerException e) {
                simulator.putLog(owner.getCurrentTime(), "No mapping between incomming and outgoing pair could be found" +
                        "", Logger.RED, -1, msg.getWavelenght());
                return false;
            }
        } else {
            //Forwarding did not work - is there an OCS circuit setup?
            simulator.putLog(owner.getCurrentTime(), "Forwarding of OCScircuit teardown did not work" +
                    "", Logger.RED, -1, msg.getWavelenght());
            return false;
        }

    }

    public Map<LinkWavelengthPair, LinkWavelengthPair> getLinkMapping() {
        return linkMapping;
    }

    /**
     * Handles a OCSSetupFailMessage. This message is send when, at some part
     * of the OCS setup the setup fails and needs to be roll back.
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
            if (outPort.getID().startsWith(owner.getId()) &&
                    outPort.getID().endsWith(lastHopOnPath.getId())) {
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
}
