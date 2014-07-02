/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator11.Hybrid;

import Distributions.DDNegExp;
import Distributions.DDNormal;
import Distributions.DDUniform;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.Messages.JobRequestMessage;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Outputter;
import Grid.Utilities.Config;
import java.util.ArrayList;
import java.util.List;
import simbase.SimulationInstance;
import simbase.Time;

public class PruebaI 
{
    private GridSimulator simulator;
    private SimulationInstance simInstance; 
            
    private ResourceNode resource1;
    private ResourceNode resource2;
    
    private ServiceNode serviceNode1;
    private ServiceNode serviceNode2;
    private ServiceNode serviceNode3;
    
    private ClientNode client1;
    private ClientNode client2;
    private ClientNode client3;
    private ClientNode client4;    
    
    private Switch switch1;
    private Switch switch2;
    private Switch switch3;
    private Switch switch4;
    private Switch switch5;
    private Switch switch6;
    private Switch switch7;
   private List<Switch> switchesList = new ArrayList<Switch>();
    
   public PruebaI()
   {
        simInstance = new GridSimulation("sources\\configFiles\\loadCircuits.cfg");
        simulator = new GridSimulator();
        simInstance.setSimulator(simulator);
        
        resource1 = Grid.Utilities.Util.createHyridResourceNode("Resource1", simulator);
        resource2 = Grid.Utilities.Util.createHyridResourceNode("Resource2", simulator);
        
        serviceNode1 = Grid.Utilities.Util.createHybridServiceNode("ServiceNode1", simulator);
        serviceNode2 = Grid.Utilities.Util.createHybridServiceNode("ServiceNode2", simulator);
        serviceNode3 = Grid.Utilities.Util.createHybridServiceNode("ServiceNode3", simulator);
        
     
        
        client1 = Grid.Utilities.Util.createHybridClient("Client1", simulator, serviceNode1);
        client2 = Grid.Utilities.Util.createHybridClient("Client2", simulator, serviceNode1);
        client3 = Grid.Utilities.Util.createHybridClient("Client3", simulator, serviceNode2);
        client4 = Grid.Utilities.Util.createHybridClient("Client4", simulator, serviceNode3);
        
        switch1 = Grid.Utilities.Util.createHybridSwitch("switch1" , simulator); 
        switch2 = Grid.Utilities.Util.createHybridSwitch("switch2" , simulator); 
        switch3 = Grid.Utilities.Util.createHybridSwitch("switch3" , simulator); 
        switch4 = Grid.Utilities.Util.createHybridSwitch("switch4" , simulator); 
        switch5 = Grid.Utilities.Util.createHybridSwitch("switch5" , simulator); 
        switch6 = Grid.Utilities.Util.createHybridSwitch("switch6" , simulator); 
        switch7 = Grid.Utilities.Util.createHybridSwitch("switch7" , simulator); 
        
        Grid.Utilities.Util.createBiDirectionalLink(switch1, switch2);
        Grid.Utilities.Util.createBiDirectionalLink(switch2, switch3); 
        Grid.Utilities.Util.createBiDirectionalLink(switch2, switch4); 
        Grid.Utilities.Util.createBiDirectionalLink(switch2, switch5);
        Grid.Utilities.Util.createBiDirectionalLink(switch3, switch4);
        Grid.Utilities.Util.createBiDirectionalLink(switch3, switch6); 
        Grid.Utilities.Util.createBiDirectionalLink(switch4, switch5);
        Grid.Utilities.Util.createBiDirectionalLink(switch4, switch6);
        Grid.Utilities.Util.createBiDirectionalLink(switch5, switch6);
        Grid.Utilities.Util.createBiDirectionalLink(switch5, switch7);
        
        Grid.Utilities.Util.createBiDirectionalLink(switch1, client1);
        Grid.Utilities.Util.createBiDirectionalLink(switch1, client2);
        
        Grid.Utilities.Util.createBiDirectionalLink(switch3, client3);
        Grid.Utilities.Util.createBiDirectionalLink(switch5, client4);
        
        Grid.Utilities.Util.createBiDirectionalLink(switch1, serviceNode1);
        Grid.Utilities.Util.createBiDirectionalLink(switch3, serviceNode2);
        Grid.Utilities.Util.createBiDirectionalLink(switch5, serviceNode3);
        
        Grid.Utilities.Util.createBiDirectionalLink(resource1, switch7);  
        Grid.Utilities.Util.createBiDirectionalLink(resource2, switch5);  
        
        simulator.route();
        
        switchesList.add(switch1);
        switchesList.add(switch2);
        switchesList.add(switch4);
        switchesList.add(switch6);
        switchesList.add(switch7);        
        List path = switchesList.subList(0, switchesList.size());        
        Grid.Utilities.Util.createOCSCircuit(switch1,switch7, simulator,true,new Time(0),path);
        
        double dataSize = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.defaultDataSize);
        double switchingSpeed = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.switchingSpeed);
        double mu = switchingSpeed / dataSize;
        int wavelengths = simInstance.configuration.getIntProperty(Config.ConfigEnum.defaultWavelengths);
        double lamda1 = 10*mu*wavelengths; //120
        double lamda2 = 0.5*mu*wavelengths; //6
        
        
        //**************************************************************************************
        //Modificaion del la distribucion que genera los intervalos de timempo entre tarea y tarea.
//        client1.getState().setJobInterArrival(new DDNegExp(simulator, 1/lamda1));        
//        client2.getState().setJobInterArrival(new DDUniform(simulator, 1000, 1500));
//        client3.getState().setJobInterArrival(new DDNormal(simulator, 25, 10));
        //**************************************************************************************
        
        //**************************************************************************************
        //Modificacion para establecer la distribucion para el tamaño del archivo
        //client1.getState().setSizeDistribution(new DDNegExp(simulator,00.1));
        //client2.getState().setSizeDistribution(new DDNormal(simulator,5000,500));
        //***************************************************************************************
      
        //**************************************************************************************
        //Modificacion para establecer la distribucion para el unidades CPU requeridas por la tarea 
        // client1.getState(). setFlops(new DDNegExp(simulator,250000));      
        //*********************************************************************
        
        //**************************************************************************************
        // Los siguientes son lo diferente tipo de agentadores.
        ////new UniformSelector(resources); 
        //new StandardSelector(resources, this, sim); 
        //new RoundRobinResourceSelector(resources);
        // y solo se establece el contructor de la clase AbstractServiceNode ->resourceSelector; 
        
        //***********************************************************************************
        // configuracion de la capacida de procesamiento y del numero de CPU
        // resource1.setCpuCapacity(1000000000);
       //  resource1.setCpuCount(100, 500);
        //********************************************************************************
        
        client1.getState().setFlops(new DDUniform(simulator, 200000, 300000));      
       
        
        double o = resource1.getStorageCount();
         resource1.setStorageCount(1000);
         simulator.initEntities();
         resource1.addServiceNode(serviceNode1);
         resource2.addServiceNode(serviceNode2);
         resource2.addServiceNode(serviceNode3);
         
  
        simInstance.run();
        Outputter output = new Outputter(simulator);
        
        output.printClient(client1);      
        output.printClient(client2);
        output.printClient(client3);      
        output.printClient(client4); 
           
         
        output.printResource(resource1);
        output.printResource(resource2);
        
        output.printSwitch(switch1);
        output.printSwitch(switch7);                    
        
   }
     public static void main(String[] args) {
        new PruebaI();

    }
    
}
