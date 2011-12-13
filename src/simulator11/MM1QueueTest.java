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
import Grid.Utilities.Config;
import Grid.Utilities.SampleAverage;
import Grid.Utilities.Util;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Jens Buysse
 */
public class MM1QueueTest {

    public static void main(String[] args) {
        for (int i = 1; i <= 10; i++) {
            GridSimulation simInstance = new GridSimulation("mm1.cfg");
            GridSimulator simulator = new GridSimulator();
            simInstance.setSimulator(simulator);
            ResourceNode resource = Grid.Utilities.Util.createOBSResource("resource", simulator);
            ServiceNode serviceNode = Grid.Utilities.Util.createOBSServiceNode("broker", simulator);
            ClientNode client = Util.createOBSClient("client", simulator, serviceNode);
            double occ = 0.1 * i;
            double mu = 1 / (GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.defaultFlopSize) /
                    GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.defaultCapacity));
            double lambda = occ * mu;
            client.getState().setJobInterArrival(new DDNegExp(simulator, 1 / lambda));

            Grid.Utilities.Util.createBiDirectionalLink(resource, serviceNode);
            Grid.Utilities.Util.createBiDirectionalLink(client, serviceNode);
            Grid.Utilities.Util.createBiDirectionalLink(client, resource);
            simulator.route();
            simulator.initEntities();
            resource.addServiceNode(serviceNode);
            simInstance.run();
            System.out.print(occ);
            System.out.print("\t");
            double IAT = lambda;

            double bezettingsGraad = IAT / mu;
            double sysTime = 1 / (mu - IAT);
            SampleAverage systemTime = resource.getSystemTime();
            sample(systemTime, 1.645, sysTime);

            double systemPop = bezettingsGraad / (1 - bezettingsGraad);
            sample(resource.getSystemPopulation(), 1.645, systemPop);
            System.out.println();
        }
    }

    private static void sample(SampleAverage avg, double CONFIDENCE, double expectedAverage) {
        NumberFormat f = new DecimalFormat();
        f.setMaximumFractionDigits(3);
        double sumSamples = avg.getTotalAverage();
        double totalNumberOfSamples = avg.getNrOfSamples();
        double sumSampleSquares = avg.getSquare();
        double avgSamples = sumSamples / totalNumberOfSamples;
        double varSamples = 1 / (totalNumberOfSamples - 1.0d) * (sumSampleSquares - avgSamples * avgSamples);
        double confIntervalWidth = CONFIDENCE * Math.sqrt(varSamples / totalNumberOfSamples);
        System.out.print(f.format(avg.getNrOfSamples()));
        System.out.print("\t");
        System.out.print(f.format(avgSamples));
        System.out.print("\t");
        System.out.print(f.format(expectedAverage));
        System.out.print("\t");

    }
}
