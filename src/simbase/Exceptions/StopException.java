/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simbase.Exceptions;

/**
 *
 * @author Jens Buysse
 */
public class StopException extends Exception {

    /**
     * Creates a new instance of <code>StopException</code> without detail message.
     */
    public StopException() {
    }


    /**
     * Constructs an instance of <code>StopException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public StopException(String msg) {
        super(msg);
    }
}
