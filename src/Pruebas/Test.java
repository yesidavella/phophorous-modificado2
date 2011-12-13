/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Pruebas;

import Distributions.DDNegExp;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Outputter;
import Grid.Utilities.Config;
/**
 *
 * @author Semana
 */
public class Test 
{
    public static void main( String args[] )
    {
        GridSimulation simInstance = new GridSimulation("D:\\Desktop\\Source_simulation_framework\\src\\configFiles\\loadCircuits.cfg");

        GridSimulator simulator = new GridSimulator();

        Switch switch1 = Grid.Utilities.Util.createHybridSwitch("SWITCH1", simulator);
        ServiceNode broker = Grid.Utilities.Util.createHybridServiceNode("BROKER", simulator);
        ResourceNode resource1 = Grid.Utilities.Util.createHyridResourceNode("RESOURCE1", simulator);
        ClientNode client1 = Grid.Utilities.Util.createHybridClient("CLIENT1", simulator, broker);

        resource1.addServiceNode(broker);

         
       
        Grid.Utilities.Util.createBiDirectionalLink(switch1, client1);
        Grid.Utilities.Util.createBiDirectionalLink(client1, switch1);
        Grid.Utilities.Util.createBiDirectionalLink(switch1, broker);
        Grid.Utilities.Util.createBiDirectionalLink(switch1, resource1);

//         Grid.Utilities.Util.createOCSCircuit(client1, switch1, simulator, true);
//        Grid.Utilities.Util.createOCSCircuit(resource1, client1, simulator, true);
           
//     double dataSize = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.defaultDataSize);
//        double switchingSpeed = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.switchingSpeed);
//        double mu = switchingSpeed / dataSize;
//        int wavelengths = simInstance.configuration.getIntProperty(Config.ConfigEnum.defaultWavelengths);
//        double lamda1 = 10*mu*wavelengths;
//        double lamda2 = 0.5*mu*wavelengths;
//
//        client1.getState().setJobInterArrival(new DDNegExp(simulator, 1/lamda1));
//
     
       // simInstance.run();

        Outputter output = new Outputter(simulator);

        output.printClient(client1);
        output.printResource(resource1);
        output.printSwitch(switch1);
    }
}
