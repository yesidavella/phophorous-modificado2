/*
 */
package simulator11;

import Distributions.DDNegExp;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Utilities.Config;
import Grid.Utilities.Config.ConfigEnum;
import Grid.Utilities.IllegalEdgeException;
import Grid.Utilities.Util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import simbase.SimulationInstance;
import simbase.Stats.SimBaseStats.Stat;
import simbase.Stop.ErlangStopper;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class ERLANGBServerTestForMoreServers {

    public static int[] cpus = {2,4,6,8,10,12,14,16,20,22,24};
    
    public static void main(String[] args) {
        System.out.println("erlang B Server test for more servers");

     for (int i = 1; i < cpus.length; i++) {
            System.gc();
            double occ = cpus[i]*0.5;
            SimulationInstance simInstance = new GridSimulation("erlangBServer.cfg");
            GridSimulator simulator = new GridSimulator();
            simInstance.setSimulator(simulator);

            ResourceNode resource = Grid.Utilities.Util.createOBSResource("resource", simulator,cpus[i],simInstance.configuration.getDoubleProperty(
                Config.ConfigEnum.defaultCapacity));
            ServiceNode serviceNode = Grid.Utilities.Util.createOBSServiceNode("broker", simulator);
            ClientNode client = Util.createOBSClient("Client", simulator, serviceNode);
            simInstance.setStopEntity(new ErlangStopper("erlangBStopper", simulator, new Time(10000), 10000, resource));
            Grid.Utilities.Util.createBiDirectionalLink(resource, client);
            Grid.Utilities.Util.createBiDirectionalLink(client, serviceNode);
            Grid.Utilities.Util.createBiDirectionalLink(serviceNode, resource);

            double mu = simInstance.configuration.getDoubleProperty(ConfigEnum.defaultFlopSize) /
                    simInstance.configuration.getDoubleProperty(ConfigEnum.defaultCapacity);
            mu = 1 / mu;
            double lambda = occ * mu;

            client.getState().setJobInterArrival(new DDNegExp(simulator, 1 / lambda));

            simulator.route();
            simulator.initEntities();
            resource.addServiceNode(serviceNode);
            simInstance.run();

            double jobsSend = simulator.getStat(client, Stat.CLIENT_JOB_SENT);
            double IAT = 1 / lambda;

            double erlang = Util.getErlang(IAT, 1 / mu, cpus[i]);
            double Fail_T = jobsSend * erlang;
            double Fail_res = simulator.getStat(resource, Stat.RESOURCE_FAIL_NO_FREE_PLACE);
            double percentage = Math.abs(100 - ((Fail_T / Fail_res) * 100));

            NumberFormat f = new DecimalFormat();
            f.setMaximumFractionDigits(3);

            System.out.println(f.format(((lambda) / (cpus[i]*mu))) + "\t" + f.format(Fail_T) + "\t" + f.format(Fail_res) + "\t" + f.format(percentage) + "\t" + f.format(jobsSend));
        }
    }

    
}
