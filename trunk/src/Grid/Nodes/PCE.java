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
import Grid.Nodes.Hybrid.Parallel.HybridResourceNode;
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
import java.util.*;
import simbase.Time;

public class PCE extends HybridSwitchImpl {

    private GridSimulator simulator;
    private Routing routing;
    private double b;
    private double B_lamdba; // Trafico que fluye mediante LSP Directos.
    private double B_Fibra; // Trafico que fluye mediante lSP por defecto. 

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


        ///Variables para costo de senializacion

        double a = 1; // Accion sobre la capa lambda. 
        double Csign = 1; //Costo de señalizacion de la informacion a todos los nodos involucrados. 
        double Ccomp = 1; //Costo para recomputación de los caminos mas cortos entre par de nodos del camino de luz. Despues de la modificacion de la toplogia 

        double Cfind = 1; //Costo de busqueda de una longitud de onda comun hacer usada en la fibras.
        double Callocate = 1; // Costo de alojar la longitud de onda en el camino de luz        
        double Cx = Csign + Ccomp;
        double Cy = Cfind + Callocate;

        // Variable para costo de comutacion


        double Copt; //Coeficiete para la conmutacion de lamdaSP en los comutadores opticos de camino 
        double C_lambda; //Coeficiente para la conmutacion opto-elect en el final de camino de luz 



        double Y; // 




        forResource:
        for (ResourceNode resourceNode : resourceNodes) {

            HybridClientNodeImpl clientNodeImpl = (HybridClientNodeImpl) clientNode;
            OBSSender obsSender = (OBSSender) ((HyrbidEndSender) clientNodeImpl.getSender()).getObsSender();
            Map<String, GridOutPort> routingMap2 = ((OBSSender) obsSender).getRoutingMap();
            GridOutPort gridOutPort = routingMap2.get(resourceNode.getId());
            HybridSwitchImpl swicthFirst = (HybridSwitchImpl) gridOutPort.getTarget().getOwner();


            HybridResourceNode hybridResourceNode = (HybridResourceNode) resourceNode;
            OBSSender obsSender2 = (OBSSender) ((HyrbidEndSender) hybridResourceNode.getSender()).getObsSender();
            Map<String, GridOutPort> routingMap3 = ((OBSSender) obsSender2).getRoutingMap();
            GridOutPort gridOutPort2 = routingMap3.get(clientNodeImpl.getId());
            HybridSwitchImpl swicthLast = (HybridSwitchImpl) gridOutPort2.getTarget().getOwner();

            findBs(swicthFirst, swicthLast);

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
                                Hf = routeToDestination.size() - 1;
                                T = jobAckMessage.getRequestMessage().getJobSize() / b;
                                Wb = Ccap * W * Hf * T;

                                map.put(resourceNode, Wb);

//                                System.out.println( "Estimacion PCE -  Cliente "+
//                                        clientNode+" Recurso "+resourceNode+" Mensaje "+jobAckMessage+" Peso  "+jobAckMessage.getRequestMessage().getJobSize()
//                                        +"  Wb: "+Wb );

                                break;
                            }
                        }
                    }
                }
            }
        }

        return map;
    }

    public void findBs(HybridSwitchImpl source, HybridSwitchImpl destination) {
        B_Fibra = 0;
        B_lamdba = 0;
        List<OCSRoute> ocsRoutes = simulator.returnOcsCircuit(source, destination);
        //FIXME : verifica si el oCSRoute.getWavelength() se asigna cuando al inicio o al fina de la creacion de OCS
        if (ocsRoutes != null) {
            ArrayList<Integer> lambdaList = new ArrayList<Integer>();
            OBSSender obsSender = (OBSSender) ((HybridSwitchSender) source.getSender()).getObsSender();
            Map<String, GridOutPort> routingMap = ((OBSSender) obsSender).getRoutingMap();
            GridOutPort gridOutPort = routingMap.get(destination.getId());

            for (OCSRoute oCSRoute : ocsRoutes) {

                lambdaList.add(oCSRoute.getWavelength());

                gridOutPort.getLinkSpeed();
                LambdaChannelGroup channelGroup = source.getMapLinkUsage().get(gridOutPort).get(oCSRoute.getWavelength());

                System.out.println("PCE -  Rutas ocs  S:" + source + " - D:"
                        + destination + " id " + oCSRoute.getWavelength() + " FreeChannel: " + channelGroup.getFreeBandwidth(source.getCurrentTime().getTime()));
            }

            for (int i = 0; i < gridOutPort.getMaxNumberOfWavelengths(); i++) {
                LambdaChannelGroup channelGroup = source.getMapLinkUsage().get(gridOutPort).get(i);
                if (lambdaList.contains(i)) {

                    B_lamdba += gridOutPort.getLinkSpeed() - channelGroup.getFreeBandwidth(source.getCurrentTime().getTime());
                } else {
                    for (LambdaChannelGroup.Channel channel : channelGroup.getChannels()) {
                        if (channel.getEntitySource().equals(source) && channel.getEntityDestination().equals(destination)) {
                            B_Fibra += gridOutPort.getLinkSpeed() - channelGroup.getFreeBandwidth(source.getCurrentTime().getTime());
                        }
                    }
                }
            }
            System.out.println("********** PCE pos sumas: BL =" + B_lamdba + " BF=" + B_Fibra);
        }


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
