/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Interfaces.Switches;

import Grid.GridSimulator;


/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public abstract class OCSSwitch extends AbstractSwitch {

    public OCSSwitch(String id, GridSimulator simulator) {
        super(id, simulator);
    }

    @Override
    public boolean supportsOBS() {
        return false;
    }

    @Override
    public boolean supportsOCS() {
        return true;
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
