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
import Grid.Utilities.Config.ConfigEnum;
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
public class OCSErlangBMorgeWavelengths {

    public static int[] wavelengths = {2, 4, 6, 8, 10, 12, 16, 20, 22, 24};

    public static void main(String[] args) {
        //System.out.println("OCS erlang More wavelengths Switch test");
        for (int i = 0; i < 1; i++) {
            SimulationInstance simInstance = new GridSimulation("OCSErlangMoreServer.cfg");
            GridSimulator simulator = new GridSimulator();
            simInstance.setSimulator(simulator);
            Switch sw = Util.createOCSSwitch("OCSswitch", simulator, 0.0, wavelengths[i]);
            simInstance.setStopEntity(new ErlangSwitchStopper("erlangswitchstopper", simulator, new Time(1000), "OCSswitch", 1000));
            double mu = simInstance.configuration.getDoubleProperty(ConfigEnum.defaultFlopSize) /
                    simInstance.configuration.getDoubleProperty(ConfigEnum.defaultCapacity);
            mu = 1 / mu;
            double occ = 0.5;
            double lambda = occ * mu;

            ServiceNode broker = new DummyServiceNode("broker", simulator);
            simulator.register(broker);

            ClientNode client = Util.createOCSClient("client", simulator, broker);
            client.getState().setJobInterArrival(new DDNegExp(simulator, 1 / lambda));


            try {
                Util.createLink(client, sw);
                Util.createLink(sw, broker);
            } catch (IllegalEdgeException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }

            GridOutPort port = sw.getOutport("OCSswitch-broker");
            port.setSwitchingSpeed(1);

            simulator.route();
            Grid.Utilities.Util.createOCSCircuit(client, broker, simulator,true);
            Grid.Utilities.Util.createOCSCircuit(client, broker, simulator,true);
            simulator.initEntities();
            simInstance.run();

            double erlang = Util.getErlang(1 / lambda, 1 / mu, wavelengths[i]);
            double Fail_S = simulator.getStat(Stat.SWITCH_MESSAGE_DROPPED);
            double Fail_T = erlang * simulator.getStat(client, Stat.CLIENT_REQ_SENT);

            double percentage = Math.abs(100 - ((Fail_T / Fail_S) * 100));

            NumberFormat f = new DecimalFormat();
            f.setMaximumFractionDigits(3);
            //System.out.println(f.format(((lambda) / (mu))) + "\t" + f.format(Fail_S) + "\t" + f.format(Fail_T) + "\t" + f.format(percentage) + "\t" + f.format(simulator.getStat(client, Stat.CLIENT_REQ_SENT)));
            System.gc();
        }
    }
}
