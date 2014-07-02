/**
 * The abstract class GridMessage is an extension of the SimBaseMessage, which
 * is grid-aware. It has a notion of the source and destination nodes of the
 * actual message, and keeps track of the route followed.
 *
 * @version 2.0
 */
package Grid.Interfaces.Messages;

import Grid.Entity;
import Grid.Nodes.Hybrid.Parallel.HybridSwitchImpl;
import Grid.OCS.OCSRoute;
import Grid.Route;
import Grid.Sender.Hybrid.Parallel.HybridSwitchSender;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public abstract class GridMessage extends SimBaseMessage {

    protected boolean reSent = false;
    protected SimBaseInPort inportInWait;
    protected HybridSwitchSender hybridSwitchSenderInWait;
    protected double assigned_b = -1;
    /**
     * The time the job has been generated
     */
    protected Time generationTime = null;
    /**
     * The maximal sustainable delay
     */
    protected double maxDelay = 1000;
    /**
     * Source of the message
     */
    protected Entity source = null;
    /**
     * Destination of the message
     */
    protected Entity destination = null;
    /**
     * Route followed by the message
     */
    protected Route route = null;
    /**
     * Message size in bits
     */
    protected double size = 0;
    /**
     * The wavelength this message uses
     */
    protected int wavelengthID = 0;
    /**
     * Flag to see which kind of message this is. OBS/OCS
     */
    protected MessageType typeOfMessage = GridMessage.MessageType.OBSMESSAGE;
    /**
     * Is this message dropped?
     */
    protected boolean dropped = false;
    /**
     * Offset between header and payload. This is > 0 for OBSMessage and this =0
     * if it is an OCS message. Unit is second.
     */
    protected double offSet;
    /**
     * My traveling route
     */
    private OCSRoute ocsRoute;

    public OCSRoute getOcsRoute() {
        return ocsRoute;
    }
    
    public void setOCSRoute(OCSRoute ocsRoute) {
        this.ocsRoute = ocsRoute;
    }
    

    /**
     * Type which shows which kind of message this is.
     */
    public enum MessageType {

        OCSMESSAGE, OBSMESSAGE
    }
    protected HybridSwitchImpl firstSwitch;
    protected int firstWaveLengthID;

    /**
     * Constructor.
     *
     * @param id message ID
     */
    public GridMessage(String id, Time generationTime) {
        super(id);
        this.generationTime = generationTime;
    }

    public Time getGenerationTime() {
        return generationTime;
    }

    public void setGenerationTime(Time generationTime) {
        this.generationTime = generationTime;
    }

    public double getOffSet() {
        return offSet;
    }

    public void setOffSet(double offSet) {
        this.offSet = offSet;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    /**
     * Returns source of the message
     *
     * @return source of the message
     */
    public Entity getSource() {
        return source;
    }

    /**
     * Returns destination of the message
     *
     * @return destination of the message
     */
    public Entity getDestination() {
        return destination;
    }

    /**
     * Returns the message size
     *
     * @return the message size
     */
    public double getSize() {
        return size;
    }

    /**
     * Sets the messsage source
     *
     * @param source the message source
     */
    public void setSource(Entity source) {
        this.source = source;
        if (route == null) {
            route = new Route(source, null);
        } else {
            route.setSource(source);
        }
    }

    /**
     * Sets the message destination
     *
     * @param destination the message destination
     */
    public void setDestination(Entity destination) {
        this.destination = destination;
        if (route == null) {
            route = new Route(null, destination);
        } else {
            route.setDestination(destination);
        }
    }

    /**
     * Sets the message size
     *
     * @param size the message size
     * @exception IllegalargumentException the argument cannot be smaller then
     * zero
     */
    public void setSize(double size) {
        if (size >= 0) {
            this.size = size;
        } else {
            throw new IllegalArgumentException("Size of message is negative");
        }
    }

    /**
     * Initiates a new Route object with given source and destination nodes
     */
    public void initRoute() {
        route = new Route(source, destination);
    }

    /**
     * Adds a hop to the Route
     *
     * @param hop the next hop
     */
    public void addHop(Entity hop) {
        if (route != null) {
            route.addHop(hop);
        }
    }

    /**
     * Returns the previous hop on the path
     *
     * @return the previous hop on the path
     */
    public Entity getPreviousHop() {
        return route.getLastHopAddedToPath();
    }

    /**
     * Returns the wavelength ID the message is traveling on
     *
     * @return the wavelength ID
     */
    public int getWavelengthID() {
        return wavelengthID;
    }

    /**
     * Sets the wavelength ID the message is traveling on
     *
     * @param wavelengthID the wavelength ID
     */
    public void setWavelengthID(int wavelengthID) {
        this.wavelengthID = wavelengthID;
    }

    /**
     * Sets the jobs execution deadline
     *
     * @param maxDelay job deadline
     */
    public void setMaxDelay(double maxDelay) {
        this.maxDelay = maxDelay;
    }

    /**
     * Returns the jobs execution deadline
     *
     * @return job deadline
     */
    public double getMaxDelay() {
        return maxDelay;
    }

    /**
     * Calculates when the job will need to be executed, taking into account the
     * time needed to return the results to the client, and the deadline
     *
     * @return the actual execution deadline
     */
    public Time getMaxEndTime() {
        // returns maximum (absolute) time when job calculation
        // should be finished, based upon generationtime, the
        // travel time (from path, 2x) and the maxdelay
        Time time = new Time(generationTime);
        time.addTime(maxDelay);
        return time;
    }

    /**
     * Returns which type of message this is.
     *
     * @return The type of message this is.
     */
    public MessageType getTypeOfMessage() {
        return typeOfMessage;
    }

    /**
     * Sets the type of message (OCS/OBS)
     *
     * @param typeOfMessage The message type of this message.
     */
    public void setTypeOfMessage(MessageType typeOfMessage) {
        this.typeOfMessage = typeOfMessage;
    }

    public boolean isDropped() {
        return dropped;
    }

    public void dropMessage() {
        this.dropped = true;
    }

    public double getAssigned_b() {
        return assigned_b;
    }

    public void setAssigned_b(double assigned_b) {
        this.assigned_b = assigned_b;
    }

    public boolean isReSent() {
        return reSent;
    }

    public void setReSent(boolean reSent) {
        this.reSent = reSent;
    }

    public SimBaseInPort getInportInWait() {
        return inportInWait;
    }

    public void setInportInWait(SimBaseInPort inportInWait) {
        this.inportInWait = inportInWait;
    }

    public HybridSwitchSender getHybridSwitchSenderInWait() {
        return hybridSwitchSenderInWait;
    }

    public void setHybridSwitchSenderInWait(HybridSwitchSender hybridSwitchSenderInWait) {
        this.hybridSwitchSenderInWait = hybridSwitchSenderInWait;
    }

    public HybridSwitchImpl getFirstSwitch() {
        return firstSwitch;
    }

    public void setFirstSwitch(HybridSwitchImpl firstSwitch) {
        this.firstSwitch = firstSwitch;
    }

    public int getFirstWaveLengthID() {
        return firstWaveLengthID;
    }

    public void setFirstWaveLengthID(int firstWaveLengthID) {
        this.firstWaveLengthID = firstWaveLengthID;
    }
}
