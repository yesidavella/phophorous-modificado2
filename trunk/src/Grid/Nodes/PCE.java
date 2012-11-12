package Grid.Nodes;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.Messages.JobAckMessage;
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
import Grid.Sender.Sender;
import java.util.*;
import simbase.Time;

public class PCE extends HybridSwitchImpl {

    private GridSimulator simulator;
    private transient Routing routing;
    private CostMultiMarkovAnalyzer costMultiMarkovAnalyzer;
    public static final boolean TRACK_INSTRUCTION = true;

    public PCE(String id, GridSimulator simulator, double costFindCommonWavelenght, double costAllocateWavelenght) {
        super(id, simulator, costFindCommonWavelenght, costAllocateWavelenght);
        this.simulator = simulator;
        routing = simulator.getRouting();
        costMultiMarkovAnalyzer = new CostMultiMarkovAnalyzer(simulator);

    }

    public Map<ResourceNode, Double> getMarkovCostList(ClientNode clientNode,
            List<ResourceNode> resourceNodes, JobAckMessage jobAckMessage) {

        Map<ResourceNode, Double> mapResourceNetworkCost = new HashMap<ResourceNode, Double>();

        for (ResourceNode resourceNode : resourceNodes) {
            double cost = getNetworkMarkovCost(resourceNode, clientNode, jobAckMessage.getRequestMessage().getJobSize(), !TRACK_INSTRUCTION, null);
            mapResourceNetworkCost.put(resourceNode, cost);

        }
        System.out.println("");
        return mapResourceNetworkCost;
    }

    /**
     *
     * @param resourceNode
     * @param clientNode
     * @param jobSize
     * @param createInstructions
     * @return
     */
    public double getNetworkMarkovCost(ResourceNode resourceNode,
            ClientNode clientNode,
            double jobSize,
            boolean trackInstructions,
            ArrayList<OCSRoute> OCS_Instructions) {

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

        Time firstSwitchCurrentTime = firstSwicth.getCurrentTime();
        if (firstSwicth.equals(lastSwicth)) {
            System.out.println("Return. No hay costo de red. Destino:"+resourceNode);
            return 0D;
        }
        OpticFlow opticFlow = findBs(firstSwicth, lastSwicth);

        ArrayList<OCSRoute> ocsShortesPath = getOCSShortesPath(firstSwicth, lastSwicth);

        double b = getEstimatedBandwidhtToGrant(clientNode.getState().getTrafficPriority(), firstSwitchCurrentTime, ocsShortesPath);
        double costAllRoutesFullBusy = Double.MAX_VALUE;

        if (b == Sender.INVALID_BANDWIDHT) {
            
//            System.out.println("Recalculando b en directo OCS b: "+b);
            OCSRoute ocsRoute = simulator.getPhysicTopology().findOCSRoute(firstSwicth, lastSwicth);
            Entity nextHop = ocsRoute.findNextHop(firstSwicth);
            GridOutPort outportToNextHop = firstSwicth.findOutPort(nextHop);

            b = Sender.getBandwidthToGrant(outportToNextHop.getLinkSpeed(), clientNode.getState().getTrafficPriority(), 0);

            costAllRoutesFullBusy = costMultiMarkovAnalyzer.getCostOCSDirectToCreate(firstSwicth, lastSwicth, firstSwitchCurrentTime, this, opticFlow, b, jobSize);

            if (trackInstructions) {
                OCS_Instructions.add(ocsRoute);
//                System.out.println("XXXXXXXXXX B INVALID_BANDWIDHT y Crear ruta:"+ocsRoute.toString());
            }
//            System.out.println("Return. Todo ocupado* A crear Directo:* AnchoBandaAsignado:"+b+" CostoCrearDirecto:"+costAllRoutesFullBusy+" Destino:"+resourceNode);
            return costAllRoutesFullBusy;
        }

        HybridSwitchSender hybridSenderFirtSwitch = (HybridSwitchSender) firstSwicth.getSender();
        Map routingMapFirtSwitch = ((OBSSender) hybridSenderFirtSwitch.getObsSender()).getRoutingMap();


        if (routingMapFirtSwitch.containsKey(resourceNode.getId())) {

            ArrayList<OCSRoute> ocsSupportBWRequest = new ArrayList();
            ArrayList<OCSRoute> ocsNotSupportBWRequest = new ArrayList();

            for (OCSRoute ocs : ocsShortesPath) {

                Entity ocsSource = ocs.getSource();
                int wavelenghtStartsOCS = ocs.getWavelength();
                Entity nextHop = ocs.findNextHop(ocsSource);
                GridOutPort outportToNextHop = ocsSource.findOutPort(nextHop);

                //   System.out.println("*Iniciando en: " + ocsSource + " FreeBW: " + ocsSource.getFreeBandwidth(outportToNextHop, wavelenghtStartsOCS, firstSwitchCurrentTime));
                if (b <= ocsSource.getFreeBandwidth(outportToNextHop, wavelenghtStartsOCS, firstSwitchCurrentTime)) {
                    ocsSupportBWRequest.add(ocs);
                } else {
                    //Si todos los ocss default tienen capacidad para alojar b los trae, sin tan solo uno no, la lista es nula. 
                    ArrayList<OCSRoute> fullDefaultOCSsSupportBWRequest = getFullDefaultOCSsSupportBWRequest(b, firstSwitchCurrentTime, ocs);

                    //Si es nulla entonces deja el circuito optico q con anterioridad no soportaba a b
                    if (fullDefaultOCSsSupportBWRequest == null) {
                        ocsNotSupportBWRequest.add(ocs);
                    } else {
                        //Si todos los defaults q hacen parte de ocs soportan b, entonces los introduzco en los q si soportan b
                        ocsSupportBWRequest.addAll(fullDefaultOCSsSupportBWRequest);
                    }
                }
            }
//            System.out.println("Existe directo con capacidad "+ (ocsSupportBWRequest.size() == 1 && ocsNotSupportBWRequest.isEmpty()) );
            //Por si existe un ocs directo
            if (ocsSupportBWRequest.size() == 1 && ocsNotSupportBWRequest.isEmpty()) {

                OCSRoute probableDirectOCS = ocsSupportBWRequest.get(0);
                Entity probableSource = probableDirectOCS.getSource();
                Entity probableDestination = probableDirectOCS.getDestination();

                if (probableSource.equals(firstSwicth) && probableDestination.equals(lastSwicth)) {
                    //Returna el costo de usar el OCS directo ya creado. 
                    Double directOCScost = costMultiMarkovAnalyzer.getCostOCSDirect(probableDirectOCS, firstSwitchCurrentTime, b, opticFlow, jobSize);

                    if (directOCScost != null) {
//                        System.out.println("Return. Existe un directo con capacidad** b:"+b+" Costo:"+directOCScost+" Destino:"+resourceNode);
                        return directOCScost;
                    }
                }
            }
            System.out.println("Analisis de Bth...");
            double costByDecisionThreshold = -1;

            costByDecisionThreshold = costMultiMarkovAnalyzer.getCostP_LambdaOrCreateNewDirectOCS(
                    firstSwicth,
                    lastSwicth,
                    ocsSupportBWRequest,
                    ocsNotSupportBWRequest,
                    firstSwitchCurrentTime,
                    opticFlow,
                    b,
                    jobSize);

            if (ocsNotSupportBWRequest.isEmpty() || (costMultiMarkovAnalyzer.getAcciontaken() == 1)) {

                if (trackInstructions && costMultiMarkovAnalyzer.getAcciontaken() == 1) {
                    OCSRoute OCS_Route = new OCSRoute(firstSwicth, lastSwicth, -1);
                    OCS_Instructions.add(OCS_Route);
//                    System.out.println("Return. TOMO ACCION con costo calculado por Bth:"+costByDecisionThreshold+" Destino:"+resourceNode);
                }
//                System.out.println("Return Anterior debe ser *Return. TOMO ACCION*. Costo:"+costByDecisionThreshold+" Destino"+resourceNode);
                return costByDecisionThreshold;
            }

            // la desion del Bth es 0  y se deben crear los circuitos de p-lambda que soportan el trafico
            if (!ocsNotSupportBWRequest.isEmpty()) {
                costByDecisionThreshold = 0;
                //Crear OCS de los circuitos de p-lamda  que no tienen capacidad
                for (OCSRoute ocsNotSupport : ocsNotSupportBWRequest) {

                    HybridSwitchImpl firstMiddleSwicth = (HybridSwitchImpl) ocsNotSupport.getSource();
                    HybridSwitchImpl lastMiddleSwicth = (HybridSwitchImpl) ocsNotSupport.getDestination();
                    costByDecisionThreshold += costMultiMarkovAnalyzer.getCostOCSDirectToCreate(firstMiddleSwicth, lastMiddleSwicth, firstSwitchCurrentTime, this, opticFlow, b, jobSize);

                    if (trackInstructions) {
                        OCS_Instructions.add(ocsNotSupport);
                    }
                }
//                System.out.println("Return. Creacion de OCS que no soportan trafico: " + resourceNode + " Costo:" + costByDecisionThreshold + " Destino:" + resourceNode);
                return costByDecisionThreshold;

            }
        }
//        System.out.println("");
        return Double.MAX_VALUE;

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

//                //System.out.println("PCE -  Rutas ocs  S:" + source + " - D:"
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
//        //System.out.println("********** PCE pos sumas: BL =" + B_lambda + " BF=" + B_Fiber);

        return new OpticFlow(B_lambda, B_Fiber);
    }

    /**
     * @param source The Head of a entire route.
     * @param destination The Head-end of a entire route.
     * @return A list of structures where each structure represents a inner
     * circuit between source and destination and the state of each circuit
     * evaluated in the currentTime of the source node.
     */
//    @Deprecated
//    public ArrayList<SubCircuitAbs> getCircuitsConcatenation(
//            Entity source,
//            Entity destination,
//            JobMessage jobDummyMsg,
//            double bandwidthRequested) {
//
//        Time currentTimeSourceNode = source.getCurrentTime();
//
//        return getCircuitsConcatenation(source, destination, currentTimeSourceNode, jobDummyMsg, bandwidthRequested);
//    }
    /**
     * @param source The Head of a entire route.
     * @param destination The Head-end of a entire route.
     * @param evaluationTime Time when the concatenation of circuits is
     * avaluated.
     * @return A list of structures where each structure represents a inner
     * circuit between source and destination evaluated in evaluationTime.
     */
//    public ArrayList<SubCircuitAbs> getCircuitsConcatenation(Entity source,
//            Entity destination,
//            Time evaluationTime,
//            JobMessage jobDummyMsg,
//            double bandwidthRequested) {
//
//        ArrayList<SubCircuitAbs> circuitsConcatenation = new ArrayList<SubCircuitAbs>();
//
//        boolean supportBandwidthRequested = true;
//        List<OCSRoute> ocsRoutes = null;
//        Route physicHopRoute = simulator.getPhysicTopology().findOCSRoute(source, destination);
//
//        Entity origin = source;
//
//        //Busco salto a salto hacia atras buscando los OCS
//        for (int i = physicHopRoute.size(); (i >= 2) && (origin != destination); i--) {
//
//            Entity backwardHop = physicHopRoute.get(i - 1);
//            ocsRoutes = simulator.returnOcsCircuit(origin, backwardHop);
//
//            condOCSRoutes:
//            if (ocsRoutes != null) {
//
//                //Como puede haber mas de un OCS entre par de nodos, con esta 
//                //bandera selecciono el primero q NO tiene capacidad.
//                boolean foundOCSCantSupport = false;
//
//                for (int routeIndex = 0; routeIndex < ocsRoutes.size(); routeIndex++) {
//
//                    OCSRoute ocsRoute = ocsRoutes.get(routeIndex);
//
//                    int wavelenghtStartOCS = ocsRoute.getWavelength();
//                    Entity originNextHop = ocsRoute.findNextHop(origin);
//                    GridOutPort outportToNextHop = origin.findOutPort(originNextHop);
//
//                    if (bandwidthRequested <= origin.getFreeBandwidth(outportToNextHop, wavelenghtStartOCS, evaluationTime)) {
////                    if (putMsgOnLinkTest(jobDummyMsg, outportToNextHop, currentTime, origin)) {
//                        circuitsConcatenation.add(new SubCircuitAbs(origin, backwardHop, supportBandwidthRequested));
//                        origin = backwardHop;
//                        i = physicHopRoute.size() + 1;
//
//                        break condOCSRoutes;
//
//                    } else if (!foundOCSCantSupport) {
//
//                        foundOCSCantSupport = true;
//                    }
//
//                    //Examino si es el ultimo OCS y si encontro algun circuito q
//                    //NO soporta en ancho de banda solicitado
//                    if ((routeIndex == ocsRoutes.size() - 1) && (foundOCSCantSupport)) {
//
//                        circuitsConcatenation.add(new SubCircuitAbs(origin, backwardHop, !supportBandwidthRequested));
//
//                        i = physicHopRoute.size() + 1;
//                        origin = backwardHop;
//                    }
//                }
//            }
//        }
//
//        return circuitsConcatenation;
//    }
    /**
     *
     * @param head The head(Ingress router) in the backbone network route.
     * @param headEnd The head-end (Egress router) in the backbone network
     * route.
     * @return A list of ocs´s order from head to head-end. All route is mapped
     * and the selection of circuits is based on the max free bandwidth
     * available. If the ocs freebandwidth is 0 and there is no other ocs with
     * the same source-destination free bandwidth available, a ocs without free
     * bandwidht=0 is stored in the list.
     */
    private ArrayList<OCSRoute> getOCSShortesPath(Entity head, Entity headEnd) {

        ArrayList<OCSRoute> ocsConcatenation = new ArrayList<OCSRoute>();

        Routing physicTopology = simulator.getPhysicTopology();

        Route physicHopRoute = physicTopology.findOCSRoute(head, headEnd);
        Time headCurrentTime = head.getCurrentTime();
        Entity segmentHead = head;
        List<OCSRoute> ocsCircuitList;

        //Busco salto a salto hacia atras buscando los OCS
        for (int hopIndex = physicHopRoute.size(); (hopIndex >= 2) && (segmentHead != headEnd); hopIndex--) {

            Entity backwardHop = physicHopRoute.get(hopIndex - 1);
            ocsCircuitList = simulator.returnOcsCircuit(segmentHead, backwardHop);

            if (ocsCircuitList != null) {

                OCSRoute bestOCS = null;
                double maxFreeBandwidthFound = 0;//0 Mbps

                for (int circuitIndex = 0; circuitIndex < ocsCircuitList.size(); circuitIndex++) {

                    OCSRoute ocs = ocsCircuitList.get(circuitIndex);

                    if (segmentHead == ocs.getSource() && backwardHop == ocs.getDestination()) {

                        int wavelenghtStartsOCS = ocs.getWavelength();
                        Entity nextHopToSegmentHead = ocs.findNextHop(segmentHead);
                        GridOutPort outportToNextHop = segmentHead.findOutPort(nextHopToSegmentHead);

                        double ocsFreeBandwidth = segmentHead.getFreeBandwidth(outportToNextHop, wavelenghtStartsOCS, headCurrentTime);

                        if (ocsFreeBandwidth > maxFreeBandwidthFound) {
                            maxFreeBandwidthFound = ocsFreeBandwidth;
                            bestOCS = ocs;
                        }

                        if (circuitIndex == ocsCircuitList.size() - 1) {

                            if (bestOCS == null) {
                                bestOCS = ocs;
                            }

                            ocsConcatenation.add(bestOCS);
                            hopIndex = physicHopRoute.size() + 1;//Retorno el index hasta el nodo head-end.
                            segmentHead = backwardHop;
                        }
                    } else {
                        //System.out.println("El origen y final del OCS no coinciden con los argumentos de búsqueda del OCS.");
                    }
                }
            }
        }



        return ocsConcatenation;
    }

    /**
     * @param msg
     * @param evaluationTime
     * @param ocsList
     * @return The bandwidht estimated in Mbps of the shortest path of OCSs
     */
    private double getEstimatedBandwidhtToGrant(int trafficPriority, Time evaluationTime, ArrayList<OCSRoute> ocsList) {

        double BANDWIDHT_FIT_PERCENT = 0.85D;//Variable para ajustar a un 30% menos del promedio aritmetico.
        int circuitsAmount = 0;

        double aggregateBandwidthAvailable = 0;
        int aggregateNumberOfChannels = 0;

        for (OCSRoute ocs : ocsList) {

            Entity ocsSource = ocs.getSource();
            int wavelenghtStartsOCS = ocs.getWavelength();
            Entity nextHop = ocs.findNextHop(ocsSource);
            GridOutPort outportToNextHop = ocsSource.findOutPort(nextHop);

            aggregateBandwidthAvailable += ocsSource.getFreeBandwidth(outportToNextHop, wavelenghtStartsOCS, evaluationTime);
            aggregateNumberOfChannels += ocsSource.getChannelsSize(outportToNextHop, wavelenghtStartsOCS, evaluationTime);

            circuitsAmount++;
        }

        double avgBandwidthAvai = aggregateBandwidthAvailable / circuitsAmount;

        int avgNumberOfChannels = aggregateNumberOfChannels / circuitsAmount;

        return Sender.getBandwidthToGrant((BANDWIDHT_FIT_PERCENT * avgBandwidthAvai), trafficPriority, avgNumberOfChannels);
    }

    private ArrayList<OCSRoute> getFullDefaultOCSsSupportBWRequest(double b, Time evaluationTime, OCSRoute ocsToExtractDefaults) {

        ArrayList<OCSRoute> fullDefaultOCSs = new ArrayList();

        Entity ocsSource = ocsToExtractDefaults.getSource();
        Entity ocsDestination = ocsToExtractDefaults.getDestination();
        Routing physicTopology = simulator.getPhysicTopology();

        if (physicTopology.getNrOfHopsBetween(ocsSource, ocsDestination) > 0) {

            Route physicHopRoute = physicTopology.findOCSRoute(ocsSource, ocsDestination);
            Entity segmentHead = ocsSource;

            Entity nextHop;
            for (int indexHop = 1; indexHop < physicHopRoute.size(); indexHop++) {

                nextHop = ocsToExtractDefaults.get(indexHop);

                for (Object objPossDefaultOCS : simulator.returnOcsCircuit(segmentHead, nextHop)) {

                    OCSRoute possDefaultOCS = (OCSRoute) objPossDefaultOCS;

                    if (possDefaultOCS.getWavelength() == 0) {//Todos los ocss default tienen longitud de onda igual a 0

                        if (b <= segmentHead.getFreeBandwidth(segmentHead.getOutportTo(nextHop), 0, evaluationTime)) {
                            fullDefaultOCSs.add(possDefaultOCS);
                            segmentHead = nextHop;
                        } else {
                            //Si alguno de los ocs defaults no soporta b, no tiene sentido seguir sacando los defaults.
                            return null;
                        }
                    }
                }
            }

        } else {
            //El ocs ocsToExtractDefaults es un default.
            if (ocsToExtractDefaults.getWavelength() == 0) {

                if (b <= ocsSource.getFreeBandwidth(ocsSource.getOutportTo(ocsDestination), 0, evaluationTime)) {
                    fullDefaultOCSs.add(ocsToExtractDefaults);
                    return fullDefaultOCSs;
                }
            }
            return null;
        }

        return fullDefaultOCSs;
    }

    /**
     * #########################################################################
     * Inner class to model subCircuits of a route, and if this subCircuit
     * supports the bandwidth requested.
     * #########################################################################
     */
//    public class SubCircuitAbs {
//
//        Entity source;
//        Entity destination;
//        boolean supportBandwidthRequested;
//
//        public SubCircuitAbs(Entity source, Entity destination, boolean supportBandwidthRequested) {
//            this.source = source;
//            this.destination = destination;
//            this.supportBandwidthRequested = supportBandwidthRequested;
//        }
//
//        public Entity getSource() {
//            return source;
//        }
//
//        public void setSource(Entity source) {
//            this.source = source;
//        }
//
//        public Entity getDestination() {
//            return destination;
//        }
//
//        public void setDestination(Entity destination) {
//            this.destination = destination;
//        }
//
//        public boolean isSupportBandwidthRequested() {
//            return supportBandwidthRequested;
//        }
//
//        public void setSupportBandwidthRequested(boolean supportBandwidthRequested) {
//            this.supportBandwidthRequested = supportBandwidthRequested;
//        }
//    }
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

    public static GridOutPort getGridOutPort(HybridSwitchImpl firstSwicth, HybridSwitchImpl lastSwicth) {
        OBSSender obsSender = (OBSSender) ((HybridSwitchSender) firstSwicth.getSender()).getObsSender();
        Map<String, GridOutPort> routingMap = ((OBSSender) obsSender).getRoutingMap();
        GridOutPort gridOutPort = routingMap.get(lastSwicth.getId());
        return gridOutPort;
    }
}