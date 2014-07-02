/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator11.ErlangReducedModel;

import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Utilities.HopToHopLinks;
import simbase.SimulationInstance;

/**
 *
 * @author Jens Buysse
 */
public class LittleTest {

    public String[] switches = {"Dublin", "Glasgow", "Amsterdam", "London"};
    /**
     * The simulator instance
     */
    private SimulationInstance simInstance;
    /**
     * The simulator itself
     */
    private GridSimulator simulator;

    public LittleTest() {

        simInstance = new GridSimulation("configFiles\\Reduced.cfg");
        simulator = new GridSimulator();
        simInstance.setSimulator(simulator);
        HopToHopLinks creator = new HopToHopLinks(simulator);

        //Creation of resourcebroker
        ServiceNode broker = Grid.Utilities.Util.createOBSServiceNode("broker", simulator);

        //Creation of the network
        //Switchcreation
        for (int i = 0; i < this.switches.length; i++) {
            Switch sw = Grid.Utilities.Util.createOBSSwitch(switches[i] + "-switch", simulator, true);
            ClientNode client = Grid.Utilities.Util.createOBSClient(switches[i] + "-client", simulator, broker);
            Grid.Utilities.Util.createBiDirectionalLink(sw, client);
        }

        creator.addBidirectionLinkFromString("Dublin-switch", "Glasgow-switch");
        creator.addBidirectionLinkFromString("Glasgow-switch", "Amsterdam-switch");
        creator.addBidirectionLinkFromString("Amsterdam-switch", "London-switch");
        creator.addBidirectionLinkFromString("London-switch", "Dublin-switch");

        creator.addBidirectionLinkFromString("Dublin-switch", "broker");

        ResourceNode amsterdam = Grid.Utilities.Util.createOBSResource("Amsterdam-resource", simulator);
        ResourceNode glasgow = Grid.Utilities.Util.createOBSResource("Glasgow-resource", simulator);
        ResourceNode dublin = Grid.Utilities.Util.createOBSResource("Dublin-resource", simulator);
        ResourceNode london = Grid.Utilities.Util.createOBSResource("London-resource", simulator);
        creator.addBidirectionLinkFromString("Amsterdam-resource", "Amsterdam-switch");
        creator.addBidirectionLinkFromString("Dublin-resource", "Dublin-switch");
        creator.addBidirectionLinkFromString("Glasgow-resource", "Glasgow-switch");
        creator.addBidirectionLinkFromString("London-resource", "London-switch");

        simulator.route();
        simulator.initEntities();
        
        
        //Only one resource
        amsterdam.addServiceNode(broker);
        london.addServiceNode(broker);
        glasgow.addServiceNode(broker);
        dublin.addServiceNode(broker);
        


        simInstance.run();


    }

    public static void main(String[] args) {
        LittleTest test = new LittleTest();

    }
}
