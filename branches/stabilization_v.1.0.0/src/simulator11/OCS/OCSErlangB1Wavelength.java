/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator11.OCS;

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
public class OCSErlangB1Wavelength {

    public static double[] occ = {1, 2, 3, 4, 5, 5, 6, 7, 8, 9, 10};

    public static void main(String[] args) {
        //System.out.println("Ocs erlang 1 Wave switch test");
        for (int i = 0; i < occ.length; i++) {
            SimulationInstance simInstance = new GridSimulation("ocsErlang.cfg");
            double mu = 1 / GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.ACKsize);
            GridSimulator simulator = new GridSimulator();
            simInstance.setSimulator(simulator);
            Switch sw = Util.createOCSSwitch("OCSSwitch", simulator, 0.0);
            simInstance.setStopEntity(new ErlangSwitchStopper("erlangswitchstopper", simulator, new Time(1000), "OCSSwitch", 10000));
            ServiceNode broker = new DummyServiceNode("broker", simulator);
            simulator.register(broker);

            ClientNode client = Util.createOCSClient("client", simulator, broker);
            client.getState().setJobInterArrival(new DDNegExp(simulator, 1 / (occ[i] * mu)));



//            Util.createBiDirectionalLink(sw, client);
//            Util.createBiDirectionalLink(sw, broker);
            try {
                Util.createLink(client, sw);
                Util.createLink(sw, broker);
            } catch (IllegalEdgeException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }


            GridOutPort port = sw.getOutport("OCSSwitch-broker");
            port.setSwitchingSpeed(1);
            simulator.route();
            Util.createOCSCircuit(client, broker, simulator,true);
            simulator.initEntities();
            simInstance.run();
            double lambda = occ[i] * mu;

            double erlang = Util.getErlang(1 / lambda, 1 / mu, 1);
            double Fail_Sim = simulator.getStat(Stat.SWITCH_MESSAGE_DROPPED);
            double Fail_Erlang = erlang * simulator.getStat(client, Stat.CLIENT_REQ_SENT);

            double percentage = Math.abs(100 - ((Fail_Erlang / Fail_Sim) * 100));

            NumberFormat f = new DecimalFormat();
            f.setMaximumFractionDigits(3);
            //System.out.print(f.format(lambda / mu));
            //System.out.print("\t");
            //System.out.print(f.format(Fail_Sim));
            //System.out.print("\t");
            //System.out.print(f.format(Fail_Erlang));
            //System.out.print("\t");
            //System.out.print(f.format(percentage));
            //System.out.print("\t");
            //System.out.print(f.format(simulator.getStat(client, Stat.CLIENT_REQ_SENT)));
            //System.out.println();
            System.gc();
        }
    }
}
