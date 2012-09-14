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
    private CostMultiMarkovAnalyzer costMultiMarkovAnalyzer;
    
    public PCE(String id, GridSimulator simulator, double costFindCommonWavelenght, double costAllocateWavelenght) {
        super(id, simulator, costFindCommonWavelenght, costAllocateWavelenght);
        this.simulator = simulator;
        routing = simulator.getRouting();
        costMultiMarkovAnalyzer = new CostMultiMarkovAnalyzer(simulator);
        
    }
    
    public Map<ResourceNode, Double> getMarkovCostList(ClientNode clientNode, List<ResourceNode> resourceNodes, JobAckMessage jobAckMessage) {
        
        Map<ResourceNode, Double> mapResourceNetworkCost = new HashMap<ResourceNode, Double>();
        
        
        
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

                //returna el costo de usar el OCS directo ya creado. Si ningun OCS directo tiene capacidad retorna null
                Double directOCScost = costMultiMarkovAnalyzer.getCostOCSDirect(firstSwicth, lastSwicth, jobDummyMsg, jobAckMessage, firstSwitchCurrentTime, this, opticFlow);
                
                if (directOCScost != null) {
                    mapResourceNetworkCost.put(resourceNode, directOCScost);
                    continue;
                }
                
                //Si llega hasta este punto significa que no existe un OCS directo con suficiente ancho de banda.
                ArrayList<SubCircuitAbs> circuitAbses =   getCircuitsConcatenation(resourceNode, lastSwicth, jobDummyMsg);                
                ArrayList<SubCircuitAbs> circuitAbsesNoAvailable = new ArrayList<SubCircuitAbs>();
                ArrayList<SubCircuitAbs> circuitAbsesAvailable = new ArrayList<SubCircuitAbs>();
                
                //Recorrer todos circuitos que pertenecen a p-lambda.
                for(SubCircuitAbs subCircuitAbs: circuitAbses)
                {
                    if(subCircuitAbs.isSupportBandwidthRequested())
                    {
                        circuitAbsesAvailable.add(subCircuitAbs);
                    }
                    else
                    {
                        circuitAbsesNoAvailable.add(subCircuitAbs);
                    }                    
                }
                 Double  costByThreshold; 
                if(circuitAbsesNoAvailable.isEmpty())
                {
                    costByThreshold= costMultiMarkovAnalyzer.getCostP_LambdaOrCreateNewDirectOCS(firstSwicth, lastSwicth, jobDummyMsg, jobAckMessage, firstSwitchCurrentTime, this, opticFlow);
                   
                }
                else
                {
                    //Crear OCS de los circuitos de p-lamda  que no tienen capacidad 
                    for(SubCircuitAbs subCircuitAbs: circuitAbsesNoAvailable)
                    {
                        ///crear circuito entre M y N.                        
                    }
                    costByThreshold= costMultiMarkovAnalyzer.getCostP_LambdaOrCreateNewDirectOCS(firstSwicth, lastSwicth, jobDummyMsg, jobAckMessage, firstSwitchCurrentTime, this, opticFlow);
                }                
                
                mapResourceNetworkCost.put(resourceNode, costByThreshold);
                
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

    /**
     * @param source The Head of a entire route.
     * @param destination The Head-end of a entire route.
     * @return A list of structures where each structure represents a inner
     * circuit between source and destination and the state of each circuit
     * evaluated in the currentTime of the source node.
     */
    public ArrayList<SubCircuitAbs> getCircuitsConcatenation(Entity source, Entity destination, JobMessage jobDummyMsg) {
        
        Time currentTimeSourceNode = source.getCurrentTime();
        
        return getCircuitsConcatenation(source, destination, currentTimeSourceNode, jobDummyMsg);
    }

    /**
     * @param source The Head of a entire route.
     * @param destination The Head-end of a entire route.
     * @param evaluationTime Time when the concatenation of circuits is
     * avaluated.
     * @return A list of structures where each structure represents a inner
     * circuit between source and destination evaluated in evaluationTime.
     */
    public ArrayList<SubCircuitAbs> getCircuitsConcatenation(Entity source, Entity destination, Time evaluationTime, JobMessage jobDummyMsg) {
        
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

//                    if (bandwidthRequested<= origin.getFreeBandwidth(outportToNextHop, wavelenghtStartOCS, evaluationTime)) {
                    if (putMsgOnLinkTest(jobDummyMsg, outportToNextHop, currentTime, origin)) {
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
