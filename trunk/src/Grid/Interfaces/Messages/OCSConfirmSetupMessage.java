package Grid.Interfaces.Messages;

import Grid.OCS.OCSRoute;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OCSConfirmSetupMessage extends GridMessage{

    /**
     * The route of the OCS circuit which has been setup.
     */
    private OCSRoute ocsRoute;
    
    /**
     * Constructor
     * @param id The id of the message
     * @param generationTime The tiem of generation of this message
     * @param ocsRoute The route from the OCS Circuit.
     */
    public OCSConfirmSetupMessage(String id, Time generationTime,OCSRoute ocsRoute) {
        super(id, generationTime);
        this.ocsRoute = ocsRoute;
        this.wavelengthID = -1;
    }

    public OCSRoute getOcsRoute() {
        return ocsRoute;
    }

}
