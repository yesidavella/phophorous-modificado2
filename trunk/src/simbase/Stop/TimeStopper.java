/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simbase.Stop;


import Grid.Utilities.Config;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.SimBaseSimulator;
import simbase.SimulationInstance;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class TimeStopper extends StopEntity{

    private double averageClients = 0;
    
    public TimeStopper(String id,SimBaseSimulator simulator, Time offset) {
        super(id, simulator,offset);
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);
        System.gc();
       // simulator.putLogImmediately(currentTime, simulator.getMasterClock().getTime() / SimulationInstance.configuration.
         //       getDoubleProperty(Config.ConfigEnum.simulationTime)* 100 + "%");
    }
    
    

    @Override
    public boolean checkCondition() {
        if(simulator.getMasterClock().getTime() >= 
                SimulationInstance.configuration.getDoubleProperty(Config.ConfigEnum.simulationTime)){
            return true;
        }
        else {

            return false;
        }
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public double getAverageClients() {
        return averageClients;
    }
    

}
