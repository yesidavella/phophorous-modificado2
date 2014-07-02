/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Grid.Utilities;

/**
 *
 * @author Jens Buysse
 */
public class IllegalEdgeException extends Exception {

    /**
     * Creates a new instance of <code>IllegalEdgeException</code> without detail message.
     */
    public IllegalEdgeException() {
        
    }


    /**
     * Constructs an instance of <code>IllegalEdgeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public IllegalEdgeException(String msg) {
        super(msg);
    }
}
