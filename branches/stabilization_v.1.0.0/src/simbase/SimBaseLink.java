package simbase;


import Grid.Port.GridOutPort;
import simbase.Port.SimBaseInPort;

/**
 * The SimBaseLink class represents a link between two SimBaseEntities.
 * 
 * @version 1.0
 */
public class SimBaseLink {

    /**
     * The ID of this link.
     */
    protected String ID = null;
    /**
     * The source nodes outport
     */
    public GridOutPort from = null;
    /**
     * The destination nodes inport
     */
    public SimBaseInPort to = null;

    /**
     * Constructor.
     * 
     * @param from
     *            the source nodes outport
     * @param to
     *            the destination nodes inport
     */
    public SimBaseLink(GridOutPort from, SimBaseInPort to) {
        this.from = from;
        this.to = to;
        StringBuffer buffer = new StringBuffer(from.toString());
        buffer.append("--");
        buffer.append(to.toString());
        ID = buffer.toString();
    }

    /**
     *  Returns the ID of the link.
     *  
     * @return The ID of this link.
     */
    public String getID() {
        return ID;
    }

    /**
     * Return the simbase port of the end of this link.
     * 
     * @return The to-simbaseport.
     */
    public SimBaseInPort getTo() {
        return to;
    }

    /**
     * Returns the simbase port at the beginning of this link.
     * 
     * @return The beginning port of this link.
     */
    public GridOutPort getFrom() {
        return from;
    }

    /**
     * Sets the from port
     * @param from The port to set.
     */
    public void setFrom(GridOutPort from) {
        this.from = from;
    }

    /**
     * Sets the ID of this link.
     * 
     * @param ID The new ID of this link.
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * Sets the simbase to port.
     * @param to The new Simbaseto port.
     */
    public void setTo(SimBaseInPort to) {
        this.to = to;
    }

    
    
}
