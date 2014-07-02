package Grid.Interfaces.Messages;

import Grid.OCS.OCSRoute;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OCSConfirmSetupMessage extends GridMessage {

    /**
     * The route of the OCS circuit which has been setup.
     */
    private OCSRoute ocsRoute;
    /**
     * This is the id of the jobMsg that request to create a OCS.
     */
    private String idJobMsgRequestOCS;

    /**
     * Constructor
     *
     * @param id The id of the message
     * @param generationTime The tiem of generation of this message
     * @param ocsRoute The route from the OCS Circuit.
     */
    public OCSConfirmSetupMessage(String id, Time generationTime, OCSRoute ocsRoute) {
        super(id, generationTime);
        this.ocsRoute = ocsRoute;
        this.wavelengthID = -1;
    }

    public OCSRoute getOcsRoute() {
        return ocsRoute;
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
