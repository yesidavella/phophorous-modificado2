/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main.Factories.Hybrid;

import Grid.GridSimulator;
import Grid.Interfaces.ResourceNode;
import Grid.Routing.GridVertex;
import org.apache.commons.collections15.Factory;

/**
 *
 * @author Eothein
 */
public class HybridResourceFactory implements Factory<GridVertex> {

    private static int resourceNr = -1;
    private GridSimulator simulator;

    public HybridResourceFactory(GridSimulator simulator) {
        this.simulator = simulator;
    }

    public GridVertex create() {
        resourceNr++;
        ResourceNode resource = Grid.Utilities.Util.createHyridResourceNode("Resource_"+resourceNr, simulator);
        GridVertex resourceVertex = new GridVertex(resource);
        return resourceVertex;
    }
}
