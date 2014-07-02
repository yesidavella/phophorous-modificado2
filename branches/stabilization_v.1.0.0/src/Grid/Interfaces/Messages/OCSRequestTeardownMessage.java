package Grid.Interfaces.Messages;

import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import simbase.Time;

/**
 * This message is sent to the OCS´s source to request teardown the circuit.
 */
public class OCSRequestTeardownMessage extends GridMessage {

    /**
     * Static variable for making ID's for the jobs.
     */
    public static int teardownReqCounter = 0;
    /**
     * The wavelength of the OCS route.
     */
    private int wavelenght;
    /**
     * The first GirdOutPort to tear down.
     */
    private GridOutPort outport;
    /**
     * The OCS path than have to follow the msg
     */
    private OCSRoute ocsRoute;
    /**
     * The msg than requested the teardown of the ocs
     */
    private MultiCostMessage multiCostMessage;
    /**
     * The ocs than was created and now is evaluated for teardonw
     */
    private OCSRoute ocsExecutedInstruction;
    /**
     * Marks this msg if is re-programed for teardown the ocs
     * ocsExecutedInstruction
     */
    private boolean reSent = false;

    /**
     *
     * @param id The id of this message
     * @param generationTime The time of generation of this message.
     * @param wavelength The wavelength with the ocs starts.
     */
    public OCSRequestTeardownMessage(String id, Time generationTime, int wavelength, MultiCostMessage multiCostMsg) {
        super(id + "-reqTeardownOCS_" + teardownReqCounter++, generationTime);
        this.wavelenght = wavelength;
        this.multiCostMessage = multiCostMsg;
    }

    /**
     * Constructor
     *
     * @param id The id of this message.
     * @param generationTime The time of generation of this message.
     * @param route The ocs route which has to be tear down.
     */
    public OCSRequestTeardownMessage(String id, Time generationTime, OCSRoute ocsRoute) {
        super(id + "-reqTeardownOCS_" + teardownReqCounter++, generationTime);
        this.ocsRoute = ocsRoute;
    }

    /**
     *
     * @param id The id of this message.
     * @param generationTime The time of generation of this message.
     * @param wavelength The wavelength with the ocs starts.
     * @param outport The outport qith the cos starts.
     * @param route The ocs route which has to be tear down.
     */
    public OCSRequestTeardownMessage(String id, Time generationTime, int wavelength, GridOutPort outport, OCSRoute ocsRoute, MultiCostMessage multiCostMsg, OCSRoute ocsExecutedInstruction) {
        super(id + "-reqTeardownOCS_" + teardownReqCounter++, generationTime);
        this.ocsRoute = ocsRoute;
        this.wavelenght = wavelength;
        this.outport = outport;
        this.multiCostMessage = multiCostMsg;
        this.ocsExecutedInstruction = ocsExecutedInstruction;
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

    public OCSRoute getOCSRoute() {
        return ocsRoute;
    }

    public void setOCSRoute(OCSRoute ocsRoute) {
        this.ocsRoute = ocsRoute;
    }

    /**
     * Returns the msg than requested the teardown of the ocs when reach the end
     * of the ocs.
     *
     * @return the multicost msg than requested the teardown of the ocs on its
     * end
     */
    public MultiCostMessage getMultiCostMessage() {
        return multiCostMessage;
    }

    public OCSRoute getOcsExecutedInstruction() {
        return ocsExecutedInstruction;
    }

    public void setReSent(boolean reSent) {
        this.reSent = reSent;
    }

    public boolean getReSent() {
        return reSent;
    }
}
