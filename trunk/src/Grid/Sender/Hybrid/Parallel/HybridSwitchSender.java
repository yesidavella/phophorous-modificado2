package Grid.Sender.Hybrid.Parallel;

import Grid.Entity;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.*;
import Grid.Nodes.Hybrid.Parallel.HybridClientNodeImpl;
import Grid.Nodes.Hybrid.Parallel.HybridResourceNode;
import Grid.Nodes.Hybrid.Parallel.HybridSwitchImpl;
import Grid.Nodes.PCE;
import Grid.OCS.OCSRoute;
import Grid.OCS.stats.ManagerOCS;
import Grid.Port.GridOutPort;
import Grid.Route;
import Grid.Sender.OBS.OBSSender;
import Grid.Sender.OBS.OBSSwitchSenderImpl;
import Grid.Sender.OBS.OBSWavConSwitchSender;
import Grid.Sender.OCS.OCSSwitchSender;
import Grid.Sender.Sender;
import Grid.Utilities.Config;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import simbase.Port.SimBaseInPort;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class HybridSwitchSender extends AbstractHybridSender {

    private Queue<GridMessage> messageQueue;

    /**
     * Constructor
     *
     * @param owner The owner of this sender.
     * @param simulator The simulator.
     */
    public HybridSwitchSender(Entity owner, GridSimulator simulator, boolean wavelengthConversion) {
        super(owner, simulator);
        ocsSender = new OCSSwitchSender(simulator, owner, GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OCSSetupHandleTime));
        messageQueue = new ArrayBlockingQueue<GridMessage>(10);
        if (wavelengthConversion) {
            obsSender = new OBSWavConSwitchSender(owner, simulator);
        } else {
            obsSender = new OBSSwitchSenderImpl(simulator, owner);
        }
    }

    /**
     * @author AG2 team overloaded Constructor
     * @param owner The owner of this sender.
     * @param simulator The simulator.
     */
    public HybridSwitchSender(Entity owner, GridSimulator simulator, boolean wavelengthConversion,
            double costFindCommonWavelenght, double costAllocateWavelenght) {
        super(owner, simulator);
        ocsSender = new OCSSwitchSender(simulator, owner, costFindCommonWavelenght, costAllocateWavelenght);
        messageQueue = new ArrayBlockingQueue<GridMessage>(10);
        if (wavelengthConversion) {
            obsSender = new OBSWavConSwitchSender(owner, simulator);
        } else {
            obsSender = new OBSSwitchSenderImpl(simulator, owner);
        }
    }
    int countOCS = 0;// FIXME: solo para pruebas

    /**
     * This method sends the message into the network. Depending on wheter the
     * message is an OCS or an OBS message.
     *
     * @param message The message to send
     * @param t The time of sending
     * @return true if sending worked, false if not.
     */
    //NOTA: Donde se verifica el si existe un CIRCUITO  y si se usa o se crea otro.
    public boolean send(GridMessage message, SimBaseInPort inport, final Time t) {

        //testTearDownOCSs(t);
        if (((OCSSwitchSender) ocsSender).send(message, inport, t, false)) {
            //message was send on a circuit
            return true;
        } else {
            
             addTrafficToOCS(message);
            //This is not a part of an OCS circuit, but could be the beginning of one
            Entity destination = message.getDestination();
            //Check whether the destination can be reached via hybrid sending
            Map routingMap = ((OBSSender) obsSender).getRoutingMap();

            if (routingMap.containsKey(destination.getId())) {

                List<OCSRoute> ocsRoutes = null;
                Route hopRouteToDestination = simulator.getPhysicTopology().findOCSRoute(owner, destination);

                if (hopRouteToDestination.size() <= 2) {//Last switch reached in the backbone
                    //If is looking for another ocs to put the msg, could end up leaving 
                    //another ocs. Need to check if is necessary to teardown an ocs
                  ((OCSSwitchSender) ocsSender).checkForTeardownOCSs(message, t);
                   
                    return obsSender.send(message, t, true);

                } else if (message instanceof MultiCostMessage ) {
                    //Could be the HEAD or an intermediate switch node
                    MultiCostMessage multiCostMsg = (MultiCostMessage) message;

                    if (!multiCostMsg.isRealMarkovCostEvaluated() && Sender.isAg2ResourceSelectorSelected()) {
                        //It should enter just the first time when the JobMsg arrive at a 
                        //switch (in the HEAD switch of the OCS), NOT latter switches, just one time per JobMsg.
                        PCE domainPCE = multiCostMsg.getDomainPCE();
                        double networkMarkovCost = domainPCE.getNetworkMarkovCost(multiCostMsg.getDestination(), multiCostMsg.getSource(), multiCostMsg.getSize(), PCE.TRACK_INSTRUCTION, multiCostMsg.getOCS_Instructions());
                        multiCostMsg.setRealNetworkCost(networkMarkovCost);
                        multiCostMsg.setRealMarkovCostEvaluated(true);
                    }

                    if (!multiCostMsg.getOCS_Instructions().isEmpty()) {
                        //If this node is the HEAD or an intermediate switch node and have instruction to create OCS´s
                        OCSRoute ocsRouteToCreateExecuted = null;

                        for (OCSRoute oneOCSInstruction : multiCostMsg.getOCS_Instructions()) {

                            if (oneOCSInstruction.getSource().equals(owner)) {
//                                System.out.println("Crear OCS con ID:"+jobMsg.getId()+" Origen:" + oneOCSInstruction.getSource() + "->" + oneOCSInstruction.getDestination() + " El msg:" + jobMsg.getId() + " en tiempo:" + t);
                                multiCostMsg.setReSent(true);
                                multiCostMsg.setHybridSwitchSenderInWait(this);
                                multiCostMsg.setInportInWait(inport);
                                messageQueue.offer(multiCostMsg);

                                ocsRouteToCreateExecuted = simulator.getPhysicTopology().findOCSRoute(oneOCSInstruction.getSource(), oneOCSInstruction.getDestination());
                                ocsRouteToCreateExecuted.setIdJobMsgRequestOCS(multiCostMsg.getId());
                                owner.requestOCSCircuit(ocsRouteToCreateExecuted, true, t);
                                multiCostMsg.getOCS_Instructions().remove(oneOCSInstruction);
                                countOCS++;
                                break;
                            }
                        }
                        if (ocsRouteToCreateExecuted != null) {
                            multiCostMsg.getOcsExecutedInstructions().add(ocsRouteToCreateExecuted);
                            return false;
                        }
                    }
                }

                for (int i = hopRouteToDestination.size() - 2; i >= 1; i--) {

                    Entity backwardHop = hopRouteToDestination.get(i);
                    ocsRoutes = simulator.returnOcsCircuit(owner, backwardHop);

                    if (ocsRoutes != null) {
                        break;
                    }
                }

                //If is looking for another ocs to put the msg, MAYBE JUST LEFT
                //another ocs. Need to check if is necessary to teardown THE JUST
                //LEFT ocs
                ((OCSSwitchSender) ocsSender).checkForTeardownOCSs(message, t);
            
                if (ocsRoutes != null) {

                    Iterator<OCSRoute> routeIterator = ocsRoutes.iterator();

                    while (routeIterator.hasNext()) {

                        final OCSRoute ocsRoute = routeIterator.next();
                        if (ocsRoute != null) {
                            //There is an OCS route to the next virtual hop
                            Entity nextRealHop = ocsRoute.findNextHop(owner);
                            final GridOutPort theOutPort = owner.findOutPort(nextRealHop);
                            //the beginning wavelength
                            final int theOutgoingWavelength = ocsRoute.getWavelength();
                            // we start sending using a new wavelength (OCS circuit)
                            message.setWavelengthID(theOutgoingWavelength);
                            

                            boolean canSend = false;

                            if (!message.isReSent()) {
                                canSend = true;
                            } else {
                                canSend = message.getId().equalsIgnoreCase(ocsRoute.getIdJobMsgRequestOCS());
                            }

                            if (canSend) {
                                if (ocsSender.putMsgOnLink(message, theOutPort, t, true, ocsRoute.size() - 2)) {
                                    message.setOCSRoute(ocsRoute);
                                    message.setFirstWaveLengthID(theOutgoingWavelength);
                                    message.setFirstSwitch((HybridSwitchImpl) owner);

                                    message.setReSent(false);
                                    message.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);

                                    return true;
                                }
                            }
                        }
                    }

                    //Link is busy, but we can try to send it via LSP default..(inten por lsp a la cero)
                    OCSRoute ocsRoute = ocsRoutes.get(0);
                    Entity nextHopOnPath = ocsRoute.findNextHop(owner);
                    if (nextHopOnPath != null) {

                        ocsRoutes = simulator.returnOcsCircuit(owner, hopRouteToDestination.get(1));

                        if (ocsRoutes != null) {
                            for (OCSRoute ocsRoute1 : ocsRoutes) {
                                Entity nextRealHop = ocsRoute.findNextHop(owner);
                                GridOutPort theOutPort = owner.findOutPort(nextRealHop);
                                //the beginning wavelength
                                int theOutgoingWavelength = ocsRoute1.getWavelength();
                                
                                // we start sending using a new wavelength (OCS circuit)
                                message.setWavelengthID(theOutgoingWavelength);
                                
                                //We try to send
                                if (ocsSender.putMsgOnLink(message, theOutPort, t, true, ocsRoute1.size() - 2)) {
                                    message.setOCSRoute(ocsRoute);
                                    message.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);

                                    message.setFirstWaveLengthID(theOutgoingWavelength);
                                    message.setFirstSwitch((HybridSwitchImpl) owner);

//                                //System.out.println(" Switch via OCS  Msg es comienzo " + inport.getID());
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
//                    throw new IllegalStateException("NO tiene que conmutar OBC - la opcion tiene que ser LSP por defecto");
                    return obsSender.send(message, t, true);
                }

            } else {
                return false;
                // the next hop is not in the routing map so no sending is possible
            }
        }

    }

    /**
     * Method which forwards the messages. The inport is not needed, and
     * automatic searching of the incoming link is done. Though, if there are
     * two links interconnecting the owner and the former hop, the correct link
     * will not necessarily be chosen.
     *
     * @param message The message to forward
     * @param t The time to forward
     * @return True if sending worked, false if not
     */
    @Override
    public boolean send(GridMessage message, Time t, boolean outputFail) {
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
                            if (ocsSender.putMsgOnLink(message, theOutPort, t, true, ocsRoute.size() - 2)) {
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

    public boolean handleOCSPathSetupMessage(OCSRequestMessage m, SimBaseInPort inport) {

        boolean result = ((OCSSwitchSender) ocsSender).handleOCSPathSetupMessage(m, inport);
        ManagerOCS.getInstance().addWavelengthID(m, m.getWavelengthID(), owner);
        return result;
    }

    public void rollBackOCSSetup(OCSRoute ocsRoute) {

        ((OCSSwitchSender) ocsSender).rollBackOCSSetup(ocsRoute);
    }

    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        ((OCSSwitchSender) ocsSender).requestOCSCircuit(ocsRoute, permanent, time);
    }

    public boolean handleTearDownOCSCircuit(OCSTeardownMessage msg, SimBaseInPort inport) {
        return ((OCSSwitchSender) ocsSender).handleTearDownOCSCircuit(msg, inport);
    }

    public boolean handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        return ((OCSSwitchSender) ocsSender).handleOCSSetupFailMessage(msg);
    }

    /**
     * @param destination Head-end of the OCS circuit.
     * @param firstWavelength first lambda of the OCS
     * @param port
     * @param time
     */
    public boolean teardDownOCSCircuit(Entity destination, int firstWavelength, GridOutPort port, Time time) {
        return ((OCSSwitchSender) ocsSender).tearDownOCSCircuit(destination, firstWavelength, port, time);
    }

    public double calculateRealMarkovCostList(JobMessage realJobMsg) {
        return 0;
    }

    public boolean handleConfirmMessage(OCSConfirmSetupMessage msg, Time time) {
        return ((OCSSwitchSender) ocsSender).confirmOCSMessage(msg, messageQueue, time);
    }

    public void handleOCSRequestTeardownMessage(OCSRequestTeardownMessage requestTeardownMsg, Time time) {
        ((OCSSwitchSender) ocsSender).handleOCSRequestTeardownMessage(requestTeardownMsg, time);
    }

    public void addTrafficToOCS(GridMessage message) {
        
        //HybridSwitchImpl edgeRouterByEndNode = ((HybridSwitchImpl) owner).getEdgeRouterByEndNode(message.getSource(), message.getDestination());

      if (message.getFirstSwitch()!= null && message instanceof GridMessage)
        {
            if(message instanceof MultiCostMessage)
            {
                if(((MultiCostMessage)message).isReSent()){
                    return;
                }
            }
//            System.out.println("Verificando fin de ocs en: "+owner+" Mensaje "+message);
            ManagerOCS.getInstance().addTraffic(message, message.getFirstSwitch(), (HybridSwitchImpl) owner, message.getOcsRoute().getWavelength());
        }
    }
}
