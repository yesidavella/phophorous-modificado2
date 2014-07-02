/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Main.Factories.Hybrid;

import Grid.GridSimulator;
import Grid.Interfaces.ServiceNode;
import Grid.Routing.GridVertex;
import org.apache.commons.collections15.Factory;

/**
 *
 * @author Eothein
 */
public class HybridServiceFactory implements Factory<GridVertex>{
    
     private static int serviceNr = -1;
    private GridSimulator simulator;

    public HybridServiceFactory(GridSimulator simulator) {
        this.simulator = simulator;
    }

    public GridVertex create() {
        serviceNr++;
        ServiceNode service = Grid.Utilities.Util.createHybridServiceNode("Service_"+serviceNr, simulator);
        GridVertex serviceVertex = new GridVertex(service);
        return serviceVertex;
    }
    
    

}
