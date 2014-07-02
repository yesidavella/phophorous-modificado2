/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simbase.Stop;

import simbase.*;

/**
 *
 * @author Jens Buysse
 */
public class StopEvent extends SimBaseEvent{

    public StopEvent(StopMessage stopMessage, StopEntity stopEntity) {
        super();
        this.message = stopMessage;
       
    }

    
    
}
