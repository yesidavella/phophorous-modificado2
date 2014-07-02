/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Utilities;

import Grid.Entity;
import Grid.GridSimulator;

/**
 *
 * @author Jens Buysse
 */
public class HopToHopLinks {

    /**
     * The simulator.
     */
    private GridSimulator simulator;

    /**
     * Constructor
     */
    public HopToHopLinks(GridSimulator simulator) {
        this.simulator = simulator;
    }

    public void addBidirectionLinkFromString(String hop1, String hop2) {

        Entity ent1 = (Entity) simulator.getEntityWithId(hop1);
        Entity ent2 = (Entity) simulator.getEntityWithId(hop2);
        if (ent1 == null) {
            throw new IllegalArgumentException(hop1 + " does not exist yet");
        }
        if (ent2 == null) {
            throw new IllegalArgumentException(hop2 + " does not exist yet");
        }
        Grid.Utilities.Util.createBiDirectionalLink(ent1, ent2);
        }

    

    public void addOneDirectionLinkFromString(String hop1, String hop2) {
        try{
        Entity ent1 = (Entity) simulator.getEntityWithId(hop1);
        Entity ent2 = (Entity) simulator.getEntityWithId(hop2);
        if (ent1 == null) {
            throw new IllegalArgumentException(hop1 + " does not exist yet");
        }
        if (ent2 == null) {
            throw new IllegalArgumentException(hop2 + " does not exist yet");
        }
        Grid.Utilities.Util.createLink(ent1, ent2);
        }catch(IllegalEdgeException e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
