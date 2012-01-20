
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Outputter;
import simbase.SimulationInstance;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Yesidsito
 */
public class PruebaYesidsito {

    private GridSimulator simulador;
    private SimulationInstance simulacion;
    //Nodo de usuario
    private ClientNode usuarioA1;
    private ClientNode usuarioA2;
    private ClientNode usuarioB1;
    private ClientNode usuarioB2;
    private ClientNode usuarioB3;
    //Nodos de recursos
    private ResourceNode recursoA;
    private ResourceNode recursoB1;
    private ResourceNode recursoB2;
    //Agendadores
    private ServiceNode agendadorA;
    private ServiceNode agendadorB;
    //Conmutadores
    private Switch conmutador1;
    private Switch conmutador2;
    private Switch conmutador3;
    private Switch conmutador4;
    private Switch conmutador5;
    private Switch conmutador6;
    private Switch conmutador7;
    //private Switch conmutador8;
    //private Switch conmutador9;

    public PruebaYesidsito() {

        simulacion = new GridSimulation("sources\\configFiles\\yesidsito.cfg");
        simulador = new GridSimulator();
        simulacion.setSimulator(simulador);
        
        agendadorA = Grid.Utilities.Util.createHybridServiceNode("agendadorA", simulador);
        agendadorB = Grid.Utilities.Util.createHybridServiceNode("agendadorB", simulador);
        
        usuarioA1 = Grid.Utilities.Util.createHybridClient("usuarioA1", simulador,agendadorA);
        usuarioA2 = Grid.Utilities.Util.createHybridClient("usuarioA2", simulador,agendadorA);

        usuarioB1 = Grid.Utilities.Util.createHybridClient("usuarioB1", simulador,agendadorB);
        usuarioB2 = Grid.Utilities.Util.createHybridClient("usuarioB2", simulador,agendadorB);
        usuarioB3 = Grid.Utilities.Util.createHybridClient("usuarioB3", simulador,agendadorB);

        recursoA = Grid.Utilities.Util.createHyridResourceNode("recursoA", simulador);
        recursoB1 = Grid.Utilities.Util.createHyridResourceNode("recursoB1", simulador);
        recursoB2 = Grid.Utilities.Util.createHyridResourceNode("recursoB2", simulador);

        conmutador1 = Grid.Utilities.Util.createHybridSwitch("conmutador1", simulador);
        conmutador2 = Grid.Utilities.Util.createHybridSwitch("conmutador2", simulador);
        conmutador3 = Grid.Utilities.Util.createHybridSwitch("conmutador3", simulador);
        conmutador4 = Grid.Utilities.Util.createHybridSwitch("conmutador4", simulador);
        conmutador5 = Grid.Utilities.Util.createHybridSwitch("conmutador5", simulador);
        conmutador6 = Grid.Utilities.Util.createHybridSwitch("conmutador6", simulador);
        conmutador7 = Grid.Utilities.Util.createHybridSwitch("conmutador7", simulador);
        //conmutador8 = Grid.Utilities.Util.createHybridSwitch("conmutador8", simulador);
        //conmutador9 = Grid.Utilities.Util.createHybridSwitch("conmutador9", simulador);
        //Conectamos el los elementos del clusterA a un conmutador
        
        Grid.Utilities.Util.createBiDirectionalLink(usuarioA1, conmutador1);
        Grid.Utilities.Util.createBiDirectionalLink(usuarioA2, conmutador1);
        Grid.Utilities.Util.createBiDirectionalLink(recursoA, conmutador1);
        Grid.Utilities.Util.createBiDirectionalLink(agendadorA, conmutador1);

        Grid.Utilities.Util.createBiDirectionalLink(usuarioB1, conmutador6);
        Grid.Utilities.Util.createBiDirectionalLink(usuarioB2, conmutador6);
        Grid.Utilities.Util.createBiDirectionalLink(usuarioB3, conmutador6);
        Grid.Utilities.Util.createBiDirectionalLink(recursoB1, conmutador6);
        Grid.Utilities.Util.createBiDirectionalLink(recursoB2, conmutador6);
        Grid.Utilities.Util.createBiDirectionalLink(agendadorB, conmutador6);

        //Generamos las conexiones entre conmutadores
        Grid.Utilities.Util.createBiDirectionalLink(conmutador1, conmutador2);
        Grid.Utilities.Util.createBiDirectionalLink(conmutador2, conmutador3);
        Grid.Utilities.Util.createBiDirectionalLink(conmutador2, conmutador4);
        Grid.Utilities.Util.createBiDirectionalLink(conmutador3, conmutador4);
        Grid.Utilities.Util.createBiDirectionalLink(conmutador3, conmutador5);
        Grid.Utilities.Util.createBiDirectionalLink(conmutador4, conmutador6);
        Grid.Utilities.Util.createBiDirectionalLink(conmutador5, conmutador6);
        Grid.Utilities.Util.createBiDirectionalLink(conmutador5, conmutador7);
        Grid.Utilities.Util.createBiDirectionalLink(conmutador7, conmutador6);

        simulador.route();
        simulador.initEntities();
        
        recursoA.addServiceNode(agendadorA);
        recursoB1.addServiceNode(agendadorB);
        recursoB2.addServiceNode(agendadorB);

        simulacion.run();
        
        Outputter escritorEnConsola = new Outputter(simulador);
        
        escritorEnConsola.printClient(usuarioA1);
        escritorEnConsola.printClient(usuarioA2);
        escritorEnConsola.printClient(usuarioB1);
        escritorEnConsola.printClient(usuarioB2);
        escritorEnConsola.printClient(usuarioB3);
        
        escritorEnConsola.printResource(recursoA);
        escritorEnConsola.printResource(recursoB1);
        escritorEnConsola.printResource(recursoB2);
        
        escritorEnConsola.printSwitch(conmutador1);
        escritorEnConsola.printSwitch(conmutador6);
    }
    
    public static void main(String[] args){
        new PruebaYesidsito();
    }
}
