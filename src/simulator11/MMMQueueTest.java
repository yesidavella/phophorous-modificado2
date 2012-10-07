/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator11;

import Distributions.DDNegExp;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Nodes.Listeners.MMMListener;
import Grid.Utilities.Config.ConfigEnum;
import Grid.Utilities.Util;
import java.text.DecimalFormat;
import simbase.SimulationInstance;
import simbase.Stats.SimBaseStats.Stat;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class MMMQueueTest {

    public static int[] servers = {2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24};

    public static void main(String[] args) {
        //System.out.println("MM Queue Test");
        for (int i = 0; i < servers.length; i++) {
            GridSimulator simulator;
            SimulationInstance simInstance;
            ServiceNode serviceNode;
            ClientNode client;
            int nrOfCpus = servers[i];
            MMMListener listener;
            simInstance = new GridSimulation("mm1.cfg");
            simulator = new GridSimulator();
            simInstance.setSimulator(simulator);
            double mu = simInstance.configuration.getDoubleProperty(ConfigEnum.defaultFlopSize) /
                    simInstance.configuration.getDoubleProperty(ConfigEnum.defaultCapacity);
            mu = 1 / mu;
            double occ = 0.5;
            double lambda = occ * servers[i] * mu;
            serviceNode = Grid.Utilities.Util.createOBSServiceNode("broker", simulator);
            client = Util.createOBSClient("client", simulator, serviceNode);
            client.getState().setJobInterArrival(new DDNegExp(simulator, 1 / lambda));
            ResourceNode resource = Grid.Utilities.Util.createOBSResource("resource", simulator, servers[i],
                    simInstance.configuration.getDoubleProperty(ConfigEnum.defaultCapacity));

            listener = new MMMListener(resource);
            resource.addChangeListener(listener);
            Grid.Utilities.Util.createBiDirectionalLink(resource, serviceNode);
            Grid.Utilities.Util.createBiDirectionalLink(client, resource);
            Grid.Utilities.Util.createBiDirectionalLink(client, serviceNode);

            simulator.route();
            simulator.initEntities();
            resource.addServiceNode(serviceNode);




            //occupation degree
            double occupation = lambda / (nrOfCpus * mu);
            simInstance.run();


            double systemPopulation = listener.getSystemPopulation().getAverage();


            //Calculate p0
            double p0 = 0;
            double summ = 0;
            for (int k = 0; k <= nrOfCpus - 1; k++) {
                double firstClause = Math.pow(nrOfCpus * occupation, k);
                firstClause = firstClause / Grid.Utilities.Util.fac(k);
                summ += firstClause;

            }
            double rightPart = Math.pow(nrOfCpus * occupation, nrOfCpus);
            double rightUnder = Grid.Utilities.Util.fac(nrOfCpus) * (1 - occupation);
            rightPart = rightPart / rightUnder;

            p0 = summ + rightPart;
            p0 = 1 / p0;

            //Calculte the the average number of clients in the system
            double firstClause = nrOfCpus * occupation;
            double secondClause = p0 * occupation * (Math.pow(nrOfCpus * occupation, nrOfCpus));
            double under = Grid.Utilities.Util.fac(nrOfCpus) * Math.pow((1 - occupation), 2);
            secondClause = secondClause / under;

            double analyticalPopulation = firstClause + secondClause;

            //Check with simulator population
            double percentage = Math.abs(100 - ((analyticalPopulation / systemPopulation) * 100));
            double jobsSend = simulator.getStat(client, Stat.CLIENT_JOB_SENT);
            DecimalFormat f = new DecimalFormat();
            f.setMaximumFractionDigits(3);

            //System.out.print(f.format(servers[i]));
            //System.out.print("\t");
            //System.out.print(f.format(occupation));
            //System.out.print("\t");
            //System.out.print(f.format(analyticalPopulation));
            //System.out.print("\t");
            //System.out.print(f.format(systemPopulation));
            //System.out.print("\t");
            //System.out.print(f.format(percentage));
            //System.out.print("\t");
            //System.out.print(f.format(jobsSend));
            //System.out.println();
        }
    }
}
