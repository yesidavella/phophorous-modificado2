package Grid.Sender.Hybrid.Parallel;

import Grid.Entity;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.Messages.*;
import Grid.Interfaces.ResourceNode;
import Grid.Nodes.AbstractServiceNode;
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
import Grid.Utilities.Config;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import simbase.Port.SimBaseInPort;
import simbase.Stats.Logger;
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
    public static boolean ocsTearDownSend = false;

    private void testTearDownOCSs(Time t) {

        Entity hibri1 = (Entity) simulator.getEntityWithId("Enrutador_Hibrido_1");
        Entity hibri2 = (Entity) simulator.getEntityWithId("Enrutador_Hibrido_2");
        Entity pce = (Entity) simulator.getEntityWithId("PCE1");

        List defOCS = simulator.returnOcsCircuit(hibri1, hibri2);
        List ocsHibr1PCE = simulator.returnOcsCircuit(hibri1, pce);


        if (owner.getId().equalsIgnoreCase("Enrutador_Hibrido_1") && !ocsTearDownSend) {
            ocsTearDownSend = true;
            Map routingMapOwner = ((OBSSender) obsSender).getRoutingMap();
            GridOutPort outPorttoDestination = (GridOutPort) routingMapOwner.get(hibri2.getId());
//            owner.teardDownOCSCircuit(hibri2, ((OCSRoute) defOCS.get(0)).getWavelength(), outPorttoDestination, t);

            if (ocsHibr1PCE != null) {
                GridOutPort outPorttoPCE = (GridOutPort) routingMapOwner.get(pce.getId());
                owner.teardDownOCSCircuit(pce, ((OCSRoute) ocsHibr1PCE.get(0)).getWavelength(), outPorttoPCE, t);
            }
        }

    }
    Runnable runnable; // FIXME: solo para pruebas
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

//        testTearDownOCSs(t);

        if (((OCSSwitchSender) ocsSender).send(message, inport, t, false)) {
            //message was send on a circuit
            return true;
        } else {

            //This is not a part of an OCS circuit, but could be the beginning of one
            Entity destination = message.getDestination();
            //Check whether the destination can be reached via hybrid sending
            Map routingMap = ((OBSSender) obsSender).getRoutingMap();
            if (routingMap.containsKey(destination.getId())) {

                List<OCSRoute> ocsRoutes = null;
                Route hopRouteToDestination = simulator.getPhysicTopology().findOCSRoute(owner, destination);

                if (hopRouteToDestination.size() <= 2) {
                    //FIXME: el analisis de markov debe tambien contener el teardown del OCS 
                    return obsSender.send(message, t, true); // significa que esta el mensaje el router de borde-
                } else if ((message instanceof JobMessage)) {
                    //It should enter just the first time when the JobMsg arrive
                    //at a switch (in the HEAD switch of the OCS) , NOT latter switches, just onw time per JobMsg. 
                    if (!((JobMessage) message).isRealMarkovCostEvaluated()) {
//                      System.out.print("HybridSwitchSender ID msg:" + message.getId());
//                       System.out.println("  Costo real de red "+calculateRealMarkovCostList((JobMessage) message));
                    }
                }

                for (int i = hopRouteToDestination.size() - 2; i >= 1; i--) {

                    Entity backwardHop = hopRouteToDestination.get(i);
                    ocsRoutes = simulator.returnOcsCircuit(owner, backwardHop);

                    if (ocsRoutes != null) {
                        break;
                    }
                }

                ////////////////////////////////////QUITAR pruebas de espera  de mensaje por creacion de OCS //////////////////////
//
//                System.out.println("Trantando de enviar mensaje: "+message+" reenviado:"+message.isReSent()+" EN: "+owner+" Time:"+owner.getCurrentTime().getTime());
//                
//                if (hopRouteToDestination.size() > 2 && message.getSize() > 0 && countOCS <= 3) {
//
//
//                    simulator.putLog(simulator.getMasterClock(), "FAIL: Sending failed because no OCS-circuit has been setup "
//                            + owner.getId() + "-->" + message.getDestination().getId() + " : " + message.getId(), Logger.RED, message.getSize(), message.getWavelengthID());
//                    
//                    message.setReSent(true);
//                    message.setHybridSwitchSenderInWait(this);
//                    messageQueue.offer(message);
//
//                    System.out.println("Puesto en espera  mensaje: "+message+"  EN: "+owner);
//                    OCSRoute ocsRoute = simulator.getPhysicTopology().findOCSRoute(owner, hopRouteToDestination.get(hopRouteToDestination.size() - 2));
//                    owner.requestOCSCircuit(ocsRoute, true, simulator.getMasterClock());
//                    simulator.addRequestedCircuit(ocsRoute);
//                    countOCS++;
//                    
//                    return false; 
//
//                }


                ////////////////////////////////////////////////////




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
                            //We try to send
                            if (ocsSender.putMsgOnLink(message, theOutPort, t, true, ocsRoute.size() - 2)) {
                                message.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);
//                                System.out.println(" Switch via OCS  Msg es comienzo " + inport.getID());

                                HybridSwitchImpl hybridSwitchImplLast = (HybridSwitchImpl) ocsRoute.get(ocsRoute.size() - 1);
                                ManagerOCS.getInstance().addTraffic(message, (HybridSwitchImpl) owner, hybridSwitchImplLast);


//                                if (runnable==null &&  ocsRoute.size() > 3) {//FIXME: solo para pruebas
//
//                                   runnable = new Runnable() { 
//
//                                        @Override
//                                        public void run() {
//                                            try {
//                                                Thread.sleep(500);
//                                            } catch (InterruptedException ex) {
//                                                Logger.getLogger(HybridSwitchSender.class.getName()).log(Level.SEVERE, null, ex);
//                                            }
//                                            ocsRoute.getSource().teardDownOCSCircuit(ocsRoute.getDestination(), theOutgoingWavelength, theOutPort, t);
//                                            System.out.println("Elimnar OCS = " + ocsRoute.getSource() + " -> " + ocsRoute.getDestination()
//                                                    + " Color: " + theOutgoingWavelength);
//                                           
//                                        }
//                                    };
//
//                                    Thread thread = new Thread(runnable);
//                                    thread.start();
//
//                                }

                                return true;
                            }
                        }
                    }

                    OCSRoute ocsRoute = ocsRoutes.get(0);
                    //Link is busy, but we can try to send it via LSP defaulf..(inten por lsp ala cero)
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
                                    message.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);
//                                System.out.println(" Switch via OCS  Msg es comienzo " + inport.getID());
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
     * @author Yesid
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
}
