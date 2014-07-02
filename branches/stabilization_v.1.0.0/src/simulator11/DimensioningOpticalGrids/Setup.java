
package simulator11.DimensioningOpticalGrids;


import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.ServiceNode;
import Grid.Interfaces.Switch;
import Grid.Utilities.HopToHopLinks;
import Grid.Utilities.KMeans.Centroid;
import Grid.Utilities.KMeans.KMeans;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import simbase.SimulationInstance;
import simbase.Stats.SimBaseStats;
import simbase.Stop.LoadStopper;
import simbase.Time;

/**
 *
 * This class is the setup used for the dimensioning problem
 * @author Jens Buysse
 */
public class Setup {

    public static String[] Ses = {"Dublin", "Glasgow", "Oslo", "Stockholm",
        "Amsterdam", "Hamburg", "Copenhagen", "Warsaw", "Budapest", "Belgrade", "Athens",
        "Rome", "Milan", "Zurich", "Lyon", "Barcelona", "Madrid", "Bordeaux", "Paris",
        "London", "Brussels", "Strasbourg", "Munich", "Vienna", "Prague", "Berlin", "Frankfurt",
        "Zagreb"
    };
    /**
     * The simulator instance
     */
    private SimulationInstance simInstance;
    /**
     * The simulator itself
     */
    private GridSimulator simulator;

    public Setup() {

        simInstance = new GridSimulation("configFiles//Dimensioning.cfg");
        simulator = new GridSimulator();

        simInstance.setSimulator(simulator);
        HopToHopLinks creator = new HopToHopLinks(simulator);

        //Creation of resourcebroker
        ServiceNode broker = Grid.Utilities.Util.createOBSServiceNode("broker", simulator);

        LoadStopper stopper = new LoadStopper("Load stopper for " + broker.getID(), simulator, new Time(1), 8000000, broker);
        simInstance.setStopEntity(stopper);
        //Creation of the network
        //Screation
        for (int i = 0; i < this.Ses.length; i++) {
            Switch s = Grid.Utilities.Util.createOBSSwitch("switch", simulator, true);
            Switch sw = Grid.Utilities.Util.createOCSSwitch(Ses[i] + "_S", simulator, 0.2);
        //Client client = Grid.Utilities.Util.createOCSClient(Ses[i] + "_client", simulator, broker);
        //Grid.Utilities.Util.createBiDirectionalLink(sw, client);
        }
        //Bidirectional link creation

        creator.addBidirectionLinkFromString("Madrid_S", "Bordeaux_S");
        creator.addBidirectionLinkFromString("Bordeaux_S", "Paris_S");
        creator.addBidirectionLinkFromString("Paris_S", "London_S");
        creator.addBidirectionLinkFromString("London_S", "Dublin_S");
        creator.addBidirectionLinkFromString("Dublin_S", "Glasgow_S");
        creator.addBidirectionLinkFromString("Glasgow_S", "Amsterdam_S");
        creator.addBidirectionLinkFromString("Amsterdam_S", "Hamburg_S");
        creator.addBidirectionLinkFromString("Hamburg_S", "Copenhagen_S");
        creator.addBidirectionLinkFromString("Copenhagen_S", "Oslo_S");
        creator.addBidirectionLinkFromString("Oslo_S", "Stockholm_S");
        creator.addBidirectionLinkFromString("Stockholm_S", "Warsaw_S");
        creator.addBidirectionLinkFromString("Warsaw_S", "Budapest_S");
        creator.addBidirectionLinkFromString("Budapest_S", "Belgrade_S");
        creator.addBidirectionLinkFromString("Belgrade_S", "Athens_S");
        creator.addBidirectionLinkFromString("Athens_S", "Rome_S");
        creator.addBidirectionLinkFromString("Rome_S", "Milan_S");
        creator.addBidirectionLinkFromString("Milan_S", "Zurich_S");
        creator.addBidirectionLinkFromString("Zurich_S", "Lyon_S");
        creator.addBidirectionLinkFromString("Lyon_S", "Barcelona_S");
        creator.addBidirectionLinkFromString("Barcelona_S", "Madrid_S");
        creator.addBidirectionLinkFromString("Paris_S", "Lyon_S");
        creator.addBidirectionLinkFromString("Paris_S", "Strasbourg_S");
        creator.addBidirectionLinkFromString("Strasbourg_S", "Zurich_S");
        creator.addBidirectionLinkFromString("Strasbourg_S", "Frankfurt_S");
        creator.addBidirectionLinkFromString("Frankfurt_S", "Munich_S");
        creator.addBidirectionLinkFromString("Munich_S", "Milan_S");
        creator.addBidirectionLinkFromString("Munich_S", "Vienna_S");
        creator.addBidirectionLinkFromString("Vienna_S", "Zagreb_S");
        creator.addBidirectionLinkFromString("Zagreb_S", "Belgrade_S");
        creator.addBidirectionLinkFromString("Zagreb_S", "Rome_S");
        creator.addBidirectionLinkFromString("Vienna_S", "Prague_S");
        creator.addBidirectionLinkFromString("Prague_S", "Budapest_S");
        creator.addBidirectionLinkFromString("Prague_S", "Berlin_S");
        creator.addBidirectionLinkFromString("Berlin_S", "Warsaw_S");
        creator.addBidirectionLinkFromString("Berlin_S", "Hamburg_S");
        creator.addBidirectionLinkFromString("Berlin_S", "Munich_S");
        creator.addBidirectionLinkFromString("Paris_S", "Brussels_S");
        creator.addBidirectionLinkFromString("Brussels_S", "Amsterdam_S");
        creator.addBidirectionLinkFromString("Brussels_S", "Frankfurt_S");

        //Creation of the Resources

//        simulator.route();
//        //KMeans kmeans = new KMeans(6, simulator.getRouting().getOCSNetwork(), simulator.getRouting().getOCSNetworkRouting(), 200);
//        //kmeans.startAnalysis();
//
//        //List<Centroid> resources = kmeans.getCentroids(kmeans.getClusters());
//        //Iterator<Centroid> resourceIterator = resources.iterator();
//        while (resourceIterator.hasNext()) {
//            Centroid centroid = resourceIterator.next();
//            String resourceName = centroid.getEntity().substring(0, centroid.getEntity().indexOf("_"));
//            Grid.Utilities.Util.createOCSResource(resourceName + "_R", simulator);
//            creator.addBidirectionLinkFromString(resourceName + "_R", resourceName + "_S");
//        }
//        simulator.getRouting().clear();
//        simulator.route();



    }

    private void print(SimBaseStats.Stat stat) {
        DecimalFormat format = new DecimalFormat();
        //System.out.print(stat.toString());
        //System.out.print("\t");
        //System.out.print(format.format(simulator.getStat(stat)));
        //System.out.println();
    }

    public static void main(String[] args) {
        new Setup();
    }
}
