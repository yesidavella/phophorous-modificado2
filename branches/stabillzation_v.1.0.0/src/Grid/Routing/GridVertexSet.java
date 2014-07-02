/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Routing;

import Grid.Entity;
import java.util.ArrayList;


/**
 *
 * @author Jens Buysse
 */
public class GridVertexSet extends ArrayList<GridVertex> {



    public GridVertex findVertex(Entity entity) {
        for (GridVertex vertex : this) {
            if (vertex.getTheEntity().equals(entity)) {
                return vertex;
            }
        }
        return null;

    }
}
