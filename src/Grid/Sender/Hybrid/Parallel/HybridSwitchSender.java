/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Sender.Hybrid.Parallel;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.OCS.OCSRoute;
import Grid.Port.GridInPort;
import Grid.Port.GridOutPort;
import Grid.Sender.OBS.OBSSender;
import Grid.Sender.OBS.OBSSwitchSenderImpl;
import Grid.Sender.OBS.OBSWavConSwitchSender;
import Grid.Sender.OCS.OCSSwitchSender;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import simbase.Port.SimBaseInPort;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class HybridSwitchSender extends AbstractHybridSender {

    /**
     * Constructor
     * @param owner The owner of this sender.
     * @param simulator The simulator. 
     */
    public HybridSwitchSender(Entity owner, GridSimulator simulator, boolean wavelengthConversion) {
        super(owner, simulator);
        ocsSender = new OCSSwitchSender(simulator, owner, 0.1);
        if (wavelengthConversion) {
            obsSender = new OBSWavConSwitchSender(owner, simulator);
        } else {
            obsSender = new OBSSwitchSenderImpl(simulator, owner);
        }


    }

    /**
     * This method sends the message into the network. Depending on wheter
     * the message is an OCS or an OBS message.
     * @param message The message to send
     * @param t The time of sending
     * @return true if sending worked, false if not.
     */
   //NOTA: Donde se verifica el si existe un CIRCUITO  y si se usa o se crea otro.
    public boolean send(GridMessage message, SimBaseInPort inport, Time t) {
        if (((OCSSwitchSender) ocsSender).send(message, inport, t, false)) {
            //message was send on a circuit
            return true;
        } else {
//            System.out.println("obssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
            //This is not a part of an OCS circuit, but could be the beginning of one
            Entity destination = message.getDestination();
            //Check whether the destination can be reached via hybrid sending
            Map routingMap = ((OBSSender) obsSender).getRoutingMap();
            if (routingMap.containsKey(destination.getId())) {
                GridOutPort virtualOutport = (GridOutPort) routingMap.get(destination.getId());
                Entity virtualHop = (Entity) virtualOutport.getTarget().getOwner();
                //Sending is possible
                // Is there a circuit to the nextVirtualhop?
                List<OCSRoute> ocsRoutes = simulator.returnOcsCircuit(owner, virtualHop);
                //no circuits so this means that the next hop is a OBS hop
                if (ocsRoutes != null) {
                    Iterator<OCSRoute> routeIterator = ocsRoutes.iterator();

                    while (routeIterator.hasNext()) {
                        OCSRoute ocsRoute = routeIterator.next();
                        if (ocsRoute != null) {
                            //There is an OCS route to the next virtual hop
                            Entity nextRealHop = ocsRoute.findNextHop(owner);
                            GridOutPort theOutPort = owner.findOutPort(nextRealHop);
                            //the beginning wavelength
                            int theOutgoingWavelength = ocsRoute.getWavelength();

                            // we start sending using a new wavelength (OCS circuit)
                            message.setWavelengthID(theOutgoingWavelength);
                            //We try to send
                            if (this.putMessageOnLink(message, theOutPort, t)) {
                                message.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);
                                return true;
                            }
                        }
                    }
                    OCSRoute ocsRoute = ocsRoutes.get(0);
                    //Link is busy, but we can try to send it via OBS
                    Entity nextHopOnPath = ocsRoute.findNextHop(owner);
                    if (nextHopOnPath != null) {
                        return ((OBSWavConSwitchSender) obsSender).send(message, t, true, nextHopOnPath);
                    } else {
                        return false;
                    }
                } else {
                    return obsSender.send(message, t, true);
                }

            } else {
                return false;
            // the next hop is not in the routing map so no sending is possible
            }
        }

    }

    /**
     * Method which forwards the messages. The inport is not needed, and automatic 
     * searching of the incoming link is done. Though, if there are two links
     * interconnecting the owner and the former hop, the correct link will
     * not necessarily be chosen.
     * @param message The message to forward
     * @param t The time to forward
     * @return True if sending worked, false if not
     */
    @Override
    public boolean send(GridMessage message, Time t,
            boolean outputFail) {
        if (((OCSSwitchSender) ocsSender).send(message, t, false)) {
            //message was send on a circuit
            return true;
        } else {
            //This is not a part of an OCS circuit, but could be the beginning of one
            Entity destination = message.getDestination();
            //Check whether the destination can be reached via hybrid sending
            Map routingMap = ((OBSSender) obsSender).getRoutingMap();
            if (routingMap.containsKey(destination.getId())) {
                GridOutPort virtualOutport = (GridOutPort) routingMap.get(destination.getId());
                Entity virtualHop = (Entity) virtualOutport.getTarget().getOwner();
                //Sending is possible
                // Are there a circuit to the nextVirtualhop?
                List<OCSRoute> ocsRoutes = simulator.returnOcsCircuit(owner, virtualHop);
                if (ocsRoutes != null) {
                    Iterator<OCSRoute> routeIterator = ocsRoutes.iterator();

                    while (routeIterator.hasNext()) {
                        OCSRoute ocsRoute = routeIterator.next();
                        if (ocsRoute != null) {
                            //There is an OCS route to the next virtual hop
                            int index = ocsRoute.indexOf(owner);
                            Entity nextRealHop = ocsRoute.get(index + 1);

                            GridOutPort theOutPort = owner.findOutPort(nextRealHop);
                            //the beginning wavelength
                            int theOutgoingWavelength = ocsRoute.getWavelength();

                            // we start sending using a new wavelength (OCS circuit)
                            message.setWavelengthID(theOutgoingWavelength);
                            //We try to send
                            if (this.putMessageOnLink(message, theOutPort, t)) {
                                return true;
                            }
                        }
                    }
                    OCSRoute ocsRoute = ocsRoutes.get(0);
                    //Link is busy, but we can try to send it via OBS
                    int indexOfThisEntity = ocsRoute.indexOf(message.getSource());
                    if (indexOfThisEntity < ocsRoute.size()) {
                        Entity nextHopOnPath = ocsRoute.get(indexOfThisEntity + 1);
                        return ((OBSWavConSwitchSender) obsSender).send(message, t, true, nextHopOnPath);
                    } else {
                        return false;
                    }
                }

            } else {
                return obsSender.send(message, t, true);
            }
        }
        return false;
    }

    public boolean handleOCSPathSetupMessage(OCSRequestMessage m,
            SimBaseInPort inport) {

        return ((OCSSwitchSender) ocsSender).handleOCSPathSetupMessage(m, inport);
    }

    public void rollBackOCSSetup(OCSRoute ocsRoute) {

        ((OCSSwitchSender) ocsSender).rollBackOCSSetup(ocsRoute);
    }

    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        ((OCSSwitchSender) ocsSender).requestOCSCircuit(ocsRoute, permanent, time);
    }

    public boolean handleTearDownOCSCircuit(OCSTeardownMessage msg, GridInPort inport) {
        return ((OCSSwitchSender) ocsSender).handleTearDownOCSCircuit(msg, inport);
    }

    public boolean handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        return ((OCSSwitchSender) ocsSender).handleOCSSetupFailMessage(msg);
    }
}
