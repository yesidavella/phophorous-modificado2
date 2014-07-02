package Grid.Sender.Hybrid.Parallel;

import Grid.Entity;
import Grid.GridSimulation;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.OCSConfirmSetupMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.OCS.OCSRoute;
import Grid.Port.GridOutPort;
import Grid.Sender.OBS.OBSEndSender;
import Grid.Sender.OCS.OCSEndSender;
import Grid.Utilities.Config;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class HyrbidEndSender extends AbstractHybridSender {

    /**
     * Constructor
     * @param owner The owner of this sender.
     */
    public HyrbidEndSender(Entity owner, GridSimulator simulator) {
        super(owner, simulator);
        this.simulator = simulator;
        ocsSender = new OCSEndSender(simulator, owner, 5*GridSimulation.configuration.getDoubleProperty(Config.ConfigEnum.OCSSetupHandleTime));
        obsSender = new OBSEndSender(simulator, owner);
    }

    public boolean sendPureOBS(GridMessage message, Time t, Entity destination) {
        return obsSender.send(message, t, true);
    }

    public boolean sendPureOCS(GridMessage message, Time t, Entity destination) {
        return ocsSender.send(message, t, true);
    }

    public boolean handleOCSSetup(OCSRequestMessage msg, Entity entity) {
        return ((OCSEndSender) ocsSender).handleOCSSetup(msg, entity);
    }

    /**
     * @param message The message destination send.
     * @param source The source which sends the message.
     * @param destination The destination where destination send the message.
     * @return true if sending worked, false if not...
     */
    
     HashSet<String> strings = new HashSet<String>(); 
    public boolean send(GridMessage message, Time t, boolean outputFail) {
        
        Entity destination = message.getDestination();
        if (message.getWavelengthID() == -1) {
            //Control plane
            return owner.sendNow(destination, message);
        }

        //Check whether the destination can be reached via hybrid sending
        Map routingMap = ((OBSEndSender) obsSender).getRoutingMap();
        if (routingMap.containsKey(destination.getId())) {
            GridOutPort port = (GridOutPort) routingMap.get(destination.getId());
            Entity nextVirtualHop = (Entity) (port.getTarget().getOwner());
            //Sending is possible
            
            Map linkMapping = ((OCSEndSender) ocsSender).getLinkMapping();
            //NOTA: PARA EL IF SI Y SOLO SI SE CREA UN ocs ENTRE FINALES
            String aviso = "linkMapping "+linkMapping+" destination " +destination + " nextVirtualHop " +nextVirtualHop+" owner: "+ocsSender.getOwner(); 
            if(!strings.contains(aviso))
            {   
                strings.add(aviso); 
//                //System.out.println(aviso); 
            }
           
           // if (linkMapping.containsKey(nextVirtualHop)) {
            //FIXME: cambio para pruebas
             if (linkMapping.containsKey(nextVirtualHop)) {
//                //System.out.println("  endSender - owner: "+ocsSender.getOwner()+" -Destination : " + destination);
                //we have an OCS circuit directly to the next Virtual hop so use it !
                // it is possible that sending on the circuit did not work,
                // so let's give it a last effort trying OBS
                if (ocsSender.send(message, t, true)) {
                    return true;
                } else {
                    //Cannot use obsSender because of discrepancies in routing map
                    List<OCSRoute> routes = simulator.returnOcsCircuit(message.getSource(), message.getDestination());
                    if (routes != null && !routes.isEmpty()) {


                        OCSRoute route = routes.get(0);
                        int index = route.indexOf(message.getSource());
                        if (index < route.size()) {
                            Entity nextHopOnPath = route.get(index + 1);
                            return ((OBSEndSender) obsSender).send(message, t, outputFail, nextHopOnPath);
                        } else {
                            return false;
                        }


                    } else {
                        return false;
                    }
                }
            } else {
                return obsSender.send(message, t, true);
            }
        } else {
            return false;
        }
    }

    /**
     * Will send an OCSRequestMessage to setup an OCS-circuit. 
     * @param ocsRoute The hops used on the path.
     */
    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        ((OCSEndSender) ocsSender).requestOCSCircuit(ocsRoute, permanent, time);
    }

    public boolean tearDownOCSCircuit(Entity destination, int wavelength, GridOutPort outport, Time time) {
        return ((OCSEndSender) ocsSender).tearDownOCSCircuit(destination, wavelength, outport, time);
    }

    public void handleOCScircuitTearDown(OCSTeardownMessage msg) {
        ((OCSEndSender) ocsSender).handleOCScircuitTearDown(msg);
    }

    public boolean handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        return ((OCSEndSender) ocsSender).handleOCSSetupFailMessage(msg);
    }

    public void handleConfirmMessage(OCSConfirmSetupMessage msg) {
        ((OCSEndSender) ocsSender).handleConfirmMessage(msg);
    }
}
