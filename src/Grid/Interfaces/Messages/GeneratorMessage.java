/*
 * This message is send from an entity to another (or itself) to start generating
 * messages.
 */

package Grid.Interfaces.Messages;

import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class GeneratorMessage extends GridMessage {

    /**
     * Constructor
     */
    public GeneratorMessage(String id,Time generationTime) {
      super("GeneratorMessage for " + id, generationTime);
    }
    
    /**
     * toString method
     * @return A Sring represenation of this message.
     */
    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer("GeneratorMessage for : ");
        buffer.append(id);
        return buffer.toString();
    }
    
    

}
