/*
 * Provides an abstract skeleton for switches.
 */
package Grid.Interfaces.Switches;

import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.JobResultMessage;
import Grid.Interfaces.Switch;
import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public abstract class AbstractSwitch extends  Switch {

    /**
     * Constructor.
     * @param id The id of this switch.
     * @param simulator The SimBaseSimulator instance.
     */
    public AbstractSwitch(String id, GridSimulator simulator) {
        super(id, simulator);
    }
    /**
     * The handling delay every message suffers from passing through the switch
     */
    protected Time handleDelay;
    /**
     * Conversion time from the optical to the electrical domain (used for
     * communication with {@link GridEndEntity} objects
     */
    protected Time conversionTimeOE;
    /**
     * Conversion time from the electrical to the optical domain (used for
     * communication with {@link GridEndEntity} objects
     */
    protected Time conversionTimeEO;
    /**
     * Does the switch support wavelength conversion?
     */
    protected boolean wavelengthConversion=true;

    /**
     * Sets the handling delay
     * 
     * @param t
     *            the handling delay
     */
    public void setHandleDelay(Time t) {
        handleDelay = t;
    }

    /**
     * Returns the handling delay
     * 
     * @return the handling delay
     */
    public Time getHandleDelay() {
        return handleDelay;
    }

    /**
     * Sets the conversion time from the optical to the electrical domain
     * 
     * @param t
     *            the conversion time from the optical to the electrical domain
     */
    public void setConversionTimeOE(Time t) {
        this.conversionTimeOE.setTime(t);
    }

    /**
     * Returns the conversion time from the optical to the electrical domain
     * 
     * @return the conversion time from the optical to the electrical domain
     */
    public Time getConversionTimeOE() {
        return conversionTimeOE;
    }

    /**
     * Sets the conversion time from the electrical to the optical domain
     * 
     * @param t
     *            the conversion time from the electrical to the optical domain
     */
    public void setConversionTimeEO(Time t) {
        this.conversionTimeEO.setTime(t);
    }

    /**
     * Returns the conversion time from the electrical to the optical domain
     * 
     * @return the conversion time from the electrical to the optical domain
     */
    public Time getConversionTimeEO() {
        return conversionTimeEO;
    }

    /**
     * Sets whether wavelength conversion is supported
     * 
     * @param convert
     *            specifies whether wavelength is supported
     */
    public void setWavelengthConversion(boolean convert) {
        this.wavelengthConversion = convert;
    }

    /**
     * Returns whether wavelength conversion is supported
     * 
     * @return whether wavelength conversion is supported
     */
    public boolean getWavelengthConversion() {
        return wavelengthConversion;
    }

    /**
     * The message gets dropped. In case of a JobMessage or JobResultmessage 
     * stats get updated. 
     */
    protected void dropMessage(GridMessage m) {
        m.dropMessage();
        simulator.putLog(currentTime, "FAIL: "+this.getId()+
                    " dropped a message : "+m.getId(),Logger.RED,m.getSize(),m.getWavelengthID());
        simulator.addStat(this, Stat.SWITCH_MESSAGE_DROPPED);
        if(m instanceof JobMessage){
            simulator.addStat(this, Stat.SWITCH_JOBMESSAGE_DROPPED);    
        }else if (m instanceof JobResultMessage){
            simulator.addStat(this, Stat.SWITCH_JOBRESULTMESSAGE_DROPPED);
            
        }
        else if(m instanceof Grid.Interfaces.Messages.JobRequestMessage){
            
              simulator.addStat(this, Stat.SWITCH_REQ_MESSAGE_DROPPED);
//            System.out.println(" drop "+m+" clas "+m.getClass() );
            
        }
            
        
    }
}
