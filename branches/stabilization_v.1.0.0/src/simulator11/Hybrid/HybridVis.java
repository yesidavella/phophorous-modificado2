/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator11.Hybrid;

import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import javax.swing.JFrame;
import simbase.SimulationInstance;


/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class HybridVis {

    private Switch[] hops;
    private int nrOfHops = 5;
    private GridSimulator simulator;
    private SimulationInstance simInstance;
    private ResourceNode resource;
    private ServiceNode serviceNode;
    private ClientNode client;

    public HybridVis() {
                simInstance = new GridSimulation("configFiles/hybrid.cfg");
        simulator = new GridSimulator();
        simInstance.setSimulator(simulator);

        hops = new Switch[nrOfHops];
        for (int i = 0; i < nrOfHops; i++) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Switch");
            buffer.append(i);
            Switch sw = Grid.Utilities.Util.createHybridSwitch(buffer.toString(), simulator);
            hops[i] = sw;
        }
        //Create a ring network
        Grid.Utilities.Util.createBiDirectionalLink(hops[nrOfHops - 1], hops[0]);
        for (int i = 0; i < nrOfHops - 1; i++) {
            Grid.Utilities.Util.createBiDirectionalLink(hops[i], hops[i + 1]);
        }
        resource = Grid.Utilities.Util.createHyridResourceNode("Resource", simulator);
        serviceNode = Grid.Utilities.Util.createHybridServiceNode("Service", simulator);
        client = Grid.Utilities.Util.createHybridClient("Client", simulator, serviceNode);

        Grid.Utilities.Util.createBiDirectionalLink(hops[0], client);
        Grid.Utilities.Util.createBiDirectionalLink(hops[2], resource);
        Grid.Utilities.Util.createBiDirectionalLink(hops[4], serviceNode);


        resource.addServiceNode(serviceNode);
        simulator.route();
        
        
        Grid.Utilities.Util.createOCSCircuit(client, resource, simulator,true);
        Grid.Utilities.Util.createOCSCircuit(resource, client, simulator,true);
        Grid.Utilities.Util.createOCSCircuit(serviceNode, client, simulator,true);
        Grid.Utilities.Util.createOCSCircuit(client, serviceNode, simulator,true);

        simulator.initEntities();
        simInstance.run();
        

    }
    
    public static void main (String[] args){
        new HybridVis();
    }
}
