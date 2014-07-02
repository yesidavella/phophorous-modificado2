/*
 * This class is the base class for simulations. If any additions should be added
 * to the Simulationinstance, then it should come here
 */

package Grid;


import Grid.Utilities.Config;
import simbase.SimulationInstance;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class GridSimulation extends SimulationInstance{



    /**
     * Constructs a new Gridsimulation.
     * @param confiFileName The file containting the configurationparameters.
     */
    public GridSimulation(String confiFile) {
        super();
//        configuration = new Config(confiFileName);
        configuration = new Config();
        
    }
    
    

    @Override
    protected void initEntities() {  
        super.initEntities();
    }
    
    

}
