/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Routing;

import Grid.Entity;
import java.io.Serializable;

/**
 *
 * @author Jens Buysse
 */
public class GridVertex implements Comparable, Serializable {

    private Entity theEntity;

    public GridVertex(Entity entity) {
        this.theEntity = entity;

    }

    @Override
    public String toString() {
        return theEntity.getId();
    }

    @Override
    public boolean equals(Object o) {
        return theEntity.toString().equals(o.toString());
    }

    public int compareTo(Object o) {
        return theEntity.toString().compareTo(o.toString());
    }

    public Entity getTheEntity() {
        return theEntity;
    }

    public void setTheEntity(Entity theEntity) {
        this.theEntity = theEntity;
    }
    
    
}
