/*
 * Interface which gives routingtables to the GridEntities.
 */

package Grid.Routing;

import Grid.Entity;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import java.util.Map;

/**
 *
 * @author Jens Buysse
 */
public interface Routing {

    public static int PUREOBS = 0;
    public static int PUREOCS = 1;
    public static int HYBRID = 2;
    
    /**
     * Method which calculates the routes between the different GridEntities
     * in the simulation.
     */
    public void route();
    
    /**
     * This method returns a routingtable for a Entity. In consistst of a 
     * map containing <Destiantion,GridOutPort> mappings. 
     * 
     * @param entity The gridentity which request a routingmap.
     * @return The routing map for the enity.
     */
    public Map<String,GridOutPort> getRoutingTable(Entity entity);
    

    /**
     * Clears every routing mechanism
     */
    public void clear();

    /**
     * This method is called when a permanent OCS circuit has been established.
     * This method then creates an extra edge in the network graph which depicts
     * the OCS-route. This way, a serie of OBS link on which a OCS circuit has been
     * established can be seen as one edge and as such the shortes routing algorithm
     * is corrected.
     * @param ocsRoute
     */
    public void OCSCircuitInserted(OCSRoute ocsRoute);

    /**
     * Returns the number of hops between source and destination.
     * @param source The source entity
     * @param destination The destination entity
     * @return
     */
    public int getNrOfHopsBetween(Entity source, Entity destination);
    
    public OCSRoute findOCSRoute(Entity source, Entity destination);


}
