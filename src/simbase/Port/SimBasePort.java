/*
 * This is an abstract class representing a port in the simulator. 
 */
package simbase.Port;

import simbase.*;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public abstract class SimBasePort implements Comparable {

    /**
     * The port ID
     */
    protected String id = "";
    /**
     * The port owner
     */
    protected SimBaseEntity owner = null;

    /**
     * Constructor
     * 
     * @param id
     *            inport ID
     * @param owner
     *            the SimBaseEntity to which the inport belongs
     */
    public SimBasePort(String id, SimBaseEntity owner) {
        this.id = id;
        this.owner = owner;
    }

    public int compareTo(Object o) {
        return id.compareTo(o.toString());
    }

    /**
     * Returns the inport ID
     * 
     * @return the inport ID
     */
    public String getID() {
        return id;
    }

    /**
     * Returns the owner of this port.
     * 
     * @return The owner of this port.
     */
    public SimBaseEntity getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the in port.
     * @param owner the new owner
     * @since 1.1
     */
    public void setOwner(SimBaseEntity owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimBasePort) {
            return this.getID().equals(obj.toString());
        } else {
            throw new IllegalArgumentException("Cannot compare a SimbasePort with " + obj.getClass());
        }
    }
}
