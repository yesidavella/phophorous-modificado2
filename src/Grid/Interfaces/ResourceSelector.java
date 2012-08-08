/*
 * This interfaces all classes which are being used to select a resource
 * from a set of resources. This is the actual sheduling strategy.
 */

package Grid.Interfaces;

import Grid.Entity;
import Grid.Nodes.PCE;
import java.util.List;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public interface ResourceSelector {

    /**
     * Will find the best resource node to send the job to. If no resource could be found
     * than the return value  will be null.
     * @param resources The list with resources the service node is responsible for
     * @return The resource node the job can be send to.
     */
    public ResourceNode findBestResource(Entity clientNode, List<ResourceNode> resources, double jobFlops, PCE pce);

    /**
     * Will find the best resource node to send the job to. If no resource could be found
     * than the return value  will be null.
     * @return
     */
    public ResourceNode findBestresource(double jobFlops);


}
