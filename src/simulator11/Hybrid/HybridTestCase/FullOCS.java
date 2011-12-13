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
import Grid.Utilities.Config;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import simbase.SimulationInstance;
import simbase.Stats.SimBaseStats.Stat;

/**
 *
 * @author Eothein
 */
public class FullOCS {

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
    public static Object[][] output;

    public FullOCS(double LOAD, int iteration, int initDepth) {
        simInstance = new GridSimulation("hybridcase.cfg");
        simInstance.configuration.setProperty(Config.ConfigEnum.defaultWavelengths.toString(), Double.toString(Math.pow(2, initDepth)));
        //paramter configuration
        int wavelengths = simInstance.configuration.getIntProperty(Config.ConfigEnum.defaultWavelengths);
        double dataSize = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.defaultDataSize);
        double switchingSpeed = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.switchingSpeed);
        double mu = switchingSpeed / dataSize;
        double lambda = LOAD * wavelengths * mu;

        simInstance.configuration.setProperty(Config.ConfigEnum.defaultJobIAT.toString(), Double.toString(1 / lambda));



        simulator = new GridSimulator();
        //simInstance.setStopEntity(new HyrbidStopper("HyrbirdStopper", simulator, new Time(simInstance.configuration.getDoubleProperty(Config.ConfigEnum.stopEventOffSetTime))));
        simInstance.setSimulator(simulator);
        broker = Grid.Utilities.Util.createHybridServiceNode("BROKER" + nrOfBroker, simulator);

        Switch center = Grid.Utilities.Util.createHybridSwitch("CENTER" + nrOfSwitch, simulator);
        nrOfSwitch++;

        Grid.Utilities.Util.createBiDirectionalLink(broker, center, 5);

        createBranch(initDepth, center, SERVER);
        createBranch(initDepth, center, CLIENT);

        simulator.route();
        simulator.initEntities();

        switches.add(center);
        
        List clients = simulator.getEntitiesOfType(ClientNode.class);
        List resources = simulator.getEntitiesOfType(ResourceNode.class);
        for(int i =0; i<clients.size(); i++){
            ClientNode cl = (ClientNode)clients.get(i);
            for(int j =0; j< resources.size(); j++){
                ResourceNode resource = (ResourceNode)resources.get(j);
                Grid.Utilities.Util.createOCSCircuit(resource, cl, simulator,true);
                Grid.Utilities.Util.createOCSCircuit(cl,resource , simulator,true);
            }
        }


//        //creating the circuit
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

        int rowNr = -1;

        rowNr++;
        output[rowNr][iteration] = wavelengths;
        rowNr++;
        output[rowNr][iteration] = dataSize;
        rowNr++;
        output[rowNr][iteration] = mu;
        rowNr++;
        output[rowNr][iteration] = switchingSpeed;
        rowNr++;
        output[rowNr][iteration] = lambda;


        while (it2.hasNext()) {
            Switch sw = (Switch) it2.next();
            double fail_resultMessage = simulator.getStat(sw, Stat.SWITCH_JOBRESULTMESSAGE_DROPPED);
            double switchedResultMessages = simulator.getStat(sw, Stat.SWITCH_JOBRESULTMESSAGE_SWITCHED);
            double resultRelative = fail_resultMessage / (fail_resultMessage + switchedResultMessages);

            double switchedJobMessages = simulator.getStat(sw, Stat.SWITCH_JOBMESSAGE_SWITCHED);
            double droppedJobMessages = simulator.getStat(sw, Stat.SWITCH_JOBMESSAGE_DROPPED);
            double jobRelative = droppedJobMessages / (switchedJobMessages + droppedJobMessages);

            double messagesDropped = simulator.getStat(sw, Stat.SWITCH_MESSAGE_DROPPED);
            double messagesSwitched = simulator.getStat(sw, Stat.SWITCH_MESSAGE_SWITCHED);
            double relative = messagesDropped / (messagesDropped + messagesSwitched);


            rowNr++;
            output[rowNr][iteration] = sw.getId();
            rowNr++;
            output[rowNr][iteration] = switchedJobMessages;
            rowNr++;
            output[rowNr][iteration] = droppedJobMessages;
            rowNr++;
            output[rowNr][iteration] = switchedResultMessages;
            rowNr++;
            output[rowNr][iteration] = fail_resultMessage;
            rowNr++;
            output[rowNr][iteration] = messagesSwitched;
            rowNr++;
            output[rowNr][iteration] = messagesDropped;
            rowNr++;
            output[rowNr][iteration] = resultRelative;
            rowNr++;
            output[rowNr][iteration] = jobRelative;
            rowNr++;
            output[rowNr][iteration] = relative;


        }


        
        Iterator it = resources.iterator();
        while (it.hasNext()) {
            ResourceNode res = (ResourceNode) it.next();
            double nrOfJobsReceivedByServer = simulator.getStat(res, Stat.RESOURCE_JOB_RECEIVED);
            double nrOfJobsExcuted = simulator.getStat(res, Stat.CLIENT_JOB_SENT.RESOURCE_RESULTS_SENT);
            double relExecuted = nrOfJobsExcuted / nrOfJobsReceivedByServer;

            double resourceFailings = simulator.getStat(res, Stat.RESOURCE_FAIL_NO_FREE_PLACE);
            double resourceFailedToSend = simulator.getStat(res, Stat.RESOURCE_SENDING_FAILED);


            rowNr++;
            output[rowNr][iteration] = res.getID();
            rowNr++;
            output[rowNr][iteration] = Double.toString(nrOfJobsReceivedByServer);
            rowNr++;
            output[rowNr][iteration] = Double.toString(nrOfJobsExcuted);
            rowNr++;
            output[rowNr][iteration] = resourceFailings;
            rowNr++;
            output[rowNr][iteration] = resourceFailedToSend;
            rowNr++;
            output[rowNr][iteration] = relExecuted;
        }

        
        it = clients.iterator();
        while (it.hasNext()) {
            ClientNode c = (ClientNode) it.next();
            double jobsRequestSend = simulator.getStat(c, Stat.CLIENT_REQ_SENT);
            double jobsNotSend = simulator.getStat(c, Stat.CLIENT_SENDING_FAILED);
            double jobsBlocked = jobsNotSend / jobsRequestSend;


            double jobsSendByClient = simulator.getStat(c, Stat.CLIENT_JOB_SENT);
            double resultsReceived = simulator.getStat(c, Stat.CLIENT_RESULTS_RECEIVED);
            double relativeResults = resultsReceived / jobsSendByClient;


            rowNr++;
            output[rowNr][iteration] = c.getId();
            rowNr++;
            output[rowNr][iteration] = jobsRequestSend;
            rowNr++;
            output[rowNr][iteration] = jobsNotSend;
            rowNr++;
            output[rowNr][iteration] = resultsReceived;
            rowNr++;
            output[rowNr][iteration] = jobsSendByClient;
            rowNr++;
            output[rowNr][iteration] = jobsBlocked;
            rowNr++;
            output[rowNr][iteration] = relativeResults;

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
//        System.out.println(simulator.getStat(Stat.SWITCH_JOBMESSAGE_DROPPED));
//        System.out.println(simulator.getStat(Stat.CLIENT_JOB_SENT));
//        System.out.println(simulator.getStat(simulator.getEntityWithId("SWITCH5"), Stat.SWITCH_JOBMESSAGE_DROPPED));
//        System.out.println(simulator.getStat(simulator.getEntityWithId("SWITCH5"), Stat.SWITCH_MESSAGE_SWITCHED));
//        System.out.println(simulator.getStat(simulator.getEntityWithId("SWITCH5"), Stat.SWITCH_JOBMESSAGE_SWITCHED));

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
                    Grid.Utilities.Util.createBiDirectionalLink(baseNode, res);
                    res.addServiceNode(broker);
                //res2.addServiceNode(broker);
                } else if (type == CLIENT) {
                    ClientNode client = Grid.Utilities.Util.createHybridClient("CLIENT" + nrOfClient, simulator, broker);
                    nrOfClient++;
                    Grid.Utilities.Util.createBiDirectionalLink(baseNode, client);

                }
                break;
            default:
                Switch sw1 = Grid.Utilities.Util.createHybridSwitch("SWITCH" + nrOfSwitch, simulator);
                nrOfSwitch++;
                Switch sw2 = Grid.Utilities.Util.createHybridSwitch("SWITCH" + nrOfSwitch, simulator);
                nrOfSwitch++;
                Grid.Utilities.Util.createBiDirectionalLink(sw1, baseNode);
                Grid.Utilities.Util.createBiDirectionalLink(sw2, baseNode);
                depth--;
                createBranch(depth, sw1, type);
                createBranch(depth, sw2, type);
                switches.add(sw1);
                switches.add(sw2);
                break;
        }
    }

    public static void main(String[] args) {

        int initDepth = Integer.parseInt(args[0]);
        boolean hybrid = Boolean.parseBoolean(args[1]);

        double numberEndNodes = Math.pow(2, initDepth);

        double nrOfSwitches = 0;
        for (int i = 0; i <= initDepth; i++) {
            nrOfSwitches += Math.pow(2, i);
        }
        nrOfSwitches = 2 * nrOfSwitches;
        nrOfSwitches--;
        System.out.println(numberEndNodes + " " + nrOfSwitches);
        output = new Object[(int) nrOfSwitches * 10 + (int) numberEndNodes * 6 + (int) numberEndNodes * 7 + 5][10];

        double[] LOADS = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
        for (int i = 0; i < LOADS.length; i++) {
            new FullOCS(LOADS[i], i, initDepth);
            System.gc();
        }

        if (hybrid) {
            System.out.println("Hybrid \t" + initDepth);
        } else {
            System.out.println("OBS \t" + initDepth);
        }

        for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < output[0].length; j++) {
                System.out.print(output[i][j] + "\t");
            }
            System.out.println();
        }
    }
}
