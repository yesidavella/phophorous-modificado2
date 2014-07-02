package simbase;


import java.io.Serializable;
import simbase.Port.SimBaseInPort;
import simbase.Port.SimBaseOutPort;
import simbase.Port.SimBasePort;

/**
 * The SimBaseEvent class is used as a wrapper for events, and is used by the
 * SimBaseSimulator class to keep track of events in its event list.
 * 
 * @version 1.0
 */
public class SimBaseEvent implements Comparable, Serializable {

    /**
     * The source
     */
    protected SimBaseOutPort source = null;
    /**
     * The target
     */
    protected SimBaseInPort target = null;
    /**
     * The message
     */
    protected SimBaseMessage message = null;
    /**
     * The time the message is sent
     */
    protected Time time = new Time();

    /**
     * DefaultConstructor, used for testing purposes.
     * 
     */
    public SimBaseEvent() {

    }

    /**
     * Constructor
     * 
     * @param source
     *            the outport through which the message was sent
     * @param message
     *            the message
     * @param time
     *            the time of arrival at the receiving end
     */
    public SimBaseEvent(SimBaseOutPort source, SimBaseMessage message, Time time) {
        this.source = source;
        target = source.getTarget();
        this.message = message;
        this.time = time;
    }

    /**
     * Constructor
     * 
     * @param target
     *            the inport where the message will be received
     * @param message
     *            the message
     * @param time
     *            the time of arrival at the receiving end
     */
    public SimBaseEvent(SimBaseInPort target, SimBaseMessage message, Time time) {
        if (target != null) {
            source = target.getSource();
        }
        this.target = target;
        this.message = message;
        this.time = time;
    }

    public SimBaseEvent(SimBasePort port, SimBaseMessage message, Time time) {
        if (port instanceof SimBaseInPort) {
            if (target != null) {
                source = ((SimBaseInPort)target).getSource();
            }
            this.target = (SimBaseInPort) port;
            this.message = message;
            this.time = time;
        }
        if (port instanceof SimBaseOutPort) {
            this.source = (SimBaseOutPort) port;
            target = ((SimBaseOutPort) port).getTarget();
            this.message = message;
            this.time = time;
        }

    }

    /**
     * Copy constructor
     * 
     * @param s
     *            the original event
     */
    public SimBaseEvent(SimBaseEvent s) {
        this(s.getSource(), s.getMessage(), s.getTime());
    }

    /**
     * Returns the source outport
     * 
     * @return the source outport
     */
    public SimBaseOutPort getSource() {
        return source;
    }

    /**
     * Returns the destination inport
     * 
     * @return the destination inport
     */
    public SimBaseInPort getTarget() {
        return target;
    }

    /**
     * Returns the message
     * 
     * @return the message
     */
    public SimBaseMessage getMessage() {
        return message;
    }

    /**
     * Sets the GridOutPort.
     * @param source The new GridOutPort.
     */
    public void setSource(SimBaseOutPort source) {
        this.source = source;
    }

    /**
     * Sets the new simbaseinport.
     * @param target The new simbaseinport.
     */
    public void setTarget(SimBaseInPort target) {
        this.target = target;
    }
    
    

    /**
     * Returns the arrival time
     * 
     * @return the arrival time
     */
    public Time getTime() {
        return time;
    }

    public int compareTo(Object event) {
        int a = this.time.compareTo(((SimBaseEvent) event).getTime());
        if (a == 0) {
            a = this.message.id.compareTo(((SimBaseEvent) event).getMessage().id);
        }
        return a;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public String toString() {
        return "<time>" + time + "</time>" + "<sender>" + source.getID() + "</sender>" + "<target>" + target.getID() + "</target>" + "<message>" + message + "</message>";
    }

    public void setMessage(SimBaseMessage message) {
        this.message = message;
    }
    
    
}
