/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator11.OBS;

import Distributions.DDNegExp;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Nodes.DummyServiceNode;
import Grid.Port.GridOutPort;
import Grid.Utilities.Config;
import Grid.Utilities.IllegalEdgeException;
import Grid.Utilities.Util;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import simbase.SimulationInstance;
import simbase.Stats.SimBaseStats.Stat;
import simbase.Stop.ErlangSwitchStopper;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class OBSErlanB1WaveSwitchTest {

    public static double[] IATS = {1, 2, 3, 4, 5, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    public static void main(String[] args) {
        //System.out.println("OBS erlang 1 Wave switch test");
        for (int i = 0; i < IATS.length; i++) {
            SimulationInstance simInstance = new GridSimulation("OBSErlang.cfg");
            GridSimulator simulator = new GridSimulator();
            simInstance.setSimulator(simulator);
            simInstance.setStopEntity(new ErlangSwitchStopper("erlangswitchstopper", simulator, new Time(10),"OBSSwitch",10000));
            
            ServiceNode broker = new DummyServiceNode("broker", simulator);
            simulator.register(broker);
            
            ClientNode client = Util.createOBSClient("client", simulator, broker);
            client.getState().setJobInterArrival(new DDNegExp(simulator, IATS[i]));
            
            Switch sw = Util.createOBSSwitch("OBSswitch", simulator,true);
            
//            Util.createBiDirectionalLink(sw, client);
//            Util.createBiDirectionalLink(sw, broker);
            try{
            Util.createLink(client,sw );
            Util.createLink(sw, broker);
            }
            catch(IllegalEdgeException e){
                System.err.println(e.getMessage());
                System.exit(1);
            }
            
            GridOutPort port = sw.getOutport("OBSswitch-broker");
            port.setSwitchingSpeed(1);
            simulator.route();
            simulator.initEntities();
            simInstance.run();
            double IAT = IATS[i];
            double mu = GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.ACKsize);
            double erlang = Util.getErlang(IAT, mu, 2);
            double Fail_S = simulator.getStat(Stat.SWITCH_MESSAGE_DROPPED);
            double Fail_T = erlang * simulator.getStat(client, Stat.CLIENT_REQ_SENT);

            double percentage = Math.abs(100 - ((Fail_T / Fail_S) * 100));

            NumberFormat f = new DecimalFormat();
            f.setMaximumFractionDigits(3);
            //System.out.println(f.format(((1 / IAT) / (1 / mu)))+"\t"+f.format(Fail_S) + "\t" + f.format(Fail_T) + "\t" + f.format(percentage)+"\t"
                    //+ f.format(simulator.getStat(client, Stat.CLIENT_REQ_SENT)));
            System.gc();
        }
    }
}
