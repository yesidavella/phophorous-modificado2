/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.JobAckMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Nodes.Hybrid.Parallel.HybridSwitchImpl;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import Grid.Route;
import java.io.Serializable;
import java.util.List;
import simbase.Time;

/**
 *
 * @author Frank
 */
public class CostMultiMarkovAnalyzer implements Serializable {

    private GridSimulator simulator;
    private double Wtotal;//Suma total de Wsing+Wsw+Wb
    private double Wb;
    private double Wsign_0 = 0;
    private double Wsign_1 = 0;
    private double Wsw = 0;
    private double Ccap = 1; // Coeficciente de costo de ancho de banda por unidad de capacidad.
    private double W; //Capacidad de cada lambda.
    private double Hf; //Numero de saltos 
    private double T; // Tiempo de duracion de la solicitud. 
    ///Variables para costo de senializacion
    private double a = 1; // Accion sobre la capa lambda. 
    private double Csign = 0.5; //*Costo de señalizacion de la informacion a todos los nodos involucrados. 
    private double Ccomp = 0.5; //*Costo para recomputación de los caminos mas cortos entre par de nodos del camino de luz. Despues de la modificacion de la toplogia 
    private double Cfind = 1.5;//GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.findCommonWavelenght);  //Costo de busqueda de una longitud de onda comun hacer usada en la fibras.
    private double Callocate = 1; //GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.allocateWavelenght); // Costo de alojar la longitud de onda en el camino de luz        
    private double Cx = Csign + Ccomp;
    private double Cy = Cfind + Callocate;
    // Variable para costo de comutacion
    private double C_lambda = 0.35; //Coeficiente para la conmutacion opto-elect en el final de camino de luz 
    private double Copt = 0.25; //Coeficiete para la conmutacion de lamdaSP en los comutadores opticos de camino 
    private double Y;

    public CostMultiMarkovAnalyzer(GridSimulator simulator) {
        this.simulator = simulator;
    }

    public Double getCostP_LambdaOrCreateNewDirectOCS(
            HybridSwitchImpl firstSwicth,
            HybridSwitchImpl lastSwicth,
            JobMessage jobDummyMsg,
            JobAckMessage jobAckMessage,
            Time firstSwitchCurrentTime,
            PCE pce,
            PCE.OpticFlow opticFlow) {

        List<OCSRoute> ocsRoutes = null;
        Route hopRouteToDestination = simulator.getPhysicTopology().findOCSRoute(firstSwicth, lastSwicth);
        for (int i = hopRouteToDestination.size() - 2; i >= 1; i--) {

            Entity backwardHop = hopRouteToDestination.get(i);
            ocsRoutes = simulator.returnOcsCircuit(firstSwicth, backwardHop);

            if (ocsRoutes != null) {
                break;
            }
        }


        if (ocsRoutes != null) {

            for (OCSRoute ocsRoute : ocsRoutes) {
                Entity nextRealHop = ocsRoute.findNextHop(firstSwicth);
                GridOutPort theOutPort = firstSwicth.findOutPort(nextRealHop);
                int theOutgoingWavelength = ocsRoute.getWavelength();
               
                    jobDummyMsg.setWavelengthID(theOutgoingWavelength);
                    //Verifica si la primera OCS default tenga suficiente ancho de banda. 
                    if (pce.putMsgOnLinkTest(jobDummyMsg, theOutPort, firstSwitchCurrentTime, firstSwicth)) {
                        //FIXME: Verifica que exista ancho de banda por todos lo OCS default a lo largo de la ruta.

                        jobDummyMsg.setTypeOfMessage(GridMessage.MessageType.OCSMESSAGE);

                        //Costo de ancho de banda
                        W = theOutPort.getLinkSpeed();
                        Hf = hopRouteToDestination.size() - 2;
                        T = jobAckMessage.getRequestMessage().getJobSize() / pce.getBandwidthRequested();
                        Wb = Ccap * W * Hf * T;

                        //Costo de conmutacion                                 
                        Y = (((Hf - 1) * Copt) + C_lambda);

                        //Costo de señalizacion                                 
                        Wsign_0 = 0;
                        Wsign_1 = Cx + (Cy * Hf);

                        double Wsw_1 = Y * (opticFlow.getB_lambda() + opticFlow.getB_Fiber() + pce.getBandwidthRequested()) * T; // se toma la accion 
                        double Wsw_0 = ((Y * opticFlow.getB_lambda()) + (C_lambda * Hf * (opticFlow.getB_Fiber() + pce.getBandwidthRequested()))) * T;
                        
                        double Bth = getThresholdBetween(firstSwicth, lastSwicth, pce.getBandwidthRequested(), W, T, Cx, Cy, Ccap, C_lambda, Copt);
                        
                        //FIXME: TErminar la desicion .

                    
                }


            }
        }

        return null;
    }

    public Double getCostOCSDirect(
            HybridSwitchImpl firstSwicth,
            HybridSwitchImpl lastSwicth,
            JobMessage jobDummyMsg,
            JobAckMessage jobAckMessage,
            Time firstSwitchCurrentTime,
            PCE pce,
            PCE.OpticFlow opticFlow) {

        Route hopRouteToDestination = simulator.getPhysicTopology().findOCSRoute(firstSwicth, lastSwicth);
        List<OCSRoute> ocsRoutes = simulator.returnOcsCircuit(firstSwicth, lastSwicth);
        if (ocsRoutes != null) {
            //Buscar OCS directo con suficiente ancho de banda para enrutar b
            for (OCSRoute ocsRoute : ocsRoutes) {
                Entity nextRealHop = ocsRoute.findNextHop(firstSwicth);
                GridOutPort theOutPort = firstSwicth.findOutPort(nextRealHop);
                int theOutgoingWavelength = ocsRoute.getWavelength();
                if (theOutgoingWavelength == 0) {
                    continue; //Pasar a el siguiente OCS porque el OCS actual  es default
                }
                jobDummyMsg.setWavelengthID(theOutgoingWavelength);

                // Verificar si el OCS tiene  suficiente ancho de banda para enrutar b
                if (pce.putMsgOnLinkTest(jobDummyMsg, theOutPort, firstSwitchCurrentTime, firstSwicth)) {
                    //Costo de ancho de banda
                    W = theOutPort.getLinkSpeed();
                    Hf = hopRouteToDestination.size() - 2;
                    T = jobAckMessage.getRequestMessage().getJobSize() / pce.getBandwidthRequested();
                    Wb = Ccap * W * Hf * T;

                    //Costo de señalizacion  es 0 porque el OCS ya esta creado.                             
                    Wsign_0 = 0;

                    //Costo de conmutacion                                 
                    Y = (((Hf - 1) * Copt) + C_lambda);
                    //Costo de conmutacion  es con la accion 0 porque  se enruta por un OCS ya creado. 
                    double Wsw_0 = ((Y * opticFlow.getB_lambda()) + (C_lambda * Hf * (opticFlow.getB_Fiber() + pce.getBandwidthRequested()))) * T;

                    Wtotal = Wsign_0 + Wsw_0 + Wb;

                    return Wtotal;

                }

            }
        }
        return null;
    }

    public double getThresholdBetween(Entity source, Entity destination, double bandwidthRequested, double W, double T,
            double Cx, double Cy, double Ccap, double C_lambda, double Copt) {

        List<OCSRoute> ocsRoutes = null;
        Route hopRouteToDestination = simulator.getPhysicTopology().findOCSRoute(source, destination);

        //#############*Variables para determinar el limite##################*//
        int hF = hopRouteToDestination.size() - 1;//Numero total de fibras en el camino de luz.

        int β = 0;//Numero total de caminos de luz en Pλ q NO tienen suficiente ancho de banda para satisfacer la solicitud.
        int η = 0;//Numero total de caminos de luz en Pλ q tienen suficiente ancho de banda para satisfacer la solicitud.

        int hβF = 0;//Sumatoria de todas las fibras q pertenecen a β.
        int hηF = 0;//Sumatoria de todas las fibras q pertenecen a η.

        Time currentTimeSourceNode = source.getCurrentTime();
        Entity origin = source;

        //Busco salto a salto hacia atras buscando los OCS
        for (int i = hopRouteToDestination.size(); (i >= 2) && (origin != destination); i--) {

            Entity backwardHop = hopRouteToDestination.get(i - 1);
            ocsRoutes = simulator.returnOcsCircuit(origin, backwardHop);

            condOCSRoutes:
            if (ocsRoutes != null) {

                //Como puede haber mas de un OCS entre par de nodos miro cuales
                //NO tiene capacidad
                boolean foundOCSCantSupport = false;
                int βaux = 0;
                int hβFaux = 0;

                for (int routeIndex = 0; routeIndex < ocsRoutes.size(); routeIndex++) {

                    OCSRoute ocsRoute = ocsRoutes.get(routeIndex);

                    int wavelenghtStartOCS = ocsRoute.getWavelength();
                    Entity originNextHop = ocsRoute.findNextHop(origin);
                    GridOutPort outportToNextHop = origin.findOutPort(originNextHop);

                    if (bandwidthRequested <= origin.getFreeBandwidth(outportToNextHop, wavelenghtStartOCS, currentTimeSourceNode)) {

                        η++;//Un circuito mas
                        hηF += simulator.getPhysicTopology().findOCSRoute(origin, backwardHop).size() - 1;//Num de fibras

                        origin = backwardHop;
                        i = hopRouteToDestination.size() + 1;

                        break condOCSRoutes;

                    } else if (!foundOCSCantSupport) {

                        βaux++;//Un circuito mas
                        hβFaux = simulator.getPhysicTopology().findOCSRoute(origin, backwardHop).size() - 1;//Num de fibras

                        foundOCSCantSupport = true;
                    }

                    //Examino si es el ultimo OCS
                    if (routeIndex == ocsRoutes.size() - 1) {
                        β += βaux;
                        hβF += hβFaux;
                        i = hopRouteToDestination.size() + 1;
                        origin = backwardHop;
                    }
                }
            }
        }

        double thresholdNum = ((hF - hβF) * (Ccap * W * T)) - (Cx * (β - 1));
        double thresholdDiv = T * ((β + η - 1) * (C_lambda - Copt) + (Ccap * hηF));


        return thresholdNum / thresholdDiv;
    }
}
