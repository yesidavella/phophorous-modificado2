/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes.ResourceScheduler;

import Grid.GridSimulator;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ResourceSelector;
import Grid.Interfaces.ServiceNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Jens Buysse
 */
public class StandardSelector implements ResourceSelector {

    protected List<ResourceNode> resources;
    protected ServiceNode service;
    protected GridSimulator sim;

    public StandardSelector(List<ResourceNode> resources, ServiceNode service, GridSimulator sim) {
        this.resources = resources;
        this.service = service;
        this.sim = sim;
    }

    public ResourceNode findBestResource(List<ResourceNode> resourcesList,double jobFlops) {
        //find all available resourcesList
        List<ResourceNode> available = new ArrayList<ResourceNode>();
        Iterator<ResourceNode> it = resourcesList.iterator();
        while (it.hasNext()) {
            ResourceNode resource = it.next();
            if (resource.getQueuingSpace() > 0) {
                available.add(resource);
            }
        }
        if (!available.isEmpty()) {
            int hops = Integer.MAX_VALUE;
            Iterator<ResourceNode> hopIterator = available.iterator();
            ResourceNode theReturnedResource = null;
            while (hopIterator.hasNext()) {
                ResourceNode resource = hopIterator.next();
                int value = sim.getNrOfHopsBetween(service, resource);
                if (value < hops) {
                    hops = value;
                    theReturnedResource = resource;
                }
            }
            return theReturnedResource;
        } else {
            return null;
        }
    }

    public ResourceNode findBestresource(double jobFlops) {
        return findBestResource(resources, jobFlops);
    }
}
