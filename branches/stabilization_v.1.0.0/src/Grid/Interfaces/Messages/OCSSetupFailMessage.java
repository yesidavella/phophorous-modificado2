/*
 * This message is to be send when a certain place in the route of an OCSSetupMessage
 * the circuit fails to setup. All the other hops which have reserved wavelengts and links
 * have to undo all the changes done, because the OCS circuit cannot be set up. 
 * This message is then being send through the control plane.
 */
package Grid.Interfaces.Messages;

import Grid.OCS.OCSRoute;
import simbase.Time;

/**
 *
 * @author jens
 */
public class OCSSetupFailMessage extends GridMessage {

    /**
     * The wavelength of the circuit.
     */
    private int wavelength;
    
    /**
     * The route of the OCS path.
     */
     private OCSRoute ocsRoute;
    
    /**
     * Constructor
     * @param id The id of this message
     * @param generationTime The time this message was generated.
     */
    public OCSSetupFailMessage(String id, Time generationTime, int wavelength, OCSRoute ocsRoute) {
        super(id, generationTime);
        this.wavelength = wavelength;
        this.wavelengthID = -1;
        this.ocsRoute = ocsRoute;
    }

        

    /**
     * Return the wavelenght of the circuit that has to be torn down.
     * @return the circuit wavelength.
     */
    public int getWavelength() {
        return wavelength;
    }

    /**
     * Sets the wavelength of the circuit that has to be torn down.
     * @param wavelength The wavelength of the circuit that has to be torn down.
     */
    public void setWavelength(int wavelength) {
        this.wavelength = wavelength;
    }

    /**
     * Return the route of the OCS Circuit.
     * @return the route of the OCS Circuit
     */
    public OCSRoute getOcsRoute() {
        return ocsRoute;
    }

    public void setOcsRoute(OCSRoute ocsRoute) {
        this.ocsRoute = ocsRoute;
    }
    
    
    
}
