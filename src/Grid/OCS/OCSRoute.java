/*
 * The normal route object, added with a field containing the wavelength 
 * for the OCS-circuit.
 */
package Grid.OCS;

import Grid.Entity;
import Grid.Route;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OCSRoute extends Route {

    /**
     * The last wavelength which has been used to send on.
     */
    private int wavelength;
    /**
     * This is the id of the jobMsg that request to create a OCS.
     */
    private String idJobMsgRequestOCS;

    /**
     * Constructor
     *
     * @param source The source of this ocs-circuit.
     * @param destination The destination of this circuit.
     * @param wavelength The wavelenght for which this circuit is reserved.
     */
    public OCSRoute(Entity source, Entity destination, int wavelength) {
        super(source, destination);
        this.wavelength = wavelength;
    }

    public int getWavelength() {
        return wavelength;
    }

    public void setWavelength(int wavelength) {
        this.wavelength = wavelength;
    }

    /**
     * Will find the following hop on the path for this node.
     *
     * @param node The node
     * @return
     */
    public Entity findNextHop(Entity node) {

        int index = indexOf(node);
        if (index == size() - 1) {
            return null;
        } else {
            Entity newHopOnPath = get(index + 1);
            return newHopOnPath;
        }
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
     * @param idJobMsgRequestOCS
     */
    public void setIdJobMsgRequestOCS(String idJobMsgRequestOCS) {
        this.idJobMsgRequestOCS = idJobMsgRequestOCS;
    }
}
