/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Routing;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.OCS.OCSRoute;
import Grid.Port.GridHybridOutPort;
import Grid.Port.GridInPort;
import Grid.Port.GridOutPort;
import java.io.Serializable;
import java.util.*;
import simbase.Port.SimBaseOutPort;
import simbase.SimBaseEntity;
import simbase.SimBaseSimulator;
import trs.core.*;
import trs.core.routing.NetworkRoutingAlgorithm;
import trs.core.routing.RoutingAlgorithm;
import trs.core.routing.RoutingException;
import trs.core.routing.dynamic.RoutingManager;
import trs.core.routing.networkroutingalgorithms.CompleteRerouting;
import trs.core.routing.routingalgorithms.ShortestPathRoutingAlgorithm;

/**
 *
 * @author Jens Buysse
 */
public class ShortesPathRouting implements Routing,Serializable {

    public int getNrOfHopsBetween(Entity source, Entity destination) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * The simulator
     */
    protected GridSimulator simulator;
    /**
     * The TRS-OBS network.
     */
    protected Network OBSNetwork;
    /**
     * The TRS-OCS network.
     */
    protected Network OCSNetwork;
    /**
     * The TRS-Hybrid network
     */
    protected Network HyrbidNetwork;
    /**
     * The networkrouting object for OBS. Used for holding, manipulating and 
     * representing a sum of connections over the network.
     */
    private NetworkRouting OBSnetworkRouting;
    /**
     * The networkrouting object for OCS.
     */
    private NetworkRouting OCSnetworkRouting;
    /**
     * The networkrouting object for the hybrid part.
     */
    private NetworkRouting HybridNetworkRouting;
    /**
     * The hybrid routing manager.
     */
    private RoutingManager HybridroutingManager;
    /**
     * The OCS routing manager.
     */
    private RoutingManager OCSroutingManager;
    /**
     * The OBS routing manager.
     */
    private RoutingManager OBSroutingManager;
    /**
     * The network routing algorithm.
     */
    private NetworkRoutingAlgorithm networkRoutingAlgo;

    /**
     * The constructor.
     * @param simulator The simulator.
     */
    public ShortesPathRouting(GridSimulator simulator) {
        this.simulator = simulator;
        OBSNetwork = new Network();
        OCSNetwork = new Network();
        HyrbidNetwork = new Network();

        OBSnetworkRouting = new NetworkRouting();
        OCSnetworkRouting = new NetworkRouting();
        HybridNetworkRouting = new NetworkRouting();
        System.out.println("Enrutando con: ShortesPath, Configurar en GridSimulator linea 49.");
    }

    /**
     * Returns the routing table for a given entity.
     * @param entity The entity for which the routing table is asked.
     * @return A new routing table.
     */
    public Map<String, GridOutPort> getRoutingTable(Entity entity) {
        TreeMap<String, GridOutPort> map = new TreeMap();
        List<SimBaseEntity> entities = simulator.getEntities();
        Iterator<SimBaseEntity> it = entities.iterator();
        while (it.hasNext()) {
            SimBaseEntity destination = it.next();

            if (entity != destination) {
                List<Connection> conns;
                if (entity.supportsOBS() && !entity.supportsOCS()) {
                    conns = OBSnetworkRouting.findConnections(entity.getId(),
                            destination.getId());
                } else if (entity.supportsOCS() && !entity.supportsOBS()) {
                    conns = OCSnetworkRouting.findConnections(entity.getId(),
                            destination.getId());
                } else {
                    conns = HybridNetworkRouting.findConnections(entity.getId(),
                            destination.getId());
                //Hybrid part
                }
                if (!conns.isEmpty()) {
                    Connection conn = conns.get(0);

                    Route route = conn.getRoute();
                    if (route != null) {
                        Entity switchNotSupporting = isRouteValid(route, entity, (Entity) destination);
                        if (switchNotSupporting == null) {
                            List<String> edgeIds = route.getEdgeIDs();
                            String portId = edgeIds.get(0);
                            GridOutPort port = entity.getOutport(portId);
                            map.put(destination.getId(), port);
                        } else {
                        //TODO: CHANGE THE ROUTE
                        }
                    }
                }
            }
        }
        return map;
    }

    /**
     * Checks wheter the entites which are hops on the path, support switching.
     * @param route
     * @return The enitity which does not support switching, null if all entities supoort switching
     */
    private Entity isRouteValid(Route route, Entity source, Entity dest) {
        List<String> edgeIds = route.getEdgeIDs();
        for (String ent : edgeIds) {
            StringTokenizer tok = new StringTokenizer(ent, "-");
            Entity entity1 = (Entity) simulator.getEntityWithId(tok.nextToken());
            Entity entity2 = (Entity) simulator.getEntityWithId(tok.nextToken());
            if (entity1.equals(source) || entity2.equals(dest)) {
                continue;
            } else {
                if (!entity1.supportSwitching()) {
                    System.out.println(source + " " + dest + " " + route);
                    return entity1;
                } else {
                    continue;
                }

            }
        }
        return null;

    }

    /**
     * This routes the network. It makes the virtual links between everybody.
     */
    public void route() {
        // Get the list with entities
        List<SimBaseEntity> entities = simulator.getEntities();
        Iterator<SimBaseEntity> it = entities.iterator();
        while (it.hasNext()) {
            //Make for each entity a node in the TRS Network.
            Entity entity = (Entity) it.next();
            if (entity.supportsOBS() && !entity.supportsOCS()) {
                OBSNetwork.createNodeID(entity.getId());
            } else if (entity.supportsOCS() && !entity.supportsOBS()) {
                OCSNetwork.createNodeID(entity.getId());
            } else {
                HyrbidNetwork.createNodeID(entity.getId());
            }
        }

        //Creating the edges
        it = entities.iterator();
        while (it.hasNext()) {
            Entity ent = (Entity) it.next();
            if (ent.supportsOBS() && !ent.supportsOCS()) {
                initialiseEdges(OBSNetwork, ent);
            } else if (ent.supportsOCS() && !ent.supportsOBS()) {
                //OCS portion
                initialiseEdges(OCSNetwork, ent);
            } else {
                //hybrid portion
                initialiseEdges(HyrbidNetwork, ent);
            }
        }

        //TODO;CHECK IF NETWORK IS CONNECTED OR NOT

        //Creating all connections (every entity has to have a connection
        //with all other entities.
        connectTheNetwork(OCSNetwork, OCSnetworkRouting);
        connectTheNetwork(OBSNetwork, OBSnetworkRouting);
        connectTheNetwork(HyrbidNetwork, HybridNetworkRouting);


        // HopCountIDDataProvider provides an edgeCost of one for each edge
        IDDataProvider cost = new EdgeDataProvider(simulator);


        // shortestpath-algorithm uses hopcount for calculations
        RoutingAlgorithm routingAlgo = new ShortestPathRoutingAlgorithm(cost);
        networkRoutingAlgo = new CompleteRerouting(routingAlgo);

        // the routingmanager will keep the networkrouting consistent for this network
        try {
            OBSroutingManager = new RoutingManager(OBSnetworkRouting, OBSNetwork, networkRoutingAlgo);
            OCSroutingManager = new RoutingManager(OCSnetworkRouting, OCSNetwork, networkRoutingAlgo);
            HybridroutingManager = new RoutingManager(HybridNetworkRouting, HyrbidNetwork, networkRoutingAlgo);
            
//            HashMap<String,Capacity> conns = new HashMap<String,Capacity>();
//            for (Iterator itConn = HybridNetworkRouting.getConnections().iterator(); itConn.hasNext();) {
//                Connection conn = (Connection) itConn.next();
//                
//                String a="Cliente_1-Cliente_2";
//                String b="Cliente_2-Cliente_1";
//                
//                if( (conn.getSourceID()+"-"+conn.getTargetID()).equalsIgnoreCase(a) ||  (conn.getSourceID()+"-"+conn.getTargetID()).equalsIgnoreCase(b)){
//                    conns.put(conn.getID(), conn.getCapacity());
//                }
//            }
//            
//            List<Connection> list = HybridNetworkRouting.findConnections("Cliente_1","Cliente_2");
//            COnclusion, toma el camino con menos numberedcapacity de los mas cortos q hayan.
//            System.out.println("");
        } catch (RoutingException e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Created the edges for the specified network, for this entity
     * @param network The network in which the edges are created
     * @param ent The entity for which the edges are created.
     */
    private void initialiseEdges(Network network, Entity ent) {
        List<SimBaseOutPort> outPorts = ent.getOutPorts();
        Iterator<SimBaseOutPort> outportIterator = outPorts.iterator();
        //For each entity get his outports and connect it in the specified TRS network
        //with its target.
        while (outportIterator.hasNext()) {
            SimBaseOutPort outport = outportIterator.next();
            try {
                network.createEdgeID(outport.getOwner().getId(),
                        outport.getTarget().getOwner().getId(), outport.getID());
            } catch (IllegalArgumentException e) {
                System.err.println("Could not create an edge in the network : " + e.getMessage() + " " + outport.getOwner().getId() + " " + outport.getTarget().getOwner().getId() +
                        " " + outport.getID());
                System.exit(1);
            }
        }
    }

    /**
     * Interconnect the network. (Is needed for the TRS component).
     * @param network The network
     * @param routing The routing
     */
    private void connectTheNetwork(Network network, NetworkRouting routing) {
        routing.removeAllConnections();
        List<String> nodeIds = network.getNodeIDs();
        Iterator<String> nodeIterator = nodeIds.iterator();
        while (nodeIterator.hasNext()) {
            String ID = nodeIterator.next();
            Iterator<String> otherIds = nodeIds.iterator();
            while (otherIds.hasNext()) {
                String otherID = otherIds.next();
                if (!otherID.equals(ID)) {
                    Connection connection = new Connection(ID, otherID,
                            NumberedCapacity.create(Math.random()));//Ojo esto estaba en 1, lo modifique para la prueba de q camino escojia con este parametro pequeño
                    routing.addConnection(connection);
                }
            }
        }

    }

    public NetworkRouting getHybridNetworkRouting() {
        return HybridNetworkRouting;
    }

    public void setHybridNetworkRouting(NetworkRouting HybridNetworkRouting) {
        this.HybridNetworkRouting = HybridNetworkRouting;
    }

    public RoutingManager getHybridroutingManager() {
        return HybridroutingManager;
    }

    public void setHybridroutingManager(RoutingManager HybridroutingManager) {
        this.HybridroutingManager = HybridroutingManager;
    }

    public Network getHyrbidNetwork() {
        return HyrbidNetwork;
    }

    public void setHyrbidNetwork(Network HyrbidNetwork) {
        this.HyrbidNetwork = HyrbidNetwork;
    }

    public RoutingManager getOBSroutingManager() {
        return OBSroutingManager;
    }

    public void setOBSroutingManager(RoutingManager OBSroutingManager) {
        this.OBSroutingManager = OBSroutingManager;
    }

    public RoutingManager getOCSroutingManager() {
        return OCSroutingManager;
    }

    public void setOCSroutingManager(RoutingManager OCSroutingManager) {
        this.OCSroutingManager = OCSroutingManager;
    }

    /**
     * This method is called when a permanent OCS circuit has been established.
     * This method then creates an extra edge in the network graph which depicts
     * the OCS-route. This way, a serie of OBS link on which a OCS circuit has been
     * established can be seen as one edge and as such the shortes routing algorithm
     * is corrected.
     * @param ocsRoute
     */
    @Override
    public void OCSCircuitInserted(OCSRoute ocsRoute) {
        try {

            Entity source = ocsRoute.getSource();
            Entity destination = ocsRoute.getDestination();
             System.out.println("En routing via ShortesPathRouting - source :"+source+" destination "+destination);
            if (!simulator.ocsCircuitAvailable(source, destination)) {
                //Name creation of this virtual link
                StringBuffer buffer = new StringBuffer();
                buffer.append(source);
                buffer.append("-");
                buffer.append(destination);

                String edge = HyrbidNetwork.createEdgeID(source.getId(), destination.getId(), buffer.toString());

                GridHybridOutPort outPort = new GridHybridOutPort(buffer.toString(),
                        source, 0, 0, 0, ocsRoute.getWavelength());
                GridInPort inport = new GridInPort(buffer.toString(), destination);
                outPort.setTarget(inport);
                inport.setSource(outPort);
                source.addOutPort(outPort);
                destination.addInPort(inport);

                connectTheNetwork(HyrbidNetwork, HybridNetworkRouting);
                networkRoutingAlgo.calculateNetworkRouting(HybridNetworkRouting, HyrbidNetwork);

                Iterator<String> hybridNodeIt = HyrbidNetwork.getNodeIDs().iterator();
                while (hybridNodeIt.hasNext()) {
                    Entity hybridNode = (Entity) simulator.getEntityWithId(hybridNodeIt.next());
                    hybridNode.init();//recalculate routing map
                }

            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public SimBaseSimulator getSimulator() {
        return simulator;
    }

    public void setSimulator(GridSimulator simulator) {
        this.simulator = simulator;
    }


    public NetworkRouting getOBSNetworkRouting() {
        return OBSnetworkRouting;
    }


    public Network getOBSNetwork() {
        return OBSNetwork;
    }

    public Network getOCSNetwork() {
        return OCSNetwork;
    }

    public NetworkRouting getOCSNetworkRouting() {
        return OCSnetworkRouting;
    }

    /**
     * Clears every routing mechanism
     */
    public void clear() {
        OBSNetwork = null;
        OBSnetworkRouting = null;
        OCSNetwork = null;
        OCSnetworkRouting = null;
        
        System.gc();
        
        OBSNetwork = new Network();
        OCSNetwork = new Network();
        OBSnetworkRouting = new NetworkRouting();
        OCSnetworkRouting = new NetworkRouting();
    }
    //NOTA: por eso es que corre el el OCS Puro
    public OCSRoute findOCSRoute(Entity source, Entity destination) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
