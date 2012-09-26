/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator11.Hybrid.HybridTestCase.LoadCircuit;

import Distributions.DDNegExp;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Outputter;
import Grid.Utilities.Config;
import simbase.SimulationInstance;
import simbase.Time;


/**
 *
 * @author Eothein
 */
public class LoadCircuits {

    private SimulationInstance simInstance;
    private GridSimulator simulator;
    private Switch switch1;
    private Switch switch2;
    private Switch switch3;
    private Switch switch4;
    private Switch center;
    private ClientNode client1;
    private ClientNode client2;
    private ResourceNode resource1;
    private ResourceNode resource2;
    private ServiceNode broker;

    public LoadCircuits() {
        simInstance = new GridSimulation("D:\\Desktop\\Source_simulation_framework\\src\\configFiles\\loadCircuits.cfg");
        simulator = new GridSimulator();
        simInstance.setSimulator(simulator);

        //Node Creation
        switch1 = Grid.Utilities.Util.createHybridSwitch("SWITCH1", simulator);
        switch2 = Grid.Utilities.Util.createHybridSwitch("SWITCH2", simulator);
        switch3 = Grid.Utilities.Util.createHybridSwitch("SWITCH3", simulator);
        switch4 = Grid.Utilities.Util.createHybridSwitch("SWITCH4", simulator);
        center = Grid.Utilities.Util.createHybridSwitch("CENTER", simulator);
        broker = Grid.Utilities.Util.createHybridServiceNode("BROKER", simulator);
        client1 = Grid.Utilities.Util.createHybridClient("CLIENT1", simulator, broker);
        client2 = Grid.Utilities.Util.createHybridClient("CLIENT2", simulator, broker);
        resource1 = Grid.Utilities.Util.createHyridResourceNode("RESOURCE1", simulator);
        resource2 = Grid.Utilities.Util.createHyridResourceNode("RESOURCE2", simulator);
        
        //Service node registration
        resource1.addServiceNode(broker);
        resource2.addServiceNode(broker);

        //Network creation
        Grid.Utilities.Util.createBiDirectionalLink(client1, switch1);
        Grid.Utilities.Util.createBiDirectionalLink(client2, switch2);
        Grid.Utilities.Util.createBiDirectionalLink(switch3, resource1);
        Grid.Utilities.Util.createBiDirectionalLink(switch4, resource2);
        Grid.Utilities.Util.createBiDirectionalLink(switch1, center);
        Grid.Utilities.Util.createBiDirectionalLink(switch2, center);
        Grid.Utilities.Util.createBiDirectionalLink(switch3, center);
        Grid.Utilities.Util.createBiDirectionalLink(switch4, center);
        
        
        //Parameter selection
        double dataSize = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.defaultDataSize);
        double switchingSpeed = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.switchingSpeed);
        double mu = switchingSpeed / dataSize;
        int wavelengths = simInstance.configuration.getIntProperty(Config.ConfigEnum.defaultWavelengths);
        double lamda1 = 10*mu*wavelengths;
        double lamda2 = 0.5*mu*wavelengths;
        
        client1.getState().setJobInterArrival(new DDNegExp(simulator, 1/lamda1));
        client2.getState().setJobInterArrival(new DDNegExp(simulator, 1/lamda2));
        
        //Routing
       // simulator.route();
       simulator.initEntities();
        
        //Creation of the circuits
        
        //Grid.Utilities.Util.createOCSCircuit(client1, resource1, simulator, false);
        //Grid.Utilities.Util.createOCSCircuit(client1, resource2, simulator, false);
        
        //Run the simulation
        simInstance.run();

        //Output 
        Outputter output = new Outputter(simulator);
        System.out.println(lamda1 + " "+ mu);
        System.out.println(lamda2 + " "+ mu);
        
        output.printClient(client1);
        output.printClient(client2);
        output.printResource(resource1);
        output.printResource(resource2);
        output.printSwitch(center);
        output.printSwitch(switch1);
        output.printSwitch(switch2);
        output.printSwitch(switch3);
        output.printSwitch(switch4);
    
    }
    
    public static void main(String[] args){
        new LoadCircuits();
    }
}
