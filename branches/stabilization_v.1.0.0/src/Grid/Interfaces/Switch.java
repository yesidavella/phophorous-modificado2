/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Interfaces;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public abstract class Switch extends Entity {

    public Switch(String id, GridSimulator gridSim) {
        super(id, gridSim);
    }

    /**
     * Sets the handling delay.
     * 
     * @param t
     *            the handling delay
     */
    public abstract void setHandleDelay(Time t);

    /**
     * Returns the handling delay
     * 
     * @return the handling delay
     */
    public abstract Time getHandleDelay();

    /**
     * Sets the conversion time from the optical to the electrical domain
     * 
     * @param t
     *            the conversion time from the optical to the electrical domain
     */
    public abstract void setConversionTimeOE(Time t);

    /**
     * Returns the conversion time from the optical to the electrical domain
     * 
     * @return the conversion time from the optical to the electrical domain
     */
    public abstract Time getConversionTimeOE();

    /**
     * Sets the conversion time from the electrical to the optical domain
     * 
     * @param t
     *            the conversion time from the electrical to the optical domain
     */
    public abstract void setConversionTimeEO(Time t);

    /**
     * Returns the conversion time from the electrical to the optical domain
     * 
     * @return the conversion time from the electrical to the optical domain
     */
    public abstract Time getConversionTimeEO();

    /***
     * Sets whether wavelength conversion is supported.
     * @param convert
     *            specifies whether wavelength is supported
     */
    public abstract void setWavelengthConversion(boolean convert);

    /**
     * Returns whether wavelength conversion is supported
     * 
     * @return whether wavelength conversion is supported
     */
    public abstract boolean getWavelengthConversion();

    /**
     * Drops the message, because some kind of anomaly rose up.
     */
    protected abstract void dropMessage(GridMessage m);

    @Override
    public boolean supportSwitching() {
        return true;
    }
}
