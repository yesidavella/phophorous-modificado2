package Grid.Interfaces.Messages;

import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import simbase.Time;

/**
 * This message is sent to the OCS´s source to request teardown the circuit.
 */
public class OCSRequestTeardownMessage extends GridMessage{
    
 /**
     * The wavelength of the OCS route.
     */
    protected int wavelenght;
    /**
     * The first GirdOutPort to tear down.
     */
    protected GridOutPort outport;
    /**
     * The OCS path
     */
    private OCSRoute ocsRoute;

    /**
     * Constructor
     *
     * @param id The id of this message
     * @param generationTime The time of generation of this message.
     * @param source The source of this OCS circuit.
     * @param end The end of the OCS circuit.
     */
    public OCSRequestTeardownMessage(String id, Time generationTime, int wavelength) {
        super(id, generationTime);
        this.wavelenght = wavelength;
    }

    /**
     * Constructor
     *
     * @param id The id of this message.
     * @param generationTime The time of generation of this message.
     * @param route The ocs route which has to be tear down.
     */
    public OCSRequestTeardownMessage(String id, Time generationTime, OCSRoute route) {
        super(id, generationTime);
        this.route = route;
    }

    /**
     * Returns the wavelengths of the circuit that has to be torn down.
     *
     * @return the wavelengths of the circuit that has to be torn down
     */
    public int getWavelenght() {
        return wavelenght;
    }

    /**
     * Sets the wavelength of the circuit that has to be torn down.
     *
     * @param wavelenght The wavelength of the circuit that has to be torn down.
     */
    public void setWavelenght(int wavelenght) {
        this.wavelenght = wavelenght;
    }

    public GridOutPort getOutport() {
        return outport;
    }

    public void setOutport(GridOutPort outport) {
        this.outport = outport;
    }

    public OCSRoute getOcsRoute() {
        return ocsRoute;
    }

    public void setOcsRoute(OCSRoute ocsRoute) {
        this.ocsRoute = ocsRoute;
    }
    
}
