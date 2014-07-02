/*
 * With an OCSRequestMessage, you are able to setup an OCS Circuit. It contains
 * a field with the hops in the circuit which should be connected through an OCS
 * circuit.
 */
package Grid.Interfaces.Messages;

import Grid.OCS.OCSRoute;
import Grid.Route;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OCSRequestMessage extends GridMessage {

    /**
     * The OCS path
     */
    private OCSRoute OCSroute;
    /**
     * Flag which tells if the circuit is permanent.
     */
    private boolean permanent = false;
    /**
     * This is the id of the jobMsg that request to create a OCS.
     */
    private String idJobMsgRequestOCS;

    /**
     * Constructor
     *
     * @param id The id of this message
     * @param generationTime The time of generation of this message.
     * @param OCSRoute The OCS route
     */
    public OCSRequestMessage(String id, Time generationTime, OCSRoute OCSroute, boolean permanent) {
        super(id, generationTime);
        this.OCSroute = OCSroute;
        this.source = OCSroute.get(0);
        this.destination = OCSroute.get(OCSroute.size() - 1);
        this.permanent = permanent;
    }

    /**
     * Constructor
     *
     * @param id The id of the message.
     * @param generationTime The thime of generation of this message.
     */
    public OCSRequestMessage(String id, Time generationTime) {
        super(id, generationTime);
    }

    /**
     * Return the route for the OCS circuit.
     *
     * @return The ocs-circuit route.
     */
    public OCSRoute getOCSRoute() {
        return OCSroute;
    }

    /**
     * Sets the OCS-rout circuit.
     *
     * @param OCSRoute The new OCS route circuit.
     */
    public void setOCSRoute(OCSRoute OCSroute) {
        this.OCSroute = OCSroute;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    /**
     * Get the id of the jobMsg that request the creation of the ocs.
     *
     * @return idJobMsgRequestOCS
     */
    public String getIdJobMsgRequestOCS() {
        return idJobMsgRequestOCS;
    }

    /**
     * Set the id of the jobMsg that request to create a OCS.
     *
     * @param idJobMsgRequestOCS
     */
    public void setIdJobMsgRequestOCS(String idJobMsgRequestOCS) {
        this.idJobMsgRequestOCS = idJobMsgRequestOCS;
    }
}
