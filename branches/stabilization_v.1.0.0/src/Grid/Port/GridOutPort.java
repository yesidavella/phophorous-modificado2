/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Port;

import java.util.ArrayList;
import java.util.List;
import simbase.Port.SimBaseOutPort;
import simbase.SimBaseEntity;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class GridOutPort extends SimBaseOutPort {

    /**
     * The list with used wavelengths for OCS circuits.
     */
    private List<Integer> usedOCSwavelengths;
    /**
     * The maximum number of wavelengths this port containts.
     */
    protected int maxNumberOfWavelengths;
    /**
     * The switchSpeed of this link neededToSwitch.
     */
    protected double switchSpeed;
    /**
     * The time needed to reach the other end of the link.
     */
    protected double linkSpeed;

    /**
     * Constructor
     * @param id
     * @param owner
     */
    public GridOutPort(String id, SimBaseEntity owner, double switchingSpeed, double linkSpeed, int maxWavelengths) {
        super(id, owner);
        this.switchSpeed = switchingSpeed;
        usedOCSwavelengths = new ArrayList<Integer>(maxWavelengths);
        this.maxNumberOfWavelengths = maxWavelengths;
        this.linkSpeed = linkSpeed;
    }

    /**
     * Returns the switchSpeed of this link in size units per time unit.
     * @return The switchSpeed of this link.
     */
    public double getSwitchingSpeed() {
        return switchSpeed;
    }

    /**
     * Sets the switchSpeed of this link in units per time unit.
     * @param switchSpeed The switchSpeed of this link.
     */
    public void setSwitchingSpeed(double switchingSpeed) {
        this.switchSpeed = switchingSpeed;
    }

    /**
     * Adds this wavelength to the list of used wavelengths by circuits
     * @param wave The wave which has to be added
     * @return True if adding worked, false if not.
     */
    public boolean addWavelength(int wave) {
        return usedOCSwavelengths.add(new Integer(wave));
    }

    /**
     * Removes the wavelength specified from the list of used
     * wavelengths by circuits.
     * @param wave The wave to remove.
     * @return True if removing worked, false if not.
     */
    public boolean removeWavelength(int wave) {
        Integer w = new Integer(wave);
        return usedOCSwavelengths.remove(w);
    }

    /**
     * Returns true if this wavelengths is used by a circuit on this port.
     * @param wave The wave to check.
     * @return True if it is already used, false if not.
     */
    public boolean isWaveUsedInCircuit(int wave) {
        if (wave >= 0) {
            Integer w = new Integer(wave);
            return usedOCSwavelengths.contains(w);
        } else {
            throw new IllegalArgumentException("Cannot check fo a wavelength <0");
        }

    }

    /**
     * Return the maximum number of wavelenghts this outport supports.
     * @return The maximum number of wavelenghts this ouport supports.
     */
    public int getMaxNumberOfWavelengths() {
        return maxNumberOfWavelengths;
    }

    public void setMaxNumberOfWavelengths(int maxNumberOfWavelengths) {
        this.maxNumberOfWavelengths = maxNumberOfWavelengths;
        usedOCSwavelengths = new ArrayList<Integer>(maxNumberOfWavelengths);
        
    }

    /**
     * Return a new free wavelengths which can be used for the setup of a 
     * OCS circuit.
     * @return A new wavelengths, usuable for a new OCS circuit.
     */
    public int getNexFreeWavelength() {
        int wavelength = -1;
        for (int i = 0; i < maxNumberOfWavelengths; i++) {
            if (isWaveUsedInCircuit(i)) {
                continue;
            } else {
                return i;
            }
        }
        return wavelength;
    }

    /**
     * Get the time needed to reach the other end of the link.
     * @return The link speed.
     */
    public double getLinkSpeed() {
        return linkSpeed;
    }

    /**
     * Sets the time needed to reach the other end of the link.
     * @param linkSpeed The new link speed.
     */
    public void setLinkSpeed(double linkSpeed) {
        this.linkSpeed = linkSpeed;
    }
}
