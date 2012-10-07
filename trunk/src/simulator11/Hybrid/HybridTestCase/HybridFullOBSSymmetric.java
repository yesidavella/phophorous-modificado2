/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator11.Hybrid.HybridTestCase;

import Grid.Entity;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Port.GridOutPort;
import Grid.Utilities.Config;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import simbase.SimulationInstance;
import simbase.Stats.SimBaseStats.Stat;
import simbase.Stop.HyrbidStopper;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class HybridFullOBSSymmetric {

    public static int SERVER = 0;
    public static int CLIENT = 1;
    public static int SWITCH = 2;
    public static int BROKER = 3;
    private SimulationInstance simInstance;
    private GridSimulator simulator;
    private int nrOfServer = 0;
    private static int nrOfSwitch = 0;
    private static int nrOfClient = 0;
    private static int nrOfBroker = 0;
    private static ServiceNode broker;
    private ArrayList<Switch> switches = new ArrayList();

    public HybridFullOBSSymmetric() {
        simInstance = new GridSimulation("configFiles\\hybridcase.cfg");

        simulator = new GridSimulator();
        //simInstance.setStopEntity(new HyrbidStopper("HybridStopper", simulator, new Time(simInstance.configuration.getDoubleProperty(Config.ConfigEnum.stopEventOffSetTime))));
        simInstance.setSimulator(simulator);
        broker = Grid.Utilities.Util.createHybridServiceNode("BROKER" + nrOfBroker, simulator);

        Switch center = Grid.Utilities.Util.createHybridSwitch("CENTER" + nrOfSwitch, simulator);
        nrOfSwitch++;

        Grid.Utilities.Util.createBiDirectionalLink(broker, center, 5);

        createBranch(2, center, SERVER);
        createBranch(2, center, CLIENT);

        simulator.route();
        simulator.initEntities();

        switches.add(center);


        //creating the circuit
//        Grid.Utilities.Util.createOCSCircuitInHybridNetwork("SWITCH1", "SWITCH7", simulator);
//        Grid.Utilities.Util.createOCSCircuitInHybridNetwork("SWITCH1", "SWITCH8", simulator);
//        Grid.Utilities.Util.createOCSCircuitInHybridNetwork("SWITCH2", "SWITCH7", simulator);
//        Grid.Utilities.Util.createOCSCircuitInHybridNetwork("SWITCH2", "SWITCH8", simulator);
//
//        Grid.Utilities.Util.createOCSCircuitInHybridNetwork("SWITCH7", "SWITCH1", simulator);
//        Grid.Utilities.Util.createOCSCircuitInHybridNetwork("SWITCH7", "SWITCH2", simulator);
//        Grid.Utilities.Util.createOCSCircuitInHybridNetwork("SWITCH8", "SWITCH1", simulator);
//        Grid.Utilities.Util.createOCSCircuitInHybridNetwork("SWITCH8", "SWITCH2", simulator);


        simInstance.run();

        ArrayList list = simulator.getEntitiesOfType(Switch.class);
        Iterator it2 = list.iterator();
        while (it2.hasNext()) {
            Switch sw = (Switch) it2.next();
            double fail_resultMessage = simulator.getStat(sw, Stat.SWITCH_JOBRESULTMESSAGE_DROPPED);
            double switchedMessages = simulator.getStat(sw, Stat.SWITCH_JOBMESSAGE_SWITCHED);
            double droppedMessages = simulator.getStat(sw, Stat.SWITCH_JOBMESSAGE_DROPPED);

            double totalMessages = switchedMessages + droppedMessages;
            //System.out.println(sw.getId());
            //System.out.println(fail_resultMessage);
            //System.out.println(switchedMessages);
            //System.out.println(droppedMessages);
            //System.out.println(totalMessages);
        }


        List resources = simulator.getEntitiesOfType(ResourceNode.class);
        Iterator it = resources.iterator();
        while (it.hasNext()) {
            ResourceNode res = (ResourceNode) it.next();
            double nrOfJobsServer = simulator.getStat(res, Stat.RESOURCE_JOB_RECEIVED);
            //System.out.println(res.getID() + " " + nrOfJobsServer);
        }

//        // visualisation on a panel
//        Panel panel = new Panel(simulator.getRouting().getHyrbidNetwork());
//        Tree tree = new Tree(new TreeModel(panel.getRootElement()));
//
//        // showing the panel within a frame
//        JFrame frame = new JFrame();
//        frame.setContentPane(tree);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);

    //showing the panel within a frame

//        Panel panel = new Panel(simulator.getRouting().getHyrbidNetwork());
//        JFrame frame = new JFrame();
//        frame.setContentPane(panel);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//        //System.out.println(simulator.getStat(Stat.SWITCH_JOBMESSAGE_DROPPED));
//        //System.out.println(simulator.getStat(Stat.CLIENT_JOB_SENT));
//        //System.out.println(simulator.getStat(simulator.getEntityWithId("SWITCH5"), Stat.SWITCH_JOBMESSAGE_DROPPED));
//        //System.out.println(simulator.getStat(simulator.getEntityWithId("SWITCH5"), Stat.SWITCH_MESSAGE_SWITCHED));
//        //System.out.println(simulator.getStat(simulator.getEntityWithId("SWITCH5"), Stat.SWITCH_JOBMESSAGE_SWITCHED));

//        for(Switch sw: switches){
//            printOutForSwitch(sw);
//        }
    }

    private void createBranch(int depth, Entity baseNode, int type) {

        switch (depth) {
            case 0:
                if (type == SERVER) {
                    ResourceNode res = Grid.Utilities.Util.createHyridResourceNode("RESOURCE" + nrOfServer, simulator);
                    nrOfServer++;
                    //ResourceNode res2 = Grid.Utilities.Util.createHyridResourceNode("RESOURCE" + nrOfServer, simulator);
                    //nrOfServer++;
                    Grid.Utilities.Util.createBiDirectionalLink(baseNode, res, 5);
                    //GridOutPort port1 = baseNode.getOutport(baseNode.getId()+"-"+res.getID());
                    //GridOutPort port2 = res.getOutport(res.getID()+"-"+baseNode.getId());
                    //port1.setSwitchingSpeed(Integer.MAX_VALUE);
                    //port2.setSwitchingSpeed(Integer.MAX_VALUE);
                    //Grid.Utilities.Util.createBiDirectionalLink(baseNode, res2);
                    res.addServiceNode(broker);
                //res2.addServiceNode(broker);
                } else if (type == CLIENT) {
                    ClientNode client = Grid.Utilities.Util.createHybridClient("CLIENT" + nrOfClient, simulator, broker);
                    nrOfClient++;
                    Grid.Utilities.Util.createBiDirectionalLink(baseNode, client, 4);
                    GridOutPort port1 = baseNode.getOutport(baseNode.getId() + "-" + client.getId());
                    GridOutPort port2 = client.getOutport(client.getId() + "-" + baseNode.getId());
                    port1.setSwitchingSpeed(Integer.MAX_VALUE);
                    port2.setSwitchingSpeed(Integer.MAX_VALUE);
                    port1.setLinkSpeed(0);
                    port2.setLinkSpeed(0);
                //ClientNode client2 = Grid.Utilities.Util.createHybridClient("CLIENT" + nrOfClient, simulator, broker);
                //nrOfClient++;
                //Grid.Utilities.Util.createBiDirectionalLink(baseNode, client2, depth);

                }
                break;
            default:
                Switch sw1 = Grid.Utilities.Util.createHybridSwitch("SWITCH" + nrOfSwitch, simulator);
                nrOfSwitch++;
                Switch sw2 = Grid.Utilities.Util.createHybridSwitch("SWITCH" + nrOfSwitch, simulator);
                nrOfSwitch++;
                Grid.Utilities.Util.createBiDirectionalLink(sw1, baseNode, 4);
                Grid.Utilities.Util.createBiDirectionalLink(sw2, baseNode, 4);
                depth--;
                createBranch(depth, sw1, type);
                createBranch(depth, sw2, type);
                switches.add(sw1);
                switches.add(sw2);
                break;
        }
    }

    public static void main(String[] args) {
        new HybridFullOBSSymmetric();
    }
}
