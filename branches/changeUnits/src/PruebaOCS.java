
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Outputter;
import simbase.SimulationInstance;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author Frank
 */
public class PruebaOCS {

    private SimulationInstance simulacion;
    private GridSimulator simulador;
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

   public static void main(String[] args){
        new PruebaOCS();
    }
    public PruebaOCS() 
    {
        simulacion = new GridSimulation("sources\\configFiles\\yesidsito.cfg");
        simulador = new GridSimulator();
        simulacion.setSimulator(simulador);

        agendadorA = Grid.Utilities.Util.createOCSServiceNode("agendadorA", simulador);
        agendadorA = Grid.Utilities.Util.createOCSServiceNode("agendadorB", simulador);
        agendadorB = Grid.Utilities.Util.createOCSServiceNode("agendadorC", simulador);

        usuarioA1 = Grid.Utilities.Util.createOCSClient("usuarioA1", simulador, agendadorA);
        usuarioA2 = Grid.Utilities.Util.createOCSClient("usuarioA2", simulador, agendadorA);

        usuarioB1 = Grid.Utilities.Util.createOCSClient("usuarioB1", simulador, agendadorB);
        usuarioB2 = Grid.Utilities.Util.createOCSClient("usuarioB2", simulador, agendadorB);
        usuarioB3 = Grid.Utilities.Util.createOCSClient("usuarioB3", simulador, agendadorB);

        recursoA = Grid.Utilities.Util.createOCSResource("recursoB1", simulador); 
        recursoB1 = Grid.Utilities.Util.createOCSResource("recursoB2", simulador);
        recursoB2 = Grid.Utilities.Util.createOCSResource("recursoB3", simulador);

        conmutador1 = Grid.Utilities.Util.createOCSSwitch("conmutador1", simulador, 20);
        conmutador2 = Grid.Utilities.Util.createOCSSwitch("conmutador2", simulador,20);
        conmutador3 = Grid.Utilities.Util.createOCSSwitch("conmutador3", simulador,20);
        conmutador4 = Grid.Utilities.Util.createOCSSwitch("conmutador4", simulador,20);
        conmutador5 = Grid.Utilities.Util.createOCSSwitch("conmutador5", simulador,20);
        conmutador6 = Grid.Utilities.Util.createOCSSwitch("conmutador6", simulador,20);
        conmutador7 = Grid.Utilities.Util.createOCSSwitch("conmutador7", simulador,20);
    

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

        //((ShortesPathRouting)simulador.getRouting()).getHyrbidNetwork().

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
}
