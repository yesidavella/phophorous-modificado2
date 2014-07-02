/**
 * This class is a hopcount-dataprovider which will return
 * a double value of 1.0 when the double-value of an edgeID or nodeID
 * is asked. This way the HopCountDataProvider will return a cost of
 * 1.0 for every edgeID and can be used to compute the shortestpath
 * with a hopcount as cost. The other implemented methods of
 * IDDataProvider, getObjectID, getIntID, getBoolID return
 * respectively null, 0 and false.
 * This class will return the max value for an integer if the edge is connected
 * to a node which is not a switch. This way, no routing will be done traversing
 * a non-switching node.
 */
package Grid.Routing;

import Grid.Entity;
import Grid.GridSimulator;
import java.util.Iterator;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import trs.core.IDDataProvider;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class EdgeDataProvider implements IDDataProvider {

    /**
     * The list with the entities which do not support switching.
     */
    private Set<String> edgeNodes;

    /**
     * Constructor
     */
    public EdgeDataProvider(GridSimulator sim) {
        edgeNodes = new TreeSet<String>();
        Iterator it = sim.getEntities().iterator();
        while (it.hasNext()) {
            Entity ent = (Entity) it.next();
            if (!ent.supportSwitching()) {
                edgeNodes.add(ent.getId());
            }
        }

    }

    /**
     * This will return a false since this is not the method used within the
     * shortestpath-algorithm
     *
     * @param id the object indicating the ID associated with the edge or node
     *
     * @return false since this method should not be used.
     *
     * @see trs.core.IDDataProvider#getBoolID(Object)
     */
    public boolean getBoolID(String arg0) {
        return false;
    }

    /**
     * This method will return a double-value 1.0  for a non edge node since this is the cost
     * associated to each edge for a hopcount, of a very high number, so that the routing
     * algorithm will not use this as an intermediate hop.
     *
     * @param id the object indicating the ID associated with the edge or node
     *
     * @return 1.0        the double-value of 1.0 according to a hopcount of 5000 for a edge hop.
     *
     * @see trs.core.IDDataProvider#getDoubleID(Object)
     */
    public double getDoubleID(String id) {
        StringTokenizer tok = new StringTokenizer(id, "-");
        String first = tok.nextToken();
        String second = tok.nextToken();
        if (edgeNodes.contains(first) || edgeNodes.contains(second)) {
            return 5000;
        } else {
            return 1;
        }
    }

    /**
     * This method will return a double-value 1.0  for a non edge node since this is the cost
     * associated to each edge for a hopcount, of a very high number, so that the routing
     * algorithm will not use this as an intermediate hop.
     *
     * @param id the object indicating the ID associated with the edge or node
     *
     * @return 1.0        the double-value of 1.0 according to a hopcount of 5000 for a edge hop
     *
     * @see trs.core.IDDataProvider#getDoubleID(Object)
     */
    public int getIntID(String id) {
        StringTokenizer tok = new StringTokenizer(id, "-");
        String first = tok.nextToken();
        String second = tok.nextToken();
        if (edgeNodes.contains(first) || edgeNodes.contains(second)) {
            return 5000;
        } else {
            return 1;
        }
    }

    public Object getObjectID(String arg0) {
        return null;
    }
}
