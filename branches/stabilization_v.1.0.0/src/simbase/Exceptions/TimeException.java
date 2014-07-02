/*
 * This exception is thrown when something goes wrong with the timing of the 
 * simulator.
 */

package simbase.Exceptions;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class TimeException extends Exception {

    /**
     * Creates a new instance of <code>TimeException</code> without detail message.
     */
    public TimeException() {
    }


    /**
     * Constructs an instance of <code>TimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TimeException(String msg) {
        super(msg);
    }
}
