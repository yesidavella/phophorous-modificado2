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
import Grid.Outputter;
import Grid.Utilities.Config;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import simbase.SimulationInstance;
import simbase.Stats.SimBaseStats.Stat;
import simbase.Stop.StopperWhenOCSIsOk;
import simbase.Time;

/**
 *
 * @author Eothein
 */
public class HybridWaveCase {

    public static int SERVER = 0;
    public static int CLIENT = 1;
    public static int SWITCH = 2;
    public static int BROKER = 3;
    private SimulationInstance simInstance;
    private GridSimulator simulator;
    private int nrOfServer = 0;
    private int nrOfSwitch = 0;
    private int nrOfClient = 0;
    private int nrOfBroker = 0;
    private ServiceNode broker;
    private ArrayList<Switch> switches = new ArrayList();
    public static int[] wavesTable = {128,64,32, 16};
    public static double[] lambdasjaj = {1.6,3.2,4.8,6.4,8,9.6,11.2,12.8,14.4,16,17.6,19.2,20.8,22.4,24,25.6,27.2,28.8,30.4,32};
    Graph g;
    VisualizationViewer vv;

    public HybridWaveCase(double LOAD, int initDepth, int OCSDepth) {

        Outputter outputter = null;


        simInstance = new GridSimulation("hybridWaveCase.cfg");

        double mu = simInstance.configuration.getDoubleProperty(Config.ConfigEnum.switchingSpeed) / simInstance.configuration.getDoubleProperty(Config.ConfigEnum.defaultDataSize);

        //parameter configuration

        double lambda = lambdasjaj[(int)LOAD];

        simInstance.configuration.setProperty(Config.ConfigEnum.defaultJobIAT.toString(), Double.toString(1 / lambda));

        simulator = new GridSimulator();

        simInstance.setStopEntity(new StopperWhenOCSIsOk("OCSStopper", simulator, new Time(simInstance.configuration.getDoubleProperty(Config.ConfigEnum.stopEventOffSetTime))));

        //simInstance.setStopEntity(new HyrbidStopper("HyrbirdStopper", simulator, new Time(simInstance.configuration.getDoubleProperty(Config.ConfigEnum.stopEventOffSetTime))));
        simInstance.setSimulator(simulator);

        broker = Grid.Utilities.Util.createHybridServiceNode("BROKER" + nrOfBroker, simulator);

        Switch center = Grid.Utilities.Util.createHybridSwitch("CENTER" + nrOfSwitch, simulator);
        nrOfSwitch++;

        Grid.Utilities.Util.createBiDirectionalLink(broker, center);

        List<Switch> OCSdepthSwitches = new ArrayList();

        createBranch(initDepth, initDepth, center, SERVER, OCSDepth, OCSdepthSwitches);
        createBranch(initDepth, initDepth, center, CLIENT, OCSDepth, OCSdepthSwitches);

        if (OCSdepthSwitches.size() % 2 != 0) {
            throw new IllegalArgumentException("Something went wrong when deciding which switch is on OCSDepth");
        }

        simulator.route();
        simulator.initEntities();

        switches.add(center);

        if (OCSDepth > 0) {
            List<Switch> part1 = OCSdepthSwitches.subList(0, OCSdepthSwitches.size() / 2);
            List<Switch> part2 = OCSdepthSwitches.subList(OCSdepthSwitches.size() / 2, OCSdepthSwitches.size());

            for (Switch sw : part1) {
                for (Switch sw2 : part2) {
                    
                    for (int i = 0; i < Math.floor(wavesTable[OCSDepth - 1] / Math.pow(2, OCSDepth)); i++) {
                        //Grid.Utilities.Util.createOCSCircuitInHybridNetwork(sw, sw2, simulator);
                        Grid.Utilities.Util.createOCSCircuit(sw2, sw, simulator, true);

                    }
                }
          
            }
        }



        simInstance.run();

        ArrayList list = simulator.getEntitiesOfType(Switch.class);

        //System.out.println(lambda/(wavesTable[wavesTable.length-1]*mu));
        
        //Outputting
        double messageSend = simulator.getStat(Stat.CLIENT_JOB_SENT);
        double jobMessagesDroppedInNetwork = simulator.getStat(Stat.SWITCH_JOBMESSAGE_DROPPED);
        //System.out.println(messageSend);
        //System.out.println(jobMessagesDroppedInNetwork);
        //System.out.println(jobMessagesDroppedInNetwork/messageSend);
        
        try{
        
            FileOutputStream clientOut = new FileOutputStream("client.out");
            PrintStream clientStream = new PrintStream(clientOut);
            Outputter clientOutPutter = new Outputter(clientStream, simulator);
            
            FileOutputStream resourceOut = new FileOutputStream("resource.out");
            PrintStream resourceStream = new PrintStream(resourceOut);
            Outputter resourceOutPutter = new Outputter(resourceStream, simulator);
            
            FileOutputStream switchOut = new FileOutputStream("switch.out");
            PrintStream switchStream = new PrintStream(switchOut);
            Outputter switchOutPutter = new Outputter(switchStream, simulator);
            
            List switchNodes = simulator.getEntitiesOfType(Switch.class);
            List clients = simulator.getEntitiesOfType(ClientNode.class);
            List resources = simulator.getEntitiesOfType(ResourceNode.class);
            Iterator sw = switchNodes.iterator();
            Iterator cl = clients.iterator();
            Iterator res = resources.iterator();
            while(sw.hasNext()){
                Switch node = (Switch)sw.next();
                switchOutPutter.printSwitch(node);
            }
            while(cl.hasNext()){
                ClientNode node = (ClientNode)cl.next();
                clientOutPutter.printClient(node);
            }
            while(res.hasNext()){
                ResourceNode node = (ResourceNode)res.next();
                resourceOutPutter.printResource(node);
            }
            
            
            
        }catch(IOException e){
            e.printStackTrace();
        }
        
        

//         g = ((RoutingViaJung) simulator.getRouting()).getHybridNetwork();
//        final VisualizationViewer vv = new VisualizationViewer(new SpringLayout(g));
//        vv.addGraphMouseListener(new TestGraphMouseListener<String>());
//        vv.getRenderer().setVertexRenderer(
//        		new GradientVertexRenderer(
//        				Color.white, Color.red, 
//        				Color.white, Color.blue,
//        				vv.getPickedVertexState(),
//        				false));
//        vv.getRenderContext().setEdgeDrawPaintTransformer(new ConstantTransformer(Color.lightGray));
//        vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
//        vv.getRenderContext().setArrowDrawPaintTransformer(new ConstantTransformer(Color.lightGray));
//        vv.setVertexToolTipTransformer(new ToStringLabeller<String>());
//        vv.setEdgeToolTipTransformer(new Transformer() {
//			public String transform(Object edge) {
//				return "E"+g.getEndpoints(edge).toString();
//			}});
//        
//        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
//        vv.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
//        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
//        vv.setForeground(Color.lightGray);
//        final JFrame frame = new JFrame();
//        Container content = frame.getContentPane();
//        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
//        content.add(panel);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<String,Number>();
//        vv.setGraphMouse(graphMouse);
//        
//        vv.addKeyListener(graphMouse.getModeKeyListener());
//        vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");
//        
//        final ScalingControl scaler = new CrossoverScalingControl();
//
//        JButton plus = new JButton("+");
//        plus.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                scaler.scale(vv, 1.1f, vv.getCenter());
//            }
//        });
//        JButton minus = new JButton("-");
//        minus.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                scaler.scale(vv, 1/1.1f, vv.getCenter());
//            }
//        });
//
//        JButton reset = new JButton("reset");
//        reset.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).setToIdentity();
//				vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).setToIdentity();
//			}});
//
//        JPanel controls = new JPanel();
//        controls.add(plus);
//        controls.add(minus);
//        controls.add(reset);
//        content.add(controls, BorderLayout.SOUTH);
//
//        frame.pack();
//        frame.setVisible(true);
        


    }
    
        /**
     * A nested class to demo the GraphMouseListener finding the
     * right vertices after zoom/pan
     */
    static class TestGraphMouseListener<V> implements GraphMouseListener<V> {
        
    		public void graphClicked(V v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was clicked at ("+me.getX()+","+me.getY()+")");
    		}
    		public void graphPressed(V v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was pressed at ("+me.getX()+","+me.getY()+")");
    		}
    		public void graphReleased(V v, MouseEvent me) {
    		    System.err.println("Vertex "+v+" was released at ("+me.getX()+","+me.getY()+")");
    		}
    }

    private void createBranch(int initDepth, int depth, Entity baseNode, int type, int OCSDepth, List OCSswitches) {



        switch (depth) {
            case 0:
                Entity ent = null;
                if (type == SERVER) {
                    ent = Grid.Utilities.Util.createHyridResourceNode("RESOURCE" + nrOfServer, simulator);
                    nrOfServer++;
                    if (depth >= initDepth - OCSDepth) {
                        Grid.Utilities.Util.createBiDirectionalLink(ent, baseNode, 76);
                    } else {
                        Grid.Utilities.Util.createBiDirectionalLink(ent, baseNode, 76);
                    }
                    ((ResourceNode) ent).addServiceNode(broker);
                } else if (type == CLIENT) {
                    ent = Grid.Utilities.Util.createHybridClient("CLIENT" + nrOfClient, simulator, broker);
                    nrOfClient++;
                    if (depth >= initDepth - OCSDepth) {
                        Grid.Utilities.Util.createBiDirectionalLink(ent, baseNode, 1000);
                    } else {
                        Grid.Utilities.Util.createBiDirectionalLink(ent, baseNode, 1000);
                    }
                }
                break;
            default:
                int realWaves = wavesTable[initDepth - depth];
                Switch sw1 = Grid.Utilities.Util.createHybridSwitch("SWITCH" + nrOfSwitch, simulator);
                nrOfSwitch++;
                Switch sw2 = Grid.Utilities.Util.createHybridSwitch("SWITCH" + nrOfSwitch, simulator);
                nrOfSwitch++;
                ////System.out.println(sw1 + " "+ sw2 + " " +baseNode+" "+realWaves);



                    Grid.Utilities.Util.createBiDirectionalLink(sw1, baseNode, realWaves);
                    Grid.Utilities.Util.createBiDirectionalLink(sw2, baseNode, realWaves);


                depth--;
                if (depth == initDepth - OCSDepth) {
                    OCSswitches.add(sw1);
                    OCSswitches.add(sw2);
                }
                createBranch(initDepth, depth, sw1, type, OCSDepth, OCSswitches);
                createBranch(initDepth, depth, sw2, type, OCSDepth, OCSswitches);
                switches.add(sw1);
                switches.add(sw2);
                break;
        }
    }

    /**
     * Create this case
     * @param args
     */
    public static void main(String[] args) {
        double LOAD = Double.parseDouble(args[0]);
        int initDepth = Integer.parseInt(args[1]);
        int OCSDepth = Integer.parseInt(args[2]);
        new HybridWaveCase(LOAD, initDepth, OCSDepth);

    }
}
