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
import Grid.Utilities.Config;
import Grid.Utilities.HopToHopLinks;
import java.text.DecimalFormat;
import simbase.SimulationInstance;
import simbase.Stats.SimBaseStats;
import simbase.Stats.SimBaseStats.Stat;
import simbase.Stop.LoadStopper;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class Setup {

    public static String[] switches = {"Dublin", "Glasgow", "Oslo", "Stockholm",
        "Amsterdam", "Hamburg", "Copenhagen", "Warsaw", "Budapest", "Belgrade", "Athens",
        "Rome", "Milan", "Zurich", "Lyon", "Barcelona", "Madrid", "Bordeaux", "Paris",
        "London", "Brussels", "Strasbourg", "Munich", "Vienna", "Prague", "Berlin", "Frankfurt",
        "Zagreb"
    };
    /**
     * The simulator instance
     */
    private SimulationInstance simInstance;
    /**
     * The simulator itself
     */
    private GridSimulator simulator;


    public Setup() {
        simInstance = new GridSimulation("Reduced.cfg");
        double flopSize = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.defaultFlopSize);
        simInstance.configuration.setProperty(Config.ConfigEnum.defaultFlopSize.toString(), new Double(flopSize/500).toString());
        simulator = new GridSimulator();

        simInstance.setSimulator(simulator);
        HopToHopLinks creator = new HopToHopLinks(simulator);

        //Creation of resourcebroker
        ServiceNode broker = Grid.Utilities.Util.createOBSServiceNode("broker", simulator);

        LoadStopper stopper = new LoadStopper("Load stopper for " + broker.getID(), simulator, new Time(1), 8000000, broker);
        simInstance.setStopEntity(stopper);
        //Creation of the network
        //Switchcreation
        for (int i = 0; i < this.switches.length; i++) {
            Switch sw = Grid.Utilities.Util.createOBSSwitch(switches[i] + "-switch", simulator, true);
            ClientNode client = Grid.Utilities.Util.createOBSClient(switches[i] + "-client", simulator, broker);
            Grid.Utilities.Util.createBiDirectionalLink(sw, client);
        }
        //Bidirectional link creation

        creator.addBidirectionLinkFromString("Madrid-switch", "Bordeaux-switch");
        creator.addBidirectionLinkFromString("Bordeaux-switch", "Paris-switch");
        creator.addBidirectionLinkFromString("Paris-switch", "London-switch");
        creator.addBidirectionLinkFromString("London-switch", "Dublin-switch");
        creator.addBidirectionLinkFromString("Dublin-switch", "Glasgow-switch");
        creator.addBidirectionLinkFromString("Glasgow-switch", "Amsterdam-switch");
        creator.addBidirectionLinkFromString("Amsterdam-switch", "Hamburg-switch");
        creator.addBidirectionLinkFromString("Hamburg-switch", "Copenhagen-switch");
        creator.addBidirectionLinkFromString("Copenhagen-switch", "Oslo-switch");
        creator.addBidirectionLinkFromString("Oslo-switch", "Stockholm-switch");
        creator.addBidirectionLinkFromString("Stockholm-switch", "Warsaw-switch");
        creator.addBidirectionLinkFromString("Warsaw-switch", "Budapest-switch");
        creator.addBidirectionLinkFromString("Budapest-switch", "Belgrade-switch");
        creator.addBidirectionLinkFromString("Belgrade-switch", "Athens-switch");
        creator.addBidirectionLinkFromString("Athens-switch", "Rome-switch");
        creator.addBidirectionLinkFromString("Rome-switch", "Milan-switch");
        creator.addBidirectionLinkFromString("Milan-switch", "Zurich-switch");
        creator.addBidirectionLinkFromString("Zurich-switch", "Lyon-switch");
        creator.addBidirectionLinkFromString("Lyon-switch", "Barcelona-switch");
        creator.addBidirectionLinkFromString("Barcelona-switch", "Madrid-switch");
        creator.addBidirectionLinkFromString("Paris-switch", "Lyon-switch");
        creator.addBidirectionLinkFromString("Paris-switch", "Strasbourg-switch");
        creator.addBidirectionLinkFromString("Strasbourg-switch", "Zurich-switch");
        creator.addBidirectionLinkFromString("Strasbourg-switch", "Frankfurt-switch");
        creator.addBidirectionLinkFromString("Frankfurt-switch", "Munich-switch");
        creator.addBidirectionLinkFromString("Munich-switch", "Milan-switch");
        creator.addBidirectionLinkFromString("Munich-switch", "Vienna-switch");
        creator.addBidirectionLinkFromString("Vienna-switch", "Zagreb-switch");
        creator.addBidirectionLinkFromString("Zagreb-switch", "Belgrade-switch");
        creator.addBidirectionLinkFromString("Zagreb-switch", "Rome-switch");
        creator.addBidirectionLinkFromString("Vienna-switch", "Prague-switch");
        creator.addBidirectionLinkFromString("Prague-switch", "Budapest-switch");
        creator.addBidirectionLinkFromString("Prague-switch", "Berlin-switch");
        creator.addBidirectionLinkFromString("Berlin-switch", "Warsaw-switch");
        creator.addBidirectionLinkFromString("Berlin-switch", "Hamburg-switch");
        creator.addBidirectionLinkFromString("Berlin-switch", "Munich-switch");
        creator.addBidirectionLinkFromString("Paris-switch", "Brussels-switch");
        creator.addBidirectionLinkFromString("Brussels-switch", "Amsterdam-switch");
        creator.addBidirectionLinkFromString("Brussels-switch", "Frankfurt-switch");

        //Creation of the Resources
        ResourceNode amsterdam = Grid.Utilities.Util.createOBSResource("Amsterdam-resource", simulator);
        ResourceNode paris = Grid.Utilities.Util.createOBSResource("Paris-resource", simulator);
        ResourceNode berlin = Grid.Utilities.Util.createOBSResource("Berlin-resource", simulator);
        ResourceNode budapest = Grid.Utilities.Util.createOBSResource("Budapest-resource", simulator);
        ResourceNode madrid = Grid.Utilities.Util.createOBSResource("Madrid-resource", simulator);
        ResourceNode rome = Grid.Utilities.Util.createOBSResource("Rome-resource", simulator);

        creator.addBidirectionLinkFromString("Amsterdam-resource", "Amsterdam-switch");
        creator.addBidirectionLinkFromString("Paris-resource", "Paris-switch");
        creator.addBidirectionLinkFromString("Berlin-resource", "Berlin-switch");
        creator.addBidirectionLinkFromString("Budapest-resource", "Budapest-switch");
        creator.addBidirectionLinkFromString("Madrid-resource", "Madrid-switch");
        creator.addBidirectionLinkFromString("Rome-resource", "Rome-switch");


        creator.addBidirectionLinkFromString("broker", "Brussels-switch");



        simulator.route();

        simulator.initEntities();
        amsterdam.addServiceNode(broker);
        paris.addServiceNode(broker);
        berlin.addServiceNode(broker);
        budapest.addServiceNode(broker);
        madrid.addServiceNode(broker);
        rome.addServiceNode(broker);
        simInstance.run();


        
        //System.out.println("------------Simulation--------------");

     
        double lambda = 28* (1/simInstance.configuration.getDoubleProperty(Config.ConfigEnum.defaultJobIAT));
        double C = 120;
        double beta = 1/(simInstance.configuration.getDoubleProperty(Config.ConfigEnum.defaultFlopSize)/
                simInstance.configuration.getDoubleProperty(Config.ConfigEnum.defaultCapacity));
        
        //System.out.println("Resource load = " + lambda/(C*beta) + " " + lambda + " " + C + "  " + beta);
        
        print(Stat.CLIENT_JOB_SENT);
        print(Stat.CLIENT_REQ_ACK_RECEIVED);
        print(Stat.CLIENT_REQ_SENT);
        print(Stat.CLIENT_RESOURCES_BUSY_MSG);
        print(Stat.CLIENT_RESULTS_RECEIVED);
        print(Stat.CLIENT_SENDING_FAILED);
        print(Stat.RESOURCE_BUSY_TIME);
        print(Stat.RESOURCE_FAIL_NO_FREE_PLACE);
        print(Stat.RESOURCE_JOB_RECEIVED);
        print(Stat.RESOURCE_REGISTRATION_SENT);
        print(Stat.RESOURCE_RESULTS_SENT);
        print(Stat.RESOURCE_SENDING_FAILED);
        print(Stat.SERVICENODE_NO_FREE_RESOURCE);
        print(Stat.SERVICENODE_REGISTRATION_RECEIVED);
        print(Stat.SERVICENODE_REQ_ACK_SENT);
        print(Stat.SERVICENODE_REQ_RECIEVED);
        print(Stat.SERVICENODE_SENDING_FAILED);
        print(Stat.SWITCH_MESSAGE_DROPPED);
        print(Stat.SWITCH_JOBMESSAGE_DROPPED);
        print(Stat.SWITCH_JOBRESULTMESSAGE_DROPPED);
        
        simInstance = null;
        simulator = null;
        System.gc();

    }


    private void print(SimBaseStats.Stat stat) {
        DecimalFormat format = new DecimalFormat();
        //System.out.print(stat.toString());
        //System.out.print("\t");
        //System.out.print(format.format(simulator.getStat(stat)));
        //System.out.println();
    }

    public static void main(String[] args) {
        new Setup();
    }
}
