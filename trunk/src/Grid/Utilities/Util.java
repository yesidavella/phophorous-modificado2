/*
 * Class which helps to create the Grid network.
 */
package Grid.Utilities;

import Distributions.ConstantDistribution;
import Distributions.DDNegExp;
import Grid.Entity;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Interfaces.Switches.OBSSwitch;
import Grid.Interfaces.Switches.OCSSwitch;
import Grid.Nodes.Hybrid.Parallel.HybridClientNodeImpl;
import Grid.Nodes.Hybrid.Parallel.HybridResourceNode;
import Grid.Nodes.Hybrid.Parallel.HybridServiceNode;
import Grid.Nodes.Hybrid.Parallel.HybridSwitchImpl;
import Grid.Nodes.Hybrid.Parallel.OuputSwitchForHybridCase;
import Grid.Nodes.OBS.OBSClientImpl;
import Grid.Nodes.OCS.OCSClientNodeImpl;
import Grid.Nodes.OBS.OBSSwitchImpl;
import Grid.Nodes.OCS.OCSSwitchImpl;
import Grid.Nodes.OutputResourceNode;
import Grid.Nodes.OBS.OBSResourceNodeImpl;
import Grid.Nodes.OCS.OCSResourceNodeImpl;
import Grid.Nodes.OBS.OBSServiceNodeImpl;
import Grid.Nodes.OCS.OCSServiceNodeImpl;
import Grid.OCS.OCSRoute;
import Grid.Port.GridInPort;
import Grid.Port.GridOutPort;
import java.util.List;
import simbase.SimBaseSimulator;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class Util {

    private static void insertOptionsForClient(ClientNode client, GridSimulator simulator) {
        client.getState().setJobInterArrival(new DDNegExp(simulator,
                GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.defaultJobIAT)));
        client.getState().setFlops(new DDNegExp(simulator,
                GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.defaultFlopSize)));
        client.getState().setMaxDelayInterval(new DDNegExp(simulator,
                GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.maxDelay)));

        client.getState().setSizeDistribution(new DDNegExp(simulator,
                GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.defaultDataSize)));
        double ackSize = GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.ACKsize);
        if (ackSize == 0) {
            client.getState().setAckSizeDistribution(new ConstantDistribution(ackSize));
        } else {
            client.getState().setAckSizeDistribution(new DDNegExp((SimBaseSimulator) simulator, ackSize));
        }
    }

    /**
     * Creates a OBS client with default parameters.
     */
    public static ClientNode createOBSClient(String id, GridSimulator simulator) {
        ClientNode client = new OBSClientImpl(id, simulator);
        insertOptionsForClient(client, simulator);
        simulator.register(client);
        return client;
    }

    /**
     * Creates a OBS client with default parameters.
     */
    public static ClientNode createOBSClient(
            String id, GridSimulator simulator, ServiceNode service) {
        ClientNode client = new OBSClientImpl(id, simulator, service);
        insertOptionsForClient(client, simulator);
        //TODO: instellen van de correcte paramters.
        simulator.register(client);
        return client;
    }

    public static ClientNode createOCSClient(
            String id, GridSimulator simulator, ServiceNode service) {
        ClientNode client = new OCSClientNodeImpl(id, simulator, service);
        insertOptionsForClient(client, simulator);
        //TODO: instellen van de correcte paramters.
        simulator.register(client);
        return client;
    }

    public static ClientNode createHybridClient(String id, GridSimulator simulator, ServiceNode service) {
        ClientNode client = new HybridClientNodeImpl(id, simulator, service);
        insertOptionsForClient(client, simulator);
        simulator.register(client);
        return client;
    }
    
    public static ClientNode createHybridClient(String id, GridSimulator simulator) {
        ClientNode client = new HybridClientNodeImpl(id, simulator);
        insertOptionsForClient(client, simulator);
        simulator.register(client);
        return client;
    }
    

    private static void insertOptionsForResource(ResourceNode resource, GridSimulator simulator) {
        resource.setCpuCapacity(GridSimulation.configuration.getDoubleProperty(
                Config.ConfigEnum.defaultCapacity));
        resource.setQueueSize(GridSimulation.configuration.getIntProperty(
                Config.ConfigEnum.defaultQueueSize));
        resource.setCpuCount(GridSimulation.configuration.getIntProperty(
                Config.ConfigEnum.defaultCPUCount), GridSimulation.configuration.getDoubleProperty(
                Config.ConfigEnum.defaultCapacity));
        resource.setCpuCapacity(GridSimulation.configuration.getDoubleProperty(
                Config.ConfigEnum.defaultCapacity));
    }

    /**
     * Creates a OBS resource with default parameters.
     */
    public static ResourceNode createOBSResource(
            String id, GridSimulator simulator) {
        ResourceNode resource = new OBSResourceNodeImpl(id, simulator);
        insertOptionsForResource(resource, simulator);
        simulator.register(resource);
        return resource;
    }

    /**
     * Creates a OBS resource with default parameters.
     */
    public static ResourceNode createOBSResource(
            String id, GridSimulator simulator, int nrOfCpus,
            double cpuCapacity) {
        ResourceNode resource = new OBSResourceNodeImpl(id, simulator);
        resource.setCpuCapacity(GridSimulation.configuration.getDoubleProperty(
                Config.ConfigEnum.defaultCapacity));
        resource.setQueueSize(GridSimulation.configuration.getIntProperty(
                Config.ConfigEnum.defaultQueueSize));
        resource.setCpuCount(nrOfCpus, cpuCapacity);
        resource.setCpuCapacity(GridSimulation.configuration.getDoubleProperty(
                Config.ConfigEnum.defaultCapacity));
        simulator.register(resource);
        return resource;
    }

    /**
     * Creates a OCS resource with default parameters.
     */
    public static ResourceNode createOCSResource(
            String id, GridSimulator simulator) {
        ResourceNode resource = new OCSResourceNodeImpl(id, simulator);
        insertOptionsForResource(resource, simulator);
        simulator.register(resource);
        return resource;
    }

    public static ResourceNode createHyridResourceNode(String id, GridSimulator simulator) {
        ResourceNode resource = new HybridResourceNode(id, simulator);
        insertOptionsForResource(resource, simulator);
        simulator.register(resource);
        return resource;
    }

    /**
     * Creates a OBS Swicht with default parameters.
     * @param id The id of the switch
     * @param simulator The simulator
     * @return A OBSSwitch with default paramters.
     */
    public static OBSSwitch createOBSSwitch(
            String id, GridSimulator simulator, boolean waveLengthConversion) {
        OBSSwitch sw = new OBSSwitchImpl(id, simulator, waveLengthConversion);
        sw.setHandleDelay(new Time(GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OBSHandleTime)));
        simulator.register(sw);
        return sw;
    }

    /**
     * Creates a OBS Swicht with default .
     * @param id The id of the switch
     * @param simulator The simulator
     * @param handleDelay The handleDelay of this OBSSwitch.
     * @return A OBSSwitch with default paramters.
     */
    public static Switch createOBSSwitch(
            String id, GridSimulator simulator, double handleDelay, boolean waveLengthConversion) {
        OBSSwitch sw = new OBSSwitchImpl(id, simulator, waveLengthConversion);
        sw.setHandleDelay(new Time(handleDelay));
        simulator.register(sw);
        return sw;
    }

    /**
     * Creates a OBS Swicht with default parameters.
     * @param id The id of the switch
     * @param simulator The simulator
     * @return A OBSSwitch with default paramters.
     */
    public static Switch createOBSSwitch(
            String id, GridSimulator simulator, boolean waveLengthConversion, int waveLengths) {
        OBSSwitch sw = new OBSSwitchImpl(id, simulator, waveLengthConversion);
        sw.setHandleDelay(new Time(GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OBSHandleTime)));
        simulator.register(sw);
        return sw;
    }

    /**
     * Creates a OCS Switch with defaults.
     */
    public static Switch createOCSSwitch(
            String id, GridSimulator simulator, double handleDelay) {
        OCSSwitch sw = new OCSSwitchImpl(id, simulator);
        sw.setHandleDelay(new Time(handleDelay));
        simulator.register(sw);
        return sw;
    }

    /**
     * Creates a OCS Switch with defaults.
     */
    public static Switch createOCSSwitch(
            String id, GridSimulator simulator, double handleDelay, int wavelengths) {
        OCSSwitch sw = new OCSSwitchImpl(id, simulator);
        sw.setHandleDelay(new Time(handleDelay));
        simulator.register(sw);
        return sw;
    }

    /**
     * Creates a Hybrid Switch,
     * @param id The id of this switch
     * @param simulator The simulator to which this swithc belongs.
     * @return
     */
    public static Switch createHybridSwitch(String id, GridSimulator simulator) {
        Switch sw = new HybridSwitchImpl(id, simulator);
        sw.setHandleDelay(new Time(GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OBSHandleTime)));
        simulator.register(sw);
        return sw;
    }

    /**
     * Creates a Hybrid Output Switch
     */
    public static Switch createHybridOutputSwitch(String id, GridSimulator simulator) {
        Switch sw = new OuputSwitchForHybridCase(id, simulator);
        sw.setHandleDelay(new Time(GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OBSHandleTime)));
        simulator.register(sw);
        return sw;
    }

    /**
     * Creates an Outputresource node : for testing queuing systems
     * @param id
     * @param simulator
     * @return A new outputresource
     */
    public static ResourceNode createOutputResource(
            String id, GridSimulator simulator) {
        ResourceNode resource = new OutputResourceNode(id, simulator);
        simulator.register(resource);
        return resource;
    }

    public static void createOCSSwitch() {
        throw new UnsupportedOperationException();
    }

    public static void createHybridSwitch() {
        throw new UnsupportedOperationException();
    }

    public static ServiceNode createOBSServiceNode(
            String id, GridSimulator sim) {
        ServiceNode service = new OBSServiceNodeImpl(id, sim);

        sim.register(service);
        return service;
    }

    public static ServiceNode createOCSServiceNode(
            String id, GridSimulator sim) {
        ServiceNode service = new OCSServiceNodeImpl(id, sim);
        sim.register(service);
        return service;
    }

    public static ServiceNode createHybridServiceNode(String id, GridSimulator sim) {
        ServiceNode service = new HybridServiceNode(id, sim);

        sim.register(service);
        return service;
    }

    /**
     * Creates a one way link between from and to.
     * @param from The first end of the link
     * @param to The second end of the link.
     * @throws IllegalEdgeException 
     */
    public static void createLink(Entity from, Entity to) throws IllegalEdgeException {
        if (from.supportsOBS() == to.supportsOBS() && from.supportsOCS() == to.supportsOCS()) {
            StringBuffer buffer = new StringBuffer(from.getId());
            buffer.append("-");
            buffer.append(to.getId());
            GridOutPort out = new GridOutPort(buffer.toString(), from,
                    GridSimulation.configuration.getDoubleProperty(
                    Config.ConfigEnum.switchingSpeed),
                    GridSimulation.configuration.getDoubleProperty(
                    Config.ConfigEnum.linkSpeed),
                    GridSimulation.configuration.getIntProperty(
                    Config.ConfigEnum.defaultWavelengths));
            GridInPort in = new GridInPort(buffer.toString(), to);
            out.setTarget(in);
            in.setSource(out);
            from.addOutPort(out);
            to.addInPort(in);
        } else {
            throw new IllegalEdgeException("Cannot connect two entities which do not share the same swithcing protocols " +
                    from.getId() + " -->" + to.getId());
        }

    }

    public static void createLink(Entity from, Entity to, int wavelengths) throws IllegalEdgeException {
        if (from.supportsOBS() == to.supportsOBS() && from.supportsOCS() == to.supportsOCS()) {
            StringBuffer buffer = new StringBuffer(from.getId());
            buffer.append("-");
            buffer.append(to.getId());
            GridOutPort out = new GridOutPort(buffer.toString(), from,
                    GridSimulation.configuration.getDoubleProperty(
                    Config.ConfigEnum.switchingSpeed),
                    GridSimulation.configuration.getDoubleProperty(
                    Config.ConfigEnum.linkSpeed),
                    wavelengths);
            GridInPort in = new GridInPort(buffer.toString(), to);
            out.setTarget(in);
            in.setSource(out);
            from.addOutPort(out);
            to.addInPort(in);
        } else {
            throw new IllegalEdgeException("Cannot connect two entities which do not share the same swithcing protocols " +
                    from.getId() + " -->" + to.getId());
        }

    }

    /**
     * Create a bi directional link between from and to
     * @param from The first end of the link
     * @param to The second end of the link.
     */
    public static void createBiDirectionalLink(Entity from, Entity to) {
        try {
            Util.createLink(from, to);
            Util.createLink(to, from);
        } catch (IllegalEdgeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Create a bi directional link between from and to
     * @param from The first end of the link
     * @param to The second end of the link.
     */
    public static void createBiDirectionalLink(Entity from, Entity to, int wavelengths) {
        try {
            Util.createLink(from, to, wavelengths);
            Util.createLink(to, from, wavelengths);
        } catch (IllegalEdgeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Create a bi directional between from and to.
     * @param from The first end of the link.
     * @param to The other end of the link
     * @param sim The simulator to which these entities belongs to.
     */
    public static void createBiDirectionalLink(String from, String to, GridSimulator sim) {
        {
            Entity fromEnt = (Entity) sim.getEntityWithId(from);
            Entity toEnt = (Entity) sim.getEntityWithId(to);
            createBiDirectionalLink(fromEnt, toEnt);
        }

    }

    /***************************************************************************
     * *************************OCS PATH SETUP *********************************
     ***************************************************************************/
    /**
     * Creates an OCS circuit between the source entity and the destination entity.
     * Intermediate hops are added according to the routing mechanism of the
     * simulator. Default = ShortestPath.
     * @param source The starting point of the circuit.
     * @param destination The end point of the circuit.
     * @param gridSim The simulotor to which the entities belong
     * @param permanent flag if this is a permanent circuit : true permanent, false not permanent.
     */
    public static void createOCSCircuit(Entity source, Entity destination, GridSimulator gridSim, boolean permanent, Time t) {
        OCSRoute ocsRoute = gridSim.getRouting().findOCSRoute(source, destination);
        source.requestOCSCircuit(ocsRoute, permanent, t);
        if (permanent) {
            gridSim.addRequestedCircuit(ocsRoute);
        }
    }

    public static void createOCSCircuit(Entity source, Entity destination, GridSimulator gridSim, boolean permanent) {
        OCSRoute ocsRoute = gridSim.getRouting().findOCSRoute(source, destination);
        source.requestOCSCircuit(ocsRoute, permanent, gridSim.getMasterClock());
        if (permanent) {
            gridSim.addRequestedCircuit(ocsRoute);
        }
    }

    public static void createOCSCircuit(Entity source, Entity destination, GridSimulator gridSim, boolean permanent, List<Entity> path) {
        createOCSCircuit(source, destination, gridSim, permanent, gridSim.getMasterClock(), path);
    }

    public static void createOCSCircuit(Entity source, Entity destination, GridSimulator gridSim, boolean permanent, Time t, List<Entity> path) {
        OCSRoute ocsRoute = new OCSRoute(source, destination, -1);
        for (Entity hop : path) {
            ocsRoute.add(hop);
        }
        source.requestOCSCircuit(ocsRoute, permanent, t);
        if (permanent) {
            gridSim.addRequestedCircuit(ocsRoute);
        }
    }

    /**
    /***************************************************************************
     * *************************ERLANG COMPUTATIONS*****************************
     ***************************************************************************/
    /**
     * Return the erlangB calculation>
     * @param IAT Inter arrival time
     * @param T Job processing time
     * @param m Number of resources.
     * @return ErlangB Computation.
     */
    public static double getErlang(double IAT, double T, double m) {
        double lambda = 1 / IAT;
        double mu = 1 / T;


        double E = lambda / mu;
        double B = Math.pow(E, m);
        B =
                B / (fac(m));

        double sum = 0.0;
        for (int i = 0; i <=
                m; i++) {
            sum += (Math.pow(E, i)) / (fac(i));
        }

        return B / sum;

    }

    /**
     * Return the faculty - non recursively
     * @param m
     * @return Teh faculty of m
     */
    public static double fac(double m) {
        if (m == 0 || m == 1) {
            return 1;
        } else {
            double fac = m;
            for (double i = m - 1; i >
                    1; i--) {
                fac = fac * i;
            }

            return fac;
        }
    }
}
