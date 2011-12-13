/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes.ResourceScheduler;

import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ResourceSelector;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class UniformSelector implements ResourceSelector {

    /**
     * The list of resources this selector is responsible for.
     */
    private List<ResourceNode> resources;

    public UniformSelector(List<ResourceNode> resources) {
        this.resources = resources;
    }

    public ResourceNode findBestResource(List<ResourceNode> resources) {
        Random r = new Random();
        if (!resources.isEmpty()) {
            int index = r.nextInt(resources.size());
            return resources.get(index);
        } else {
            return null;
        }

    }

    public ResourceNode findBestresource() {
        return findBestResource(resources);
    }
}
