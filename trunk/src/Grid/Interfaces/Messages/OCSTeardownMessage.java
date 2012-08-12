/*
 * This message is send by an enity to perform a break down of a OCS circuit.
 * It contains the wavelength of the circuit which has to be torn down. This 
 * message must be send on the circuit itself so no routing information has to
 * be kept in this message.
 */
package Grid.Interfaces.Messages;

import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OCSTeardownMessage extends GridMessage {

    /**
     * The wavelength of the OCS route.
     */
    protected int wavelenght;
    /**
     * The first GirdOutPort to tear down.
     */
    protected GridOutPort outport;

    /**
     * Constructor
     * @param id The id of this message
     * @param generationTime The time of generation of this message.
     * @param source The source of this OCS circuit.
     * @param end The end of the OCS circuit.
     */
    public OCSTeardownMessage(String id, Time generationTime, int wavelength) {
        super(id, generationTime);
        this.wavelenght= wavelength;
    }

    /**
     * Constructor
     * @param id The id of this message.
     * @param generationTime The time of generation of this message.
     * @param route The ocs route which has to be tear down.
     */
    public OCSTeardownMessage(String id, Time generationTime, OCSRoute route) {
        super(id, generationTime);
        this.route = route;
    }

    /**
     * Returns the wavelengths of the circuit that has to be torn down.
     * @return the wavelengths of the circuit that has to be torn down
     */
    public int getWavelenght() {
        return wavelenght;
    }

    /**
     * Sets the wavelength of the circuit that has to be torn down.
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
}
