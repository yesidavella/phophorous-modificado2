/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simbase;

import simbase.Port.SimBaseInPort;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class SimBaseDefaultEntityImpl extends SimBaseEntityImpl{
    


    public SimBaseDefaultEntityImpl(String id, SimBaseSimulator simulator) {
          super(id, simulator);
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) {
        //System.out.println("Message Received from " + inPort.getSource() + m.getId());
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    
    
    

}
