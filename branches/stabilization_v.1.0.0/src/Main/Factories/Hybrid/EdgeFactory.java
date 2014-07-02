/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Main.Factories.Hybrid;

import Grid.Routing.GridEdge;
import org.apache.commons.collections15.Factory;

/**
 *
 * @author Eothein
 */
public class EdgeFactory implements Factory<GridEdge>{

    
    
    public GridEdge create() {
        GridEdge edge = new GridEdge(null, null);
        return edge;
    }
    
    

}
