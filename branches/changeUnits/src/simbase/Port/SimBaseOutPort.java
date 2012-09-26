package simbase.Port;

import simbase.*;

/**
 * The SimBaseOutPort object represents the outgoing end of links between
 * SimBaseEntities
 * 
 * @version 1.0
 */
public class SimBaseOutPort extends SimBasePort {

    /**
     * The target
     */
    private SimBaseInPort target = null;

    /**
     * Constructor
     * 
     * @param id
     *            outport ID
     * @param owner
     *            the SimbaseEntity to which the outport belongs
     */
    public SimBaseOutPort(String id, SimBaseEntity owner) {
        super(id, owner);
    }

    /**
    /**
     * Returns the target inport
     * 
     * @return the target inport
     */
    public SimBaseInPort getTarget() {
        return target;
    }

    /**
     * Sets the target inport
     * 
     * @param t
     *            the target inport
     */
    public void setTarget(SimBaseInPort t) {
        target = t;
    }


    
    


}
