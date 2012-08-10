/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.JobAckMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.ResourceNode;
import Grid.Nodes.Hybrid.Parallel.HybridClientNodeImpl;
import Grid.Nodes.Hybrid.Parallel.HybridSwitchImpl;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import Grid.Route;
import Grid.Routing.Routing;
import Grid.Sender.Hybrid.Parallel.HybridSwitchSender;
import Grid.Sender.Hybrid.Parallel.HyrbidEndSender;
import Grid.Sender.OBS.OBSSender;
import Grid.Sender.OBS.OBSWavConSwitchSender;
import Grid.Sender.OCS.OCSSwitchSender;
import Grid.Sender.Sender;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import simbase.Time;

public class PCE extends HybridSwitchImpl {

    private GridSimulator simulator;
    private Routing routing;
    private double b;

    public PCE(String id, GridSimulator simulator) {

        super(id, simulator);
        this.simulator = simulator;
        routing = simulator.getRouting();


    }

    public Map<ResourceNode, Double> getMarkovCostList(ClientNode clientNode, List<ResourceNode> resourceNodes, JobAckMessage jobAckMessage) {

        Map<ResourceNode, Double> map = new HashMap<ResourceNode, Double>();



        double Wtotal;
        double Wb;
        double Wsign;
        double Wsw;
        double Ccap = 1; // Coeficciente de costo de ancho de banda por unidad de capacidad.
        double W; //Capacidad de cada lambda.
        double Hf; //Numero de saltos 
        double T; // Tiempo de duracion de la solicitud. 

//        double 



        forResource:
        for (ResourceNode resourceNode : resourceNodes) {

            HybridClientNodeImpl clientNodeImpl = (HybridClientNodeImpl) clientNode;
            OBSSender obsSender = (OBSSender) ((HyrbidEndSender) clientNodeImpl.getSender()).getObsSender();



             Map<String, GridOutPort>  routingMap2 = ((OBSSender) obsSender).getRoutingMap();

            GridOutPort gridOutPort = routingMap2.get(resourceNode.getId());


            //OCSRoute oCSRoute = routing.findOCSRoute(clientNode, resourceNode);

            HybridSwitchImpl swicthFirst =  (HybridSwitchImpl) gridOutPort.getTarget().getOwner();
            
            Time t = swicthFirst.getCurrentTime();
            JobMessage message = new JobMessage(jobAckMessage, swicthFirst.getCurrentTime());


            HybridSwitchSender hybridSwitchSender = (HybridSwitchSender) swicthFirst.getSender();
            OCSSwitchSender ocsSender = (OCSSwitchSender) hybridSwitchSender.getOcsSender();


            Map routingMap = ((OBSSender) hybridSwitchSender.getObsSender()).getRoutingMap();

            if (routingMap.containsKey(resourceNode.getId())) {

                List<OCSRoute> ocsRoutes = null;
                Route routeToDestination = simulator.getRouting().findOCSRoute(swicthFirst, resourceNode);

                for (int i = routeToDestination.size() - 2; i >= 1; i--) {

                    Entity backwardHop = routeToDestination.get(i);
                    ocsRoutes = simulator.returnOcsCircuit(swicthFirst, backwardHop);

                    if (ocsRoutes != null) {
                        break;
                    }
                }



                if (ocsRoutes != null) {
                    Iterator<OCSRoute> routeIterator = ocsRoutes.iterator();

                    while (routeIterator.hasNext()) {
                        OCSRoute ocsRoute = routeIterator.next();
                        if (ocsRoute != null) {
                            //There is an OCS route to the next virtual hop
                            Entity nextRealHop = ocsRoute.findNextHop(swicthFirst);
                            GridOutPort theOutPort = swicthFirst.findOutPort(nextRealHop);
                            //the beginning wavelength
                            int theOutgoingWavelength = ocsRoute.getWavelength();

                            // we start sending using a new wavelength (OCS circuit)
                            message.setWavelengthID(theOutgoingWavelength);
                            //We try to send
                            if (putMsgOnLinkTest(message, theOutPort, t, swicthFirst)) {
                                message.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);

                                W = theOutPort.getLinkSpeed();
                                Hf = routeToDestination.size();
                                T = jobAckMessage.getRequestMessage().getJobSize() / b;
                                Wb = Ccap * W * Hf * T;
                                map.put(resourceNode, Wb);
                                break;
                            }
                        }
                    }

                }

            }

        }


        return map;
    }

    public boolean putMsgOnLinkTest(GridMessage message, GridOutPort port, Time t, Entity owner) {
        //XXX: Esto puede significar q se esta haciendo en el plano de control
        if (message.getSize() == 0) {
            return true;
        }

//        message.get

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

        b = Sender.getBandwidthToGrant(bandwidthFree, trafficPriority, channelSize);

        double linkSpeed = port.getLinkSpeed();

        if ((100 * (bandwidthFree / linkSpeed)) <= OCSSwitchSender.PERCENTAGE_TO_DROP_OCS) {
            return false;
        }

        if (owner.isAnyChannelFree(b, port, message.getWavelengthID(), t)) {

            return true;
        } else {
            return false;
        }
    }
}
