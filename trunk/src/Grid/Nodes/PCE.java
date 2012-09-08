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
import Grid.Sender.OCS.OCSSwitchSender;
import Grid.Sender.Sender;
import java.util.*;
import simbase.Time;

public class PCE extends HybridSwitchImpl {

    private GridSimulator simulator;
    private Routing routing;
    private double bandwidthRequested;//Solicitud de ancho de banda

    public PCE(String id, GridSimulator simulator, double costFindCommonWavelenght, double costAllocateWavelenght) {
        super(id, simulator, costFindCommonWavelenght, costAllocateWavelenght);
        this.simulator = simulator;
        routing = simulator.getRouting();

    }

    public Map<ResourceNode, Double> getMarkovCostList(ClientNode clientNode, List<ResourceNode> resourceNodes, JobAckMessage jobAckMessage) {

        Map<ResourceNode, Double> mapResourceNetworkCost = new HashMap<ResourceNode, Double>();

        double Wtotal;//Suma total de Wsing+Wsw+Wb
        double Wb;
        double Wsign_0 = 0;
        double Wsign_1 = 0;
        double Wsw = 0;
        double Ccap = 1; // Coeficciente de costo de ancho de banda por unidad de capacidad.
        double W; //Capacidad de cada lambda.
        double Hf; //Numero de saltos 
        double T; // Tiempo de duracion de la solicitud. 

        ///Variables para costo de senializacion

        double a = 1; // Accion sobre la capa lambda. 
        double Csign = 0.5; //*Costo de señalizacion de la informacion a todos los nodos involucrados. 
        double Ccomp = 0.5; //*Costo para recomputación de los caminos mas cortos entre par de nodos del camino de luz. Despues de la modificacion de la toplogia 

        double Cfind = 1.5;//GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.findCommonWavelenght);  //Costo de busqueda de una longitud de onda comun hacer usada en la fibras.
        double Callocate = 1; //GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.allocateWavelenght); // Costo de alojar la longitud de onda en el camino de luz        
        double Cx = Csign + Ccomp;
        double Cy = Cfind + Callocate;
        // Variable para costo de comutacion
        double C_lambda = 0.35; //Coeficiente para la conmutacion opto-elect en el final de camino de luz 
        double Copt = 0.25; //Coeficiete para la conmutacion de lamdaSP en los comutadores opticos de camino 

        double Y;

        for (ResourceNode resourceNode : resourceNodes) {

            HybridClientNodeImpl clientNodeImpl = (HybridClientNodeImpl) clientNode;
            OBSSender obsSenderClientNode = (OBSSender) ((HyrbidEndSender) clientNodeImpl.getSender()).getObsSender();
            Map<String, GridOutPort> routingMapClientNode = ((OBSSender) obsSenderClientNode).getRoutingMap();
            GridOutPort clientOutportToResource = routingMapClientNode.get(resourceNode.getId());
            HybridSwitchImpl firstSwicth = (HybridSwitchImpl) clientOutportToResource.getTarget().getOwner();

            HybridResourceNode hybridResourceNode = (HybridResourceNode) resourceNode;
            OBSSender obsSenderResource = (OBSSender) ((HyrbidEndSender) hybridResourceNode.getSender()).getObsSender();
            Map<String, GridOutPort> routingMapResourceNode = ((OBSSender) obsSenderResource).getRoutingMap();
            GridOutPort resourceOutportToClient = routingMapResourceNode.get(clientNodeImpl.getId());
            HybridSwitchImpl lastSwicth = (HybridSwitchImpl) resourceOutportToClient.getTarget().getOwner();

            OpticFlow opticFlow = findBs(firstSwicth, lastSwicth);

            Time firstSwitchCurrentTime = firstSwicth.getCurrentTime();
            JobMessage jobDummyMsg = new JobMessage(jobAckMessage, firstSwicth.getCurrentTime());

            HybridSwitchSender hybridSenderFirtSwitch = (HybridSwitchSender) firstSwicth.getSender();
            OCSSwitchSender ocsSenderFirtSwitch = (OCSSwitchSender) hybridSenderFirtSwitch.getOcsSender();

            Map routingMapFirtSwitch = ((OBSSender) hybridSenderFirtSwitch.getObsSender()).getRoutingMap();

            if (routingMapFirtSwitch.containsKey(resourceNode.getId())) {

                List<OCSRoute> ocsRoutes = null;
                Route hopRouteToDestination = simulator.getRouting().findOCSRoute(firstSwicth, resourceNode);

                for (int i = hopRouteToDestination.size() - 2; i >= 1; i--) {

                    Entity backwardHop = hopRouteToDestination.get(i);
                    ocsRoutes = simulator.returnOcsCircuit(firstSwicth, backwardHop);

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
                            Entity nextRealHop = ocsRoute.findNextHop(firstSwicth);
                            GridOutPort theOutPort = firstSwicth.findOutPort(nextRealHop);
                            //the beginning wavelength
                            int theOutgoingWavelength = ocsRoute.getWavelength();
                            // we start sending using a new wavelength (OCS circuit)
                            jobDummyMsg.setWavelengthID(theOutgoingWavelength);

                            //We try to send
                            if (putMsgOnLinkTest(jobDummyMsg, theOutPort, firstSwitchCurrentTime, firstSwicth)) {

                                jobDummyMsg.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);

                                //Costo de ancho de banda
                                W = theOutPort.getLinkSpeed();
                                Hf = hopRouteToDestination.size() - 2;
                                T = jobAckMessage.getRequestMessage().getJobSize() / bandwidthRequested;
                                Wb = Ccap * W * Hf * T;

                                //Costo de conmutacion                                 
                                Y = (((Hf - 1) * Copt) + C_lambda);

                                //Costo de señalizacion                                 
                                Wsign_0 = 0;
                                Wsign_1 = Cx + (Cy * Hf);

                                double Wsw_1 = Y * (opticFlow.getB_lambda() + opticFlow.getB_Fiber() + bandwidthRequested) * T; // se toma la accion 
                                double Wsw_0 = ((Y * opticFlow.getB_lambda()) + (C_lambda * Hf * (opticFlow.getB_Fiber() + bandwidthRequested))) * T;

                                //Calculo del threshold
                                double Bth = getThresholdBetween(firstSwicth, lastSwicth, bandwidthRequested, W, T);



                                mapResourceNetworkCost.put(resourceNode, Wb);

//                                Wtotal = Wsign+Wsw+Wb;
                                jobAckMessage.setEstimatedMarkovianCost(0);


                                System.out.print(
                                        clientNode + " Recurso " + resourceNode + " Mensaje " + jobAckMessage + " Peso  " + jobAckMessage.getRequestMessage().getJobSize()
                                        + "  Wb: " + Wb + "  ------- Wsw_false:" + Wsw_0 + " Wsw_true:" + Wsw_1 + " WSing_1:" + Wsign_1);

                                System.out.println(" bandwidthRequested:" + bandwidthRequested + opticFlow + " Hf:" + Hf + " T:" + T);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return mapResourceNetworkCost;
    }

    /**
     * Find the flows between two Switches. The sum of direct flows over the
     * direct OCSs and the flow over default OCSs.
     *
     * @param source
     * @param destination
     * @return OpticFlow objet. It has the direct and default flows.
     */
    public OpticFlow findBs(HybridSwitchImpl source, HybridSwitchImpl destination) {

        double B_lambda = 0; // Trafico que fluye mediante LSP Directos.
        double B_Fiber = 0; // Trafico que fluye mediante lSP por defecto. 

        List<OCSRoute> ocsRoutes = simulator.returnOcsCircuit(source, destination);
        //FIXME : verifica si el oCSRoute.getWavelength() se asigna cuando al inicio o al fina de la creacion de OCS

        ArrayList<Integer> lambdaList = new ArrayList<Integer>();
        OBSSender obsSender = (OBSSender) ((HybridSwitchSender) source.getSender()).getObsSender();
        Map<String, GridOutPort> routingMap = ((OBSSender) obsSender).getRoutingMap();
        GridOutPort gridOutPort = routingMap.get(destination.getId());
        if (ocsRoutes != null) {
            for (OCSRoute oCSRoute : ocsRoutes) {

                lambdaList.add(oCSRoute.getWavelength());

//                gridOutPort.getLinkSpeed();
//                LambdaChannelGroup channelGroup = source.getMapLinkUsage().get(gridOutPort).get(oCSRoute.getWavelength());

//                System.out.println("PCE -  Rutas ocs  S:" + source + " - D:"
//                        + destination + " id " + oCSRoute.getWavelength() + " FreeChannel: " + channelGroup.getFreeBandwidth(source.getCurrentTime().getTime()));
            }
        }

        for (int i = 0; i < gridOutPort.getMaxNumberOfWavelengths(); i++) {
            LambdaChannelGroup channelGroup = source.getMapLinkUsage().get(gridOutPort).get(i);
            if (lambdaList.contains(i)) {
                B_lambda += gridOutPort.getLinkSpeed() - channelGroup.getFreeBandwidth(source.getCurrentTime().getTime());
            } else {

                channelGroup.deleteLazyChannels(source.getCurrentTime().getTime());
                //double nonFreeSpace = 0;
                for (LambdaChannelGroup.Channel channel : channelGroup.getChannels()) {
                    if (channel.getEntitySource().equals(source) && channel.getEntityDestination().equals(destination)) {
                        B_Fiber += channel.getChannelSpeed();
                    }
                }
            }
        }
//        System.out.println("********** PCE pos sumas: BL =" + B_lambda + " BF=" + B_Fiber);

        return new OpticFlow(B_lambda, B_Fiber);
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

        bandwidthRequested = Sender.getBandwidthToGrant(bandwidthFree, trafficPriority, channelSize);

        if (bandwidthRequested == Sender.INVALID_BANDWIDHT) {
            return false;
        }

        if (owner.isAnyChannelFree(bandwidthRequested, port, message.getWavelengthID(), t)) {

            return true;
        } else {
            return false;
        }
    }

    public double getBandwidthRequested() {
        return bandwidthRequested;
    }

    public void setBandwidthRequested(double bandwidthRequested) {
        this.bandwidthRequested = bandwidthRequested;
    }

    public double getThresholdBetween(Entity source, Entity destination, double bandwidthRequested, double W, double T) {

        double threshold = -1;

        List<OCSRoute> ocsRoutes = null;
        Route hopRouteToDestination = simulator.getRouting().findOCSRoute(source, destination);

        //#############*Variables para determinar el limite##################*//
        int hF = hopRouteToDestination.size() - 1;//Numero total de fibras en el camino de luz.

        int β = 0;//Numero total de caminos de luz en Pλ q NO tienen suficiente ancho de banda para satisfacer la solicitud.
        int η = 0;//Numero total de caminos de luz en Pλ q tienen suficiente ancho de banda para satisfacer la solicitud.

        int hβF = 0;//Sumatoria de todas las fibras q pertenecen a β.
        int hηF = 0;//Sumatoria de todas las fibras q pertenecen a η.

        Time currentTimeSourceNode = source.getCurrentTime();
        Entity origin = source;

        //Busco salto a salto hacia atras buscando los OCS
        for (int i = hopRouteToDestination.size(); i >= 2; i--) {

            Entity backwardHop = hopRouteToDestination.get(i - 1);
            ocsRoutes = simulator.returnOcsCircuit(origin, backwardHop);

            if (ocsRoutes != null) {
                //Como puede haber mas de un OCS entre par de nodos miro cuales
                //NO tiene capacidad

                boolean foundOCSCantSupport = false;
                int βaux = 0;
                int hβFaux = 0;
                
                boolean foundOCSCanSupport = false;

                for (OCSRoute ocsRoute : ocsRoutes) {

                    int wavelenghtStartOCS = ocsRoute.getWavelength();
                    Entity originNextHop = ocsRoute.findNextHop(origin);
                    GridOutPort outportToNextHop = origin.findOutPort(originNextHop);

                    if (bandwidthRequested <= origin.getFreeBandwidth(outportToNextHop, wavelenghtStartOCS, currentTimeSourceNode)) {
                    
                        η++;
                        hηF+=simulator.getRouting().findOCSRoute(origin, backwardHop).size()-1;//Num de fibras
                        
                        
                        foundOCSCantSupport = false;
                        
                        
                        
                        
                    } else if (!foundOCSCantSupport) {

                        βaux++;
                        hβFaux=simulator.getRouting().findOCSRoute(origin, backwardHop).size()-1;//Num de fibras
                        
                        foundOCSCantSupport = true;
                    }
                    
                    if(foundOCSCantSupport){
                        β+=βaux;
                        hβF+=hβFaux;
                    }
                }

                origin = backwardHop;
                backwardHop = destination;
            }
        }




        return threshold;
    }

    /**
     * #########################################################################
     * Inner class to model the direct and not direct flows between two switches
     * #########################################################################
     */
    public class OpticFlow {

        private double B_lambda = 0.0;
        private double B_Fiber = 0.0;

        public OpticFlow(double B_lambda, double B_Fiber) {
            this.B_lambda = B_lambda;
            this.B_Fiber = B_Fiber;
        }

        public double getB_lambda() {
            return B_lambda;
        }

        public void setB_lambda(double B_lambda) {
            this.B_lambda = B_lambda;
        }

        public double getB_Fiber() {
            return B_Fiber;
        }

        public void setB_Fiber(double B_Fiber) {
            this.B_Fiber = B_Fiber;
        }

        @Override
        public String toString() {
            return " OpticFlow => B_lambda:" + B_lambda + " B_Fiber:" + B_Fiber;
        }
    }
}
