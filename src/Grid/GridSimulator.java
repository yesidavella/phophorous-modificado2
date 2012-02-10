/*
 * This class extends the SimBaseSimulaotr, by giving it extra properties,
 * typical for grid simulations.
 */
package Grid;

import Grid.OCS.OCSRoute;
import Grid.OCS.CircuitList;
import Grid.Routing.Routing;
import Grid.Routing.RoutingViaJung;
import Grid.Routing.ShortesPathRouting;
import Grid.Utilities.Config.ConfigEnum;
import Grid.Utilities.HtmlWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import simbase.SimBaseSimulator;
import simbase.Stats.Logger;
import trs.core.Connection;
import trs.core.NetworkRouting;

/**
 *
 * @author Jens Buysse
 */
public class GridSimulator extends SimBaseSimulator {

    /**
     * The ROuting component of the Simulator. Is used for routing algorithms.
     */
    private Routing routing;
    /**
     * A list containing all the OCS routes which have been requested at the moment. (OCs circuits in the network).
     */
    private CircuitList requestedCircuits = new CircuitList();
    /**
     * A list containing all the OCS routes which have been established in the network.
     */
    private CircuitList establishedCircuits = new CircuitList();

    /**
     * Defaultconstructor.
     */
    public GridSimulator() {
        super();
        this.resetAllStats();
        routing = new RoutingViaJung(this);
//        routing = new ShortesPathRouting(this);
        try {
            if (new Boolean(GridSimulation.configuration.getProperty(ConfigEnum.output.toString())).booleanValue()) {
                logger = new Logger(12, new HtmlWriter());
            } else {
                //logger = new Logger(12, System.out);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    /**
     * Return the routing component of the simulator.
     * @return The routing component of the simulator.
     */
    public Routing getRouting() {
        return routing;
    }

    /**
     * Sets the routing component of the simulator.
     * @param routing The routing component of the simulator.
     */
    public void setRouting(Routing routing) {
        this.routing = routing;
    }

    /**
     * Prepares the routing object so that he can give the routing tables
     * to the enitities asking for it.  
     */
    public void route() {
        routing.route();
        
    }

    /**
     * Return the number of hops between the two other hops, according to the
     * routing sheme.
     * @param source The source hop
     * @param destination The destination hop
     * @return The number of hops between source and destination.
     */
    public int getNrOfHopsBetween(Entity source, Entity destination) {
        return routing.getNrOfHopsBetween(source, destination);
    }

    /**
     * Add a OCS circuit setup request to the pending requests.
     * @param route The route of the OCS circuit.
     * @return true if it worked, false if not.
     */
    public boolean addRequestedCircuit(OCSRoute route) {
        return requestedCircuits.add(route);
    }

    /**
     * Confirm that the requested circuit has been set up.
     * @param route The route of the OCS circuit which has been set up.
     * @return True if confirmation was successfull, false if not.
     */
    public boolean confirmRequestedCircuit(OCSRoute route) {
        if (requestedCircuits.contains(route)) {
            requestedCircuits.remove(route);
            routing.OCSCircuitInserted(route);
            return establishedCircuits.add(route);
        } else {
            return false;
        }
    }

    /**
     * Cancels a requested circuit in the network.
     * @param route The route of the circuit that has to be cancelled.
     * @return True if cancellation worked.
     */
    public boolean cancelRequestedCircuit(OCSRoute route) {
        return requestedCircuits.remove(route);
    }

    /**
     * Returns wheter there exists a OCS circuit between the source and the destination.
     * @param source The source of the ocs-circuit.
     * @param destination The destiantion of the ocs-circuit.
     */
    public boolean ocsCircuitAvailable(Entity source, Entity destination) {
        Iterator<OCSRoute> it = establishedCircuits.iterator();
        while (it.hasNext()) {
            OCSRoute ocsRoute = it.next();
            if (ocsRoute.getSource().equals(source) && ocsRoute.getDestination().equals(destination)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the OCS route (circuit) between two entities.
     * @param source The source of the circuit.
     * @param destination The destination of the circuit.
     * @return The ocs route between source and destination.
     */
    public List returnOcsCircuit(Entity source, Entity destination) {
        ArrayList<OCSRoute> circuits = new ArrayList();
        Iterator<OCSRoute> it = establishedCircuits.iterator();
        while (it.hasNext()) {
            OCSRoute ocsRoute = it.next();
            if (ocsRoute.getSource().equals(source) && ocsRoute.getDestination().equals(destination)) {
                circuits.add(ocsRoute);
            }

        }
        if (circuits.isEmpty()) {
            return null;
        } else {
            return circuits;
        }
    }

    /**
     * Removes a circuit from the permanent circuits of the network.
     * @param route The route of the OCS circuit which has been torn down.
     * @return true if removal worked, false if not.
     */
    public boolean circuitTearDown(OCSRoute route) {
        return establishedCircuits.remove(route);
    }
}
