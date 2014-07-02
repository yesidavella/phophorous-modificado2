/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main.Factories.Hybrid;

import Grid.GridSimulator;
import Grid.Interfaces.Switch;
import Grid.Routing.GridVertex;
import org.apache.commons.collections15.Factory;

/**
 *
 * @author Eothein
 */
public class HybridSwitchFactory implements Factory<GridVertex> {

    private static int switchNr = -1;
    private GridSimulator simulator;

    public HybridSwitchFactory(GridSimulator simulator) {
        this.simulator = simulator;
    }

    public GridVertex create() {
        switchNr++;
        Switch switchNode = Grid.Utilities.Util.createHybridSwitch("Switch_" + switchNr, simulator);
        GridVertex switchVertex = new GridVertex(switchNode);
        return switchVertex;
    }
}
