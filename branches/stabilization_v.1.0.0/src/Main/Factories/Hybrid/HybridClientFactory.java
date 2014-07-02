/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Main.Factories.Hybrid;

import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Routing.GridVertex;
import org.apache.commons.collections15.Factory;

/**
 *
 * @author Eothein
 */
public class HybridClientFactory implements Factory<GridVertex>{

    private static int clientNr= -1;
    
    private GridSimulator simulator;

    public HybridClientFactory(GridSimulator simulator) {
        this.simulator = simulator;
    }
    
    public GridVertex create() {
        clientNr++;
        ClientNode client = Grid.Utilities.Util.createHybridClient("Client_"+clientNr, simulator);
        GridVertex clientVertex = new GridVertex(client);
        return clientVertex;
    }
    
    

}
