package Grid.Nodes;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Nodes.Hybrid.Parallel.HybridSwitchImpl;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import java.io.Serializable;
import java.util.ArrayList;
import simbase.Time;

public class CostMultiMarkovAnalyzer implements Serializable {

    private GridSimulator simulator;
    private double Wtotal;//Suma total de Wsing+Wsw+Wb
    private double Wb;
    private double Wsign_0 = 0;
    private double Wsign_1 = 0;
    private double Wsw = 0;
    private double Ccap = 1; // Coeficciente de costo de ancho de banda por unidad de capacidad.
    private double W; //Capacidad de cada lambda.
    private int Hf; //Numero de saltos.
    private double T; // Tiempo de duracion de la solicitud. 
    ///Variables para costo de senializacion
    private double a = 1; // Accion sobre la capa lambda. 
    private double Csign = 0.4; //*Costo de señalizacion de la informacion a todos los nodos involucrados. 
    private double Ccomp = 0.6; //*Costo para recomputación de los caminos mas cortos entre par de nodos del camino de luz. Despues de la modificacion de la toplogia 
    private double Cfind = 50;//GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.findCommonWavelenght);  //Costo de busqueda de una longitud de onda comun hacer usada en la fibras.
    private double Callocate = 50; //GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.allocateWavelenght); // Costo de alojar la longitud de onda en el camino de luz        
    private double Cx = Csign + Ccomp;
    private double Cy = Cfind + Callocate;
    // Variable para costo de comutacion
    private double C_lambda = 0.35; //Coeficiente para la conmutacion opto-elect en el final de camino de luz 
    private double Copt = 0.25; //Coeficiete para la conmutacion de lamdaSP en los comutadores opticos de camino 
    private double Y;
    private Integer acciontaken = null;
    private double B_total;

    public CostMultiMarkovAnalyzer(GridSimulator simulator) {
        this.simulator = simulator;
    }

    public int getAcciontaken() {
        if (acciontaken == null) {
            throw new IllegalStateException("The Multi Markov Analyzer has not taken accion yet");
        }

        return acciontaken.intValue();
    }

    public Double getCostP_LambdaOrCreateNewDirectOCS(HybridSwitchImpl firstSwicth,
            HybridSwitchImpl lastSwicth, ArrayList<OCSRoute> ocsSupportRequest, ArrayList<OCSRoute> ocsNotSupportRequest,
            Time firstSwitchCurrentTime, PCE.OpticFlow opticFlow,
            double bandwidthRequested, double messagerSize) {

        GridOutPort gridOutPort = PCE.getGridOutPort(firstSwicth, lastSwicth);

        //Costo de ancho de banda
        W = gridOutPort.getLinkSpeed();
        Hf = simulator.getPhysicTopology().getNrOfHopsBetween(firstSwicth, lastSwicth) + 1;
        T = messagerSize / bandwidthRequested;
        Wb = Ccap * W * Hf * T;

        //Costo de conmutacion                                 
        Y = (((Hf - 1) * Copt) + C_lambda);

        double Bth = getThresholdBetween(firstSwicth, lastSwicth, ocsSupportRequest, ocsNotSupportRequest, bandwidthRequested, W, T);

        //FIXME: TErminar la desicion .

        B_total = opticFlow.getB_Fiber() + opticFlow.getB_lambda() + bandwidthRequested;

        if (B_total > Bth) {
            acciontaken = 1;
            double Wsw_1 = Y * (opticFlow.getB_lambda() + opticFlow.getB_Fiber() + bandwidthRequested) * T; // se toma la accion 
            Wsign_1 = Cx + (Cy * Hf);
            Wtotal = Wsign_1 + Wsw_1 + Wb;
            //   System.out.println("Costo accion 1 Wb:"+Wb+" Wsign_1:"+Wsign_1+" Wsw_1:"+Wsw_1+" Total:"+Wtotal);

            return Wtotal;
        } else {
            acciontaken = 0;
            double Wsw_0 = ((Y * opticFlow.getB_lambda()) + (C_lambda * Hf * (opticFlow.getB_Fiber() + bandwidthRequested))) * T;
            Wsign_0 = 0;
            Wtotal = Wsign_0 + Wsw_0 + Wb;
            //  System.out.println("Costo accion 0 Wb:"+Wb+" Wsign_0:"+Wsign_0+" Wsw_0:"+Wsw_0+" Total:"+Wtotal);

            return Wtotal;
        }
    }

    public Double getCostOCSDirect(OCSRoute directOCS, Time firstSwitchCurrentTime, double bandwidthRequested, PCE.OpticFlow opticFlow, double messageSize) {

        Entity ocsSource = directOCS.getSource();
//        int wavelenghtStartsOCS = directOCS.getWavelength();
        Entity nextHop = directOCS.findNextHop(ocsSource);
        GridOutPort outportToNextHop = ocsSource.findOutPort(nextHop);

        //Costo de ancho de banda
        W = outportToNextHop.getLinkSpeed();
        Hf = simulator.getPhysicTopology().getNrOfHopsBetween(ocsSource, directOCS.getDestination()) + 1;
        T = messageSize / bandwidthRequested;
        Wb = Ccap * W * Hf * T;

        //Costo de señalizacion es 0 porque el OCS ya esta creado.                             
        Wsign_0 = 0;

        //Costo de conmutacion                                 
        Y = (((Hf - 1) * Copt) + C_lambda);
        //Costo de conmutacion  es con la accion 0 porque  se enruta por un OCS ya creado. 
        double Wsw_0 = ((Y * opticFlow.getB_lambda()) + (C_lambda * Hf * (opticFlow.getB_Fiber() + bandwidthRequested))) * T;

        Wtotal = Wsign_0 + Wsw_0 + Wb;

        //    System.out.println("Costo directo Wb:"+Wb+" Wsign_0:"+Wsign_0+" Wsw_0:"+Wsw_0+" Total:"+Wtotal);

        return Wtotal;

    }

    public Double getCostOCSDirectToCreate(
            HybridSwitchImpl firstSwicth,
            HybridSwitchImpl lastSwicth,
            Time firstSwitchCurrentTime,
            PCE pce,
            PCE.OpticFlow opticFlow,
            double bandwidthRequested,
            double messageSize) {

        GridOutPort gridOutPort = PCE.getGridOutPort(firstSwicth, lastSwicth);

        //Costo de ancho de banda
        W = gridOutPort.getLinkSpeed();
        Hf = simulator.getPhysicTopology().getNrOfHopsBetween(firstSwicth, lastSwicth) + 1;
        T = messageSize / bandwidthRequested;
        Wb = Ccap * W * Hf * T;

        Y = (((Hf - 1) * Copt) + C_lambda);

        double Wsw_1 = Y * (opticFlow.getB_lambda() + opticFlow.getB_Fiber() + bandwidthRequested) * T; // se toma la accion 
        Wsign_1 = Cx + (Cy * Hf);
        Wtotal = Wsign_1 + Wsw_1 + Wb;

        return Wtotal;

    }

    public double getThresholdBetween(Entity source, Entity destination, ArrayList<OCSRoute> ocsSupportRequest,
            ArrayList<OCSRoute> ocsNotSupportRequest, double bandwidthRequested, double W, double T) {

        //#############*Variables para determinar el limite##################*//
        int hF = simulator.getPhysicTopology().getNrOfHopsBetween(source, destination) + 1;//Numero total de fibras en el camino de luz.

        int β = 0;//Numero total de caminos de luz en Pλ q NO tienen suficiente ancho de banda para satisfacer la solicitud.
        int η = 0;//Numero total de caminos de luz en Pλ q tienen suficiente ancho de banda para satisfacer la solicitud.

        int hβF = 0;//Sumatoria de todas las fibras q pertenecen a β.
        int hηF = 0;//Sumatoria de todas las fibras q pertenecen a η.

        for (OCSRoute ocSupport : ocsSupportRequest) {
            η++;
            hηF += simulator.getPhysicTopology().getNrOfHopsBetween(ocSupport.getSource(), ocSupport.getDestination()) + 1;//Cuento el numero de fibras
        }

        for (OCSRoute ocNotSupport : ocsNotSupportRequest) {
            β++;
            hβF += simulator.getPhysicTopology().getNrOfHopsBetween(ocNotSupport.getSource(), ocNotSupport.getDestination()) + 1;//Cuento el numero de fibras
        }

        double thresholdNum = ((hF - hβF) * ((Ccap * W * T) + Cy)) - (Cx * (β - 1));
        double thresholdDiv = T * ((β + η - 1) * (C_lambda - Copt) + (Ccap * hηF));

        return thresholdNum / thresholdDiv;
    }
    
    /**
     * This Signaling cost incurred if the ocs would be created.
     * @param source The source of the ocs.
     * @param destination The destination of the ocs.
     * @return cost of signaling a ocs.
     */
    public double getSignalingCost(Entity source, Entity destination){
        
        int Hfaux = simulator.getPhysicTopology().getNrOfHopsBetween(source, destination) + 1;
        double Wsign_1aux = Cx + (Cy * Hfaux);
        
        return Wsign_1aux;
    }
}
