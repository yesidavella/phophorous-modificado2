/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Grid.Port;

import simbase.SimBaseEntity;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class GridHybridOutPort extends GridOutPort{

    /**
     * The wavelength which is used at the beginning of the circuit. (This port can only be the beginning of 
     * an OCS circuit).
     */
    private int OCSCircuitWavelength;
    
    public GridHybridOutPort(String id, SimBaseEntity owner, double speed,double linkSpeed, int maxOCSWavelength,int OCSCircuitWavelength) {
        super(id, owner, speed,linkSpeed, maxOCSWavelength);
        this.OCSCircuitWavelength = OCSCircuitWavelength;
    }
    
    public GridHybridOutPort(GridOutPort port,int OCSCircuitWavelength){
        super(port.getID(),port.getOwner(),port.getSwitchingSpeed(),port.getLinkSpeed(),port.getMaxNumberOfWavelengths());
        this.OCSCircuitWavelength = OCSCircuitWavelength;
        
    }

    public int getOCSCircuitWavelength() {
        return OCSCircuitWavelength;
    }

    public void setOCSCircuitWavelength(int OCSCircuitWavelength) {
        this.OCSCircuitWavelength = OCSCircuitWavelength;
    }
    
    
    

}
