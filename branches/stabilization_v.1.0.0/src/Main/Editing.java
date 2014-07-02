/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import Grid.GridSimulator;
import Grid.Routing.GridEdge;
import Grid.Routing.GridVertex;
import Main.Factories.Hybrid.EdgeFactory;
import Main.Factories.Hybrid.HybridClientFactory;
import Main.Factories.Hybrid.HybridResourceFactory;
import Main.Factories.Hybrid.HybridServiceFactory;
import Main.Factories.Hybrid.HybridSwitchFactory;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.apache.commons.collections15.Factory;

/**
 *
 * @author Eothein
 */
public class Editing extends JPanel {

    /**
     * The simulator
     */
    private GridSimulator simulator;
    /**
     * The graph which is constructed.
     */
    private Graph graph;
    /**
     * The layout which has been used.
     */
    private AbstractLayout layout;
    /**
     * Visual component for the graph.
     */
    private VisualizationViewer vv;

    public Editing(Graph graph, GridSimulator simulator) {

        super();
        this.simulator = simulator;
        this.layout = new StaticLayout<Number, Number>(graph,
                new Dimension(600, 600));
        this.vv = new VisualizationViewer(layout);
        vv.setBackground(Color.white);
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        this.add(panel);
        Factory<GridVertex> clientFactory = new HybridClientFactory(simulator);
        Factory<GridVertex> switchFactory = new HybridSwitchFactory(simulator);
        Factory<GridVertex> brokerFactory = new HybridServiceFactory(simulator);
        Factory<GridVertex> resourceFactory = new HybridResourceFactory(simulator);
        Factory<GridEdge> edgeFactory = new EdgeFactory();


        final EditingModalGraphMouse clientMouse =
                new EditingModalGraphMouse(vv.getRenderContext(), clientFactory, edgeFactory);
        final EditingModalGraphMouse switchMouse =
                new EditingModalGraphMouse(vv.getRenderContext(), switchFactory, edgeFactory);
        final EditingModalGraphMouse brokerMouse =
                new EditingModalGraphMouse(vv.getRenderContext(), brokerFactory, edgeFactory);
        final EditingModalGraphMouse resourceMouse =
                new EditingModalGraphMouse(vv.getRenderContext(), resourceFactory, edgeFactory);
        vv.setGraphMouse(switchMouse);
        vv.addKeyListener(switchMouse.getModeKeyListener());
        switchMouse.setMode(ModalGraphMouse.Mode.EDITING);
        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1 / 1.1f, vv.getCenter());
            }
        });

        JPanel controls = new JPanel();
        controls.add(plus);
        controls.add(minus);
        JComboBox modeBox = switchMouse.getModeComboBox();
        controls.add(modeBox);
        this.add(controls, BorderLayout.SOUTH);
    }
}
