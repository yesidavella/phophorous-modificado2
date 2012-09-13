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
    private transient Routing routing;
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

        labelForResource:
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
                Route hopRouteToDestination = simulator.getPhysicTopology().findOCSRoute(firstSwicth, resourceNode);

                for (int i = hopRouteToDestination.size() - 2; i >= 1; i--) {

                    Entity backwardHop = hopRouteToDestination.get(i);
                    ocsRoutes = simulator.returnOcsCircuit(firstSwicth, backwardHop);

                    if (ocsRoutes != null) {
                        break;
                    }
                }

                if (ocsRoutes != null) {
                    //Buscar OCS directo con suficiente ancho de banda para enrutar b
                    for (OCSRoute ocsRoute : ocsRoutes) {
                        Entity nextRealHop = ocsRoute.findNextHop(firstSwicth);
                        GridOutPort theOutPort = firstSwicth.findOutPort(nextRealHop);
                        int theOutgoingWavelength = ocsRoute.getWavelength();
                        if (theOutgoingWavelength == 0) {
                            continue; //Pasar a el siguiente OCS porque el OCS actual  es default
                        }
                        jobDummyMsg.setWavelengthID(theOutgoingWavelength);

                        // Verificar si el OCS tiene  suficiente ancho de banda para enrutar b
                        if (putMsgOnLinkTest(jobDummyMsg, theOutPort, firstSwitchCurrentTime, firstSwicth)) {
                            //Costo de ancho de banda
                            W = theOutPort.getLinkSpeed();
                            Hf = hopRouteToDestination.size() - 2;
                            T = jobAckMessage.getRequestMessage().getJobSize() / bandwidthRequested;
                            Wb = Ccap * W * Hf * T;

                            //Costo de señalizacion  es 0 porque el OCS ya esta creado.                             
                            Wsign_0 = 0;

                            //Costo de conmutacion                                 
                            Y = (((Hf - 1) * Copt) + C_lambda);
                            //Costo de conmutacion  es con la accion 0 porque  se enruta por un OCS ya creado. 
                            double Wsw_0 = ((Y * opticFlow.getB_lambda()) + (C_lambda * Hf * (opticFlow.getB_Fiber() + bandwidthRequested))) * T;

                            Wtotal = Wsign_0 + Wsw_0 + Wb;
                            mapResourceNetworkCost.put(resourceNode, Wtotal);
                            jobAckMessage.setEstimatedMarkovianCost(Wtotal);
                            continue labelForResource;//Continua con el siguiente recurso.
                        }

                    }

                    //Si llega hasta este punto significa que no existe un OCS directo con suficiente ancho de banda.
                    //Verificar que los  OCS default  tengan suficiente ancho de banda.
                    for (OCSRoute ocsRoute : ocsRoutes) {
                        Entity nextRealHop = ocsRoute.findNextHop(firstSwicth);
                        GridOutPort theOutPort = firstSwicth.findOutPort(nextRealHop);
                        int theOutgoingWavelength = ocsRoute.getWavelength();
                        //OCS default
                        if (theOutgoingWavelength == 0) {
                            jobDummyMsg.setWavelengthID(theOutgoingWavelength);
                            //Verifica si la primera OCS default tenga suficiente ancho de banda. 
                            if (putMsgOnLinkTest(jobDummyMsg, theOutPort, firstSwitchCurrentTime, firstSwicth)) {
                                //FIXME: Verifica que exista ancho de banda por todos lo OCS default a lo largo de la ruta.

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
                                double Bth = getThresholdBetween(firstSwicth, lastSwicth, bandwidthRequested, W, T, Cx, Cy, Ccap, C_lambda, Copt);


                                //FIXME: aqui debe decidir el Bth la decision y asi sacar el costo.


                                continue labelForResource;//Continua con el siguiente recurso.


                            } else {
                                // si OCS default  no tiene suficiente ancho de banda se crea OCS directo obligatoriamente

                                jobDummyMsg.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);

                                //Costo de ancho de banda
                                W = theOutPort.getLinkSpeed();
                                Hf = hopRouteToDestination.size() - 2;


                                ////// INI BLOQUE I//////////Este bloque toca para  hacer calculos con un OCS que no existe aun.
                                int trafficPriority = 1;
                                Entity source = jobDummyMsg.getSource();
                                Entity destination = jobDummyMsg.getDestination();

                                if (source instanceof ClientNode) {
                                    trafficPriority = ((ClientNode) source).getState().getTrafficPriority();
                                } else if (destination instanceof ClientNode) {
                                    trafficPriority = ((ClientNode) destination).getState().getTrafficPriority();
                                } else {
                                    System.out.println("Esto es un error en la asignacion de la prioridad del trafico del cliente.");
                                }
                                double bandwidthFree = theOutPort.getLinkSpeed();

                                int channelSize = firstSwicth.getChannelsSize(theOutPort, jobDummyMsg.getWavelengthID(), firstSwitchCurrentTime);

                                bandwidthRequested = Sender.getBandwidthToGrant(bandwidthFree, trafficPriority, channelSize);
                                //////FIN BLOQUE I//////////


                                T = jobAckMessage.getRequestMessage().getJobSize() / bandwidthRequested;
                                Wb = Ccap * W * Hf * T;

                                //Costo de conmutacion  es 1 porque la accion es de crear OCS                             
                                Y = (((Hf - 1) * Copt) + C_lambda);

                                //Costo de señalizacion es 1 porque la accion es de crear OCS                                                  
                                Wsign_1 = Cx + (Cy * Hf);

                                double Wsw_1 = Y * (opticFlow.getB_lambda() + opticFlow.getB_Fiber() + bandwidthRequested) * T; // se toma la accion                                


                                Wtotal = Wsign_1 + Wsw_1 + Wb;
                                mapResourceNetworkCost.put(resourceNode, Wtotal);
                                jobAckMessage.setEstimatedMarkovianCost(Wtotal);
                                continue labelForResource;//Continua con el siguiente recurso.
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

    public double getThresholdBetween(Entity source, Entity destination, double bandwidthRequested, double W, double T,
            double Cx, double Cy, double Ccap, double C_lambda, double Copt) {

        List<OCSRoute> ocsRoutes = null;
        Route hopRouteToDestination = simulator.getPhysicTopology().findOCSRoute(source, destination);

        //#############*Variables para determinar el limite##################*//
        int hF = hopRouteToDestination.size() - 1;//Numero total de fibras en el camino de luz.

        int β = 0;//Numero total de caminos de luz en Pλ q NO tienen suficiente ancho de banda para satisfacer la solicitud.
        int η = 0;//Numero total de caminos de luz en Pλ q tienen suficiente ancho de banda para satisfacer la solicitud.

        int hβF = 0;//Sumatoria de todas las fibras q pertenecen a β.
        int hηF = 0;//Sumatoria de todas las fibras q pertenecen a η.

        Time currentTimeSourceNode = source.getCurrentTime();
        Entity origin = source;

        //Busco salto a salto hacia atras buscando los OCS
        for (int i = hopRouteToDestination.size(); (i >= 2) && (origin != destination); i--) {

            Entity backwardHop = hopRouteToDestination.get(i - 1);
            ocsRoutes = simulator.returnOcsCircuit(origin, backwardHop);

            condOCSRoutes:
            if (ocsRoutes != null) {

                //Como puede haber mas de un OCS entre par de nodos miro cuales
                //NO tiene capacidad
                boolean foundOCSCantSupport = false;
                int βaux = 0;
                int hβFaux = 0;

                for (int routeIndex = 0; routeIndex < ocsRoutes.size(); routeIndex++) {

                    OCSRoute ocsRoute = ocsRoutes.get(routeIndex);

                    int wavelenghtStartOCS = ocsRoute.getWavelength();
                    Entity originNextHop = ocsRoute.findNextHop(origin);
                    GridOutPort outportToNextHop = origin.findOutPort(originNextHop);

                    if (bandwidthRequested <= origin.getFreeBandwidth(outportToNextHop, wavelenghtStartOCS, currentTimeSourceNode)) {

                        η++;//Un circuito mas
                        hηF += simulator.getPhysicTopology().findOCSRoute(origin, backwardHop).size() - 1;//Num de fibras

                        origin = backwardHop;
                        i = hopRouteToDestination.size() + 1;

                        break condOCSRoutes;

                    } else if (!foundOCSCantSupport) {

                        βaux++;//Un circuito mas
                        hβFaux = simulator.getPhysicTopology().findOCSRoute(origin, backwardHop).size() - 1;//Num de fibras

                        foundOCSCantSupport = true;
                    }

                    //Examino si es el ultimo OCS
                    if (routeIndex == ocsRoutes.size() - 1) {
                        β += βaux;
                        hβF += hβFaux;
                        i = hopRouteToDestination.size() + 1;
                        origin = backwardHop;
                    }
                }
            }
        }

        double thresholdNum = ((hF - hβF) * (Ccap * W * T)) - (Cx * (β - 1));
        double thresholdDiv = T * ((β + η - 1) * (C_lambda - Copt) + (Ccap * hηF));


        return thresholdNum / thresholdDiv;
    }

    /**
     * @param source The Head of a entire route.
     * @param destination The Head-end of a entire route.
     * @return A list of structures where each structure represents a inner
     * circuit between source and destination and the state of each circuit
     * evaluated in the currentTime of the source node.
     */
    public ArrayList<SubCircuitAbs> getCircuitsConcatenation(Entity source, Entity destination, double requestedBandwidht) {

        Time currentTimeSourceNode = source.getCurrentTime();

        return getCircuitsConcatenation(source, destination, currentTimeSourceNode, requestedBandwidht);
    }

    /**
     * @param source The Head of a entire route.
     * @param destination The Head-end of a entire route.
     * @param evaluationTime Time when the concatenation of circuits is
     * avaluated.
     * @return A list of structures where each structure represents a inner
     * circuit between source and destination evaluated in evaluationTime.
     */
    public ArrayList<SubCircuitAbs> getCircuitsConcatenation(Entity source, Entity destination, Time evaluationTime, double requestedBandwidht) {

        ArrayList<SubCircuitAbs> circuitsConcatenation = new ArrayList<SubCircuitAbs>();

        List<OCSRoute> ocsRoutes = null;
        Route physicHopRoute = simulator.getPhysicTopology().findOCSRoute(source, destination);

        Entity origin = source;

        //Busco salto a salto hacia atras buscando los OCS
        for (int i = physicHopRoute.size(); (i >= 2) && (origin != destination); i--) {

            Entity backwardHop = physicHopRoute.get(i - 1);
            ocsRoutes = simulator.returnOcsCircuit(origin, backwardHop);

            condOCSRoutes:
            if (ocsRoutes != null) {

                //Como puede haber mas de un OCS entre par de nodos, con esta 
                //bandera selecciono el primero q NO tiene capacidad.
                boolean foundOCSCantSupport = false;

                for (int routeIndex = 0; routeIndex < ocsRoutes.size(); routeIndex++) {

                    OCSRoute ocsRoute = ocsRoutes.get(routeIndex);

                    int wavelenghtStartOCS = ocsRoute.getWavelength();
                    Entity originNextHop = ocsRoute.findNextHop(origin);
                    GridOutPort outportToNextHop = origin.findOutPort(originNextHop);

                    if (requestedBandwidht <= origin.getFreeBandwidth(outportToNextHop, wavelenghtStartOCS, evaluationTime)) {
                        circuitsConcatenation.add(new SubCircuitAbs(origin, backwardHop, true));
                        origin = backwardHop;
                        i = physicHopRoute.size() + 1;

                        break condOCSRoutes;

                    } else if (!foundOCSCantSupport) {

                        foundOCSCantSupport = true;
                    }

                    //Examino si es el ultimo OCS y si encontro algun circuito q
                    //NO soporta en ancho de banda solicitado
                    if ((routeIndex == ocsRoutes.size() - 1) && (foundOCSCantSupport)) {

                        circuitsConcatenation.add(new SubCircuitAbs(origin, backwardHop, false));

                        i = physicHopRoute.size() + 1;
                        origin = backwardHop;
                    }
                }
            }
        }

        return circuitsConcatenation;
    }

    /**
     * #########################################################################
     * Inner class to model subCircuits of a route, and if this subCircuit
     * supports the bandwidth requested.
     * #########################################################################
     */
    public class SubCircuitAbs {

        Entity source;
        Entity destination;
        boolean supportBandwidthRequested;

        public SubCircuitAbs(Entity source, Entity destination, boolean supportBandwidthRequested) {
            this.source = source;
            this.destination = destination;
            this.supportBandwidthRequested = supportBandwidthRequested;
        }

        public Entity getSource() {
            return source;
        }

        public void setSource(Entity source) {
            this.source = source;
        }

        public Entity getDestination() {
            return destination;
        }

        public void setDestination(Entity destination) {
            this.destination = destination;
        }

        public boolean isSupportBandwidthRequested() {
            return supportBandwidthRequested;
        }

        public void setSupportBandwidthRequested(boolean supportBandwidthRequested) {
            this.supportBandwidthRequested = supportBandwidthRequested;
        }
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
