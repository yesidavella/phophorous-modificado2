/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Routing;

import Grid.Entity;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ServiceNode;
import Grid.OCS.OCSRoute;
import Grid.Port.GridHybridOutPort;
import Grid.Port.GridInPort;
import Grid.Port.GridOutPort;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import java.io.Serializable;
import java.util.*;
import simbase.Port.SimBaseOutPort;
import simbase.SimBaseEntity;

/**
 *
 * @author Jens Buysse
 */
public class RoutingViaJung implements Routing, Serializable {

    private Graph OBSNetwork = new DirectedSparseGraph();
    private Graph OcSNetwork = new DirectedSparseGraph();
    private Graph HybridNetwork = new DirectedSparseGraph();
    private GridSimulator sim;
    private GridVertexSet hybridSet = new GridVertexSet();
    private GridVertexSet OBSSet = new GridVertexSet();
    private GridVertexSet OCSSet = new GridVertexSet();

    public RoutingViaJung(GridSimulator sim) {
        this.sim = sim;
        //System.out.println("Creando obj. Jung para enrutar, Configurar en GridSimulator linea 49.");
    }

    @Override
    public void OCSCircuitInserted(OCSRoute ocsRoute) {

        Entity source = ocsRoute.getSource();
        Entity destination = ocsRoute.getDestination();
        System.out.println("En routing via jung - source :" + source + " destination " + destination);
        if (!sim.ocsCircuitAvailable(source, destination)) {
            //Name creation of this virtual link
            StringBuffer buffer = new StringBuffer();
            buffer.append(source);
            buffer.append("-");
            buffer.append(destination);

            GridHybridOutPort outPort = new GridHybridOutPort(buffer.toString(),
                    source, 0, 0, 0, ocsRoute.getWavelength());
            GridInPort inport = new GridInPort(buffer.toString(), destination);
            outPort.setTarget(inport);
            inport.setSource(outPort);
            source.addOutPort(outPort);
            destination.addInPort(inport);

            List list = findSetAndNetwork(source, destination);
            GridVertexSet set = (GridVertexSet) list.get(0);
            Graph graph = (Graph) list.get(2);

            GridVertex sourceVertex = set.findVertex(source);
            GridVertex destinationVertex = set.findVertex(destination);
            GridEdge edge = new GridEdge(sourceVertex, destinationVertex);
            graph.addEdge(edge, sourceVertex, destinationVertex);

            //Ojo q esto si lo hacen en la clase de shortestPathRouting en OCSCircuitInserted
            //y lo mas probable es q toque darle init() a la entidad.
//            for(GridVertex vertex : set){
//                vertex.getTheEntity().route();
//            }
        }
    }

    /**
     *Clears every routing mechanism.
     */
    @Override
    public void clear() {
        OBSNetwork = null;
        OcSNetwork = null;
        HybridNetwork = null;
        hybridSet = null;
        OBSSet = null;
        OCSSet = null;
        
        System.gc();

        OBSNetwork = new DirectedSparseGraph();
        OcSNetwork = new DirectedSparseGraph();
        HybridNetwork = new DirectedSparseGraph();
        hybridSet = new GridVertexSet();
        OBSSet = new GridVertexSet();
        OCSSet = new GridVertexSet();
    }

    public Graph getHybridNetwork() {
        return HybridNetwork;
    }

    public void setHybridNetwork(Graph HybridNetwork) {
        this.HybridNetwork = HybridNetwork;
    }

    public Graph getOBSNetwork2() {
        return OBSNetwork;
    }

    public void setOBSNetwork(Graph OBSNetwork) {
        this.OBSNetwork = OBSNetwork;
    }

    public GridVertexSet getOBSSet() {
        return OBSSet;
    }

    public void setOBSSet(GridVertexSet OBSSet) {
        this.OBSSet = OBSSet;
    }

    public Graph getOcSNetwork() {
        return OcSNetwork;
    }

    public void setOcSNetwork(Graph OcSNetwork) {
        this.OcSNetwork = OcSNetwork;
    }

    public GridVertexSet getHybridSet() {
        return hybridSet;
    }

    public void setHybridSet(GridVertexSet hybridSet) {
        this.hybridSet = hybridSet;
    }

    public void initialiseNetworks() {
        List<SimBaseEntity> allTheEntities = sim.getEntities();
        GridVertexSet vertices = new GridVertexSet();
        for (SimBaseEntity obj : allTheEntities) {
            Entity ent = (Entity) obj;
            if (ent.supportsOBS() && ent.supportsOCS()) {
                GridVertex vertex = new GridVertex(ent);
                HybridNetwork.addVertex(vertex);
                hybridSet.add(vertex);
            } else if (ent.supportsOBS() && !ent.supportsOCS()) {
                GridVertex vertex = new GridVertex(ent);
                OBSNetwork.addVertex(vertex);
                OBSSet.add(vertex);
            } else if (!ent.supportsOBS() && ent.supportsOCS()) {
                GridVertex vertex = new GridVertex(ent);
                OcSNetwork.addVertex(vertex);
                vertices.add(vertex);
                OCSSet.add(vertex);
            }
        }
    }

    private void createEdges(Graph g, GridVertexSet set) {

        for (GridVertex from : set) {

            List<SimBaseOutPort> outPorts = from.getTheEntity().getOutPorts();
            Iterator<SimBaseOutPort> outportIterator = outPorts.iterator();

            //For each entity get his outports and connect it in the specified graph
            //with its target.

            while (outportIterator.hasNext()) {
                SimBaseOutPort outport = outportIterator.next();
                try {
                    GridVertex to = set.findVertex((Entity) outport.getTarget().getOwner());
                    GridEdge edge = new GridEdge(from, to);
                    g.addEdge(edge, from, to);
                } catch (IllegalArgumentException e) {
                    System.err.println("Could not create an edge in the network : " + e.getMessage() + " " + outport.getOwner().getId() + " " + outport.getTarget().getOwner().getId()
                            + " " + outport.getID());
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    @Override
    public void route() {
        initialiseNetworks();
        createEdges(OBSNetwork, OBSSet);
        createEdges(OcSNetwork, OCSSet);
        createEdges(HybridNetwork, hybridSet);
    }

    /**
     * Returns the routing table for a given entity.
     *
     * @param entity The entity for which the routing table is asked.
     * @return A new routing table.
     */
    @Override
    public Map<String, GridOutPort> getRoutingTable(Entity entity) {
        GridVertex source;
        GridVertexSet set;
        TreeMap<String, GridOutPort> map = new TreeMap();


        DijkstraShortestPath<GridVertex, GridVertex> alg;
        if (entity.supportsOBS() && entity.supportsOCS()) {
            alg = new DijkstraShortestPath(HybridNetwork);
            set = hybridSet;
            source = hybridSet.findVertex(entity);
        } else if (entity.supportsOCS() && !entity.supportsOBS()) {
            alg = new DijkstraShortestPath(OcSNetwork);
            set = OCSSet;
            source = OCSSet.findVertex(entity);
        } else {
            alg = new DijkstraShortestPath(OBSNetwork);
            set = OBSSet;
            source = OBSSet.findVertex(entity);
            //Hybrid part
        }

        for (GridVertex destination : set) {
            if (!destination.equals(source)) {
                List l = alg.getPath(source, destination);
                GridEdge edge = (GridEdge) l.get(0);
                Entity nextHop = edge.getTo().getTheEntity();
                GridOutPort theOutport = entity.getOutportTo(nextHop);
                map.put(destination.getTheEntity().getId(), theOutport);
            }

        }
        return map;
    }

    /**
     * Returns whether this route which is found is valid. A route is not valid
     * when it contains a entity which is capable of switching.
     *
     * @param edges The list with edges
     * @return Null if the route is valid or the first GridVertex which does not
     * support switching.
     */
    private GridVertex isRouteValid(List edges) {
        return null;
    }

    @Override
    public int getNrOfHopsBetween(Entity source, Entity destination) {
        List list = findSetAndNetwork(source, destination);
        DijkstraShortestPath<GridVertex, GridVertex> alg = (DijkstraShortestPath) list.get(1);
        GridVertexSet set = (GridVertexSet) list.get(0);
        GridVertex destinationVertex = set.findVertex(destination);
        GridVertex sourceVertex = set.findVertex(source);
        List l = alg.getPath(sourceVertex, destinationVertex);
        return l.size() - 1;

    }

    private List findSetAndNetwork(Entity source, Entity destination) {
        DijkstraShortestPath<GridVertex, GridVertex> alg = null;
        GridVertexSet set = null;
        Graph graph = null;
        if (source.supportsOBS() && source.supportsOCS()) {
            if (destination.supportsOBS() && destination.supportsOCS()) {
                alg = new DijkstraShortestPath(HybridNetwork);
                set = hybridSet;
                graph = HybridNetwork;
            } else {
                throw new IllegalArgumentException("Cannot compute for : " + source + " " + destination);
            }
        } else if (source.supportsOCS() && !source.supportsOBS()) {
            if (destination.supportsOCS() && !destination.supportsOBS()) {
                alg = new DijkstraShortestPath(OcSNetwork);
                set = OCSSet;
                graph = OcSNetwork;
            } else {
                throw new IllegalArgumentException("Cannot compute for : " + source + " " + destination);
            }
        } else if (!source.supportsOCS() && source.supportsOBS()) {
            if (!destination.supportsOCS() && destination.supportsOBS()) {
                alg = new DijkstraShortestPath(OBSNetwork);
                set = OBSSet;
                graph = OBSNetwork;
            } else {
                throw new IllegalArgumentException("Cannot compute for : " + source + " " + destination);
            }
        }
        ArrayList list = new ArrayList(2);
        list.add(set);
        list.add(alg);
        list.add(graph);
        return list;
    }

    @Override
    public OCSRoute findOCSRoute(Entity source, Entity destination) {
        List list = findSetAndNetwork(source, destination);
        DijkstraShortestPath<GridVertex, GridVertex> alg = (DijkstraShortestPath) list.get(1);
        GridVertexSet set = (GridVertexSet) list.get(0);
        GridVertex destinationVertex = set.findVertex(destination);
        GridVertex sourceVertex = set.findVertex(source);
        List path = alg.getPath(sourceVertex, destinationVertex);
        OCSRoute ocsRoute = new OCSRoute(source, destination, -1);
        for (Object edges : path) {
            GridEdge edge = (GridEdge) edges;
            StringTokenizer tok = new StringTokenizer(edge.toString(), "-");
            tok.nextToken();
            ocsRoute.addHop((Entity) sim.getEntityWithId(tok.nextToken()));
        }
        return ocsRoute;
    }

    public static void main(String[] args) {
        GridSimulation simInstance = new GridSimulation("sources\\configFiles\\yesidsito.cfg ");//configFiles\\hybridcase.cfg
        GridSimulator sim = new GridSimulator();

        simInstance.setSimulator(sim);
        sim.initEntities();

        ServiceNode broker = Grid.Utilities.Util.createHybridServiceNode("Broker", sim);
        ClientNode client = Grid.Utilities.Util.createHybridClient("CLIENT1", sim, broker);
        ClientNode client2 = Grid.Utilities.Util.createHybridClient("CLIENT2", sim, broker);
        ClientNode client3 = Grid.Utilities.Util.createHybridClient("CLIENT3", sim, broker);
        ClientNode client4 = Grid.Utilities.Util.createHybridClient("CLIENT4", sim, broker);
        Grid.Utilities.Util.createBiDirectionalLink(broker, client);
        Grid.Utilities.Util.createBiDirectionalLink(client, client2);
        Grid.Utilities.Util.createBiDirectionalLink(client2, client3);
        Grid.Utilities.Util.createBiDirectionalLink(client3, client4);
        Grid.Utilities.Util.createBiDirectionalLink(client, client4);

        RoutingViaJung routing = new RoutingViaJung(sim);
        routing.route();
        System.out.println(routing.getRoutingTable(client));
        System.out.println(routing.getRoutingTable(client2));
        System.out.println(routing.getRoutingTable(client3));
        System.out.println(routing.getRoutingTable(client4));


    }
}
