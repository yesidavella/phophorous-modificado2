/*
 * Changelog
 * ---------
 * Version 1.1
 *  - added setOwner
 *  
 */
package simbase.Port;

import simbase.*;

/**
 * The SimBaseInPort object represents the receiving end of links between
 * SimBaseEntities
 * 
 * @version 1.1
 */
public class SimBaseInPort extends SimBasePort {

    /**
     * The target this port is linked to
     */
    private SimBaseOutPort source = null;

    /**
     * Constructor
     * 
     * @param id
     *            inport ID
     * @param owner
     *            the SimBaseEntity to which the inport belongs
     */
    public SimBaseInPort(String id, SimBaseEntity owner) {
        super(id, owner);
    }

    /**
     * Returns the source outport linked to this inport
     * 
     * @return the source outport linked to this inport
     */
    public SimBaseOutPort getSource() {
        return source;
    }

    /**
     * Sets the source outport linked to this inport
     * 
     * @param s
     *            the source outport linked to this inport
     */
    public void setSource(SimBaseOutPort s) {
        source = s;
    }

}
