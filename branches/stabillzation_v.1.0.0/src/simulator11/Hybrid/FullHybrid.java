/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator11.Hybrid;

import Distributions.DDNegExp;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Outputter;
import Grid.Routing.ShortesPathRouting;
import Grid.Utilities.Config;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import simbase.SimulationInstance;
import simbase.Stats.SimBaseStats;
import simbase.Time;


/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class FullHybrid {

    private GridSimulator simulator;
    private SimulationInstance simInstance;
    private ResourceNode resource;
    private ServiceNode serviceNode;
    private ClientNode client;
    private ClientNode client2;
    private Switch ingressSwitch;
    private Switch egressSwitch;
    private List<Switch> OBSSwitches = new ArrayList<Switch>();
    private List<Switch> OCSSwitches = new ArrayList<Switch>();

    public FullHybrid() {

        simInstance = new GridSimulation("sources\\configFiles\\yesidsito.cfg");
        simulator = new GridSimulator();
        simInstance.setSimulator(simulator);
        
        simulator.setRouting(new ShortesPathRouting(simulator));

        resource = Grid.Utilities.Util.createHyridResourceNode("Resource", simulator);
        serviceNode = Grid.Utilities.Util.createHybridServiceNode("Service", simulator);
        client = Grid.Utilities.Util.createHybridClient("Client", simulator, serviceNode);
        client2 = Grid.Utilities.Util.createHybridClient("Client2", simulator, serviceNode);

        for (int i = 0; i <= 3; i++) {
            OBSSwitches.add(Grid.Utilities.Util.createHybridSwitch("OBSswitch" + i, simulator));
        }

        for (int i = 0; i <= 5; i++) {
            OCSSwitches.add(Grid.Utilities.Util.createHybridSwitch("OCSswitch" + i, simulator));
        }
        ingressSwitch = Grid.Utilities.Util.createHybridSwitch("IngressSwitch", simulator);
        egressSwitch = Grid.Utilities.Util.createHybridSwitch("EgressSwitch", simulator);

        for (int i = 0; i < 3; i++) {
            Grid.Utilities.Util.createBiDirectionalLink(OBSSwitches.get(i), OBSSwitches.get(i + 1));
        }
        for (int i = 0; i < 5; i++) {
            Grid.Utilities.Util.createBiDirectionalLink(OCSSwitches.get(i), OCSSwitches.get(i + 1));
        }
        Grid.Utilities.Util.createBiDirectionalLink(ingressSwitch, OCSSwitches.get(0));
        Grid.Utilities.Util.createBiDirectionalLink(ingressSwitch, OBSSwitches.get(0));

        Grid.Utilities.Util.createBiDirectionalLink(egressSwitch, OCSSwitches.get(OCSSwitches.size() - 1));
        Grid.Utilities.Util.createBiDirectionalLink(egressSwitch, OBSSwitches.get(OBSSwitches.size() - 1));

        Grid.Utilities.Util.createBiDirectionalLink(ingressSwitch, client);
         Grid.Utilities.Util.createBiDirectionalLink(ingressSwitch, client2);
        Grid.Utilities.Util.createBiDirectionalLink(ingressSwitch, serviceNode);

        Grid.Utilities.Util.createBiDirectionalLink(egressSwitch, resource);       
        
        
        simulator.route();
        List path = OCSSwitches.subList(0, OCSSwitches.size());
        path.add(egressSwitch);
        
//        
//        double dataSize = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.defaultDataSize);
//        double switchingSpeed = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.switchingSpeed);
//        double mu = switchingSpeed / dataSize;
//        int wavelengths = simInstance.configuration.getIntProperty(Config.ConfigEnum.defaultWavelengths);
//        double lamda1 = 10*mu*wavelengths;
//        double lamda2 = 0.5*mu*wavelengths;
//        
//        client.getState().setJobInterArrival(new DDNegExp(simulator, 1/lamda1));
        
//        Grid.Utilities.Util.createOCSCircuit(ingressSwitch,egressSwitch, simulator,true,new Time(0),path);
        Grid.Utilities.Util.createOCSCircuit(ingressSwitch,egressSwitch, simulator,false);
    
        simulator.initEntities();
        resource.addServiceNode(serviceNode);
        simInstance.run();
        Outputter output = new Outputter(simulator);
        
        output.printClient(client);      
        output.printClient(client2);      
        output.printResource(resource);
        output.printSwitch(ingressSwitch);
        output.printSwitch(egressSwitch);
        output.printCircuitsStats();
       
        //System.out.println("*OCS_CIRCUIT_SET_UP:"+simulator.getStat(egressSwitch, SimBaseStats.Stat.OCS_CIRCUIT_SET_UP));
    }

    public static void main(String[] args) {
        new FullHybrid();
    }
}
