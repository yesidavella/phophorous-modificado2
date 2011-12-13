/*
 * This is a helper class to model OCS mappings.
 */
package Grid.Nodes;

import simbase.Port.SimBasePort;

/**
 *
 * @author Jens Buysse
 */
public class LinkWavelengthPair implements Comparable {

    /**
     * The Simbaseport from this pair.
     */
    protected SimBasePort port;
    /**
     * The wavelength
     */
    protected int wavelength;

    /**
     * The constructor
     * @param port The port of this pair
     * @param wavelength The wavelength of this pair
     */
    public LinkWavelengthPair(SimBasePort port, int wavelength) {
        this.port = port;
        this.wavelength = wavelength;
    }

    public SimBasePort getPort() {
        return port;
    }

    public void setPort(SimBasePort port) {
        this.port = port;
    }

    public int getWavelength() {
        return wavelength;
    }

    public void setWavelength(int wavelength) {
        this.wavelength = wavelength;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LinkWavelengthPair) {
            LinkWavelengthPair pair = (LinkWavelengthPair) obj;
            if (port.equals(pair.getPort()) && wavelength == pair.getWavelength()) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Cannot equal a Linkwavelengthpair with "+ obj.getClass().getName());
        }
    }
    
    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer(port.getID());
        buffer.append("@");
        buffer.append(wavelength);
        return buffer.toString();
    }

    /**
     * Compares two linkwavelength pairs with each other.
     * @param o The object (obviously a linkwavelengthpair
     * @return -1 is this is smaller, +1 if this is greater 0 if equal
     */
    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }
}
    
    

