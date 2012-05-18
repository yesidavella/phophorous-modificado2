/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes.Hybrid.Parallel;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.JobResultMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSSetupFailMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Interfaces.Switches.AbstractSwitch;
import Grid.OCS.OCSRoute;
import Grid.Port.GridInPort;
import Grid.Port.GridOutPort;
import Grid.Sender.Hybrid.Parallel.HybridSwitchSender;
import Grid.Sender.OBS.OBSSender;
import Grid.Sender.Sender;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.Stats.Logger;
import simbase.Stats.SimBaseStats.Stat;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OuputSwitchForHybridCase extends AbstractSwitch {

    private PrintStream writer;
    private Sender sender;
    /**
     * Formatter for the decimals.
     */
    private DecimalFormat format = new DecimalFormat();

    public OuputSwitchForHybridCase(String id, GridSimulator simulator) {
        super(id, simulator);
        sender = new HybridSwitchSender(this, simulator, wavelengthConversion);
        try {
            writer = new PrintStream("outputForHybridSwitch.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage m) throws StopException {
        super.receive(inPort, m);
        if (m instanceof OCSRequestMessage)
        {          
            handleOCSSetupMessage(inPort, (OCSRequestMessage) m);
        } else if (m instanceof OCSTeardownMessage) {
            handleTeardownMessage((OCSTeardownMessage) m, (GridInPort) inPort);
        } else if (m instanceof OCSSetupFailMessage) {
            handleOCSSetupFailMessage((OCSSetupFailMessage) m);
        } else {
            handleGridMessage(inPort, (GridMessage) m);
        }
    }

    public void handleOCSSetupFailMessage(OCSSetupFailMessage msg) {
        ((HybridSwitchSender) sender).handleOCSSetupFailMessage(msg);
    }

    public void handleTeardownMessage(OCSTeardownMessage msg, GridInPort port) {
        ((HybridSwitchSender) sender).handleTearDownOCSCircuit(msg, port);
    }

    /**
     * Will handle incoming grid messages which have nothing to do with 
     * OCS Path setup.
     * @param inport
     * @param m the message to forward.
     */
    private void handleGridMessage(SimBaseInPort inport, GridMessage m) {
        if (m instanceof JobMessage) {
            this.putLog(currentTime, m.getId(), Logger.BLACK, m.getSize(), m.getWavelengthID());
        }

        if (((HybridSwitchSender) sender).send(m, inport, currentTime)) {
            simulator.addStat(this, Stat.SWITCH_MESSAGE_SWITCHED);

            if (m.getTypeOfMessage() == GridMessage.MessageType.OBSMESSAGE) {

                simulator.putLog(currentTime, this.getId() + " OBS switched " + m.getId(), Logger.BLACK, m.getSize(), m.getWavelengthID());

            } else if (m.getTypeOfMessage() == GridMessage.MessageType.OCSMESSAGE) {
                simulator.putLog(currentTime, this.getId() + " OCS switched " + m.getId(), Logger.BLACK, m.getSize(), m.getWavelengthID());
            //putLog(currentTime, this.getId() + " OCS switched " + m.getId(), Logger.BLACK, m.getSize(), m.getWavelengthID());
            }
            if (m instanceof JobMessage) {
                Time t = new Time(currentTime.getTime() + m.getSize());
                //putLog(currentTime, "\tS\t" +t, Logger.BLACK, m.getSize(), m.getWavelengthID());
                simulator.addStat(this, Stat.SWITCH_JOBMESSAGE_SWITCHED);

            } else if (m instanceof JobResultMessage) {
                simulator.addStat(this, Stat.SWITCH_JOBRESULTMESSAGE_SWITCHED);

            }


        } else {
            dropMessage(m);

        }
    }

    private void handleOCSSetupMessage(SimBaseInPort inport, OCSRequestMessage m) {
        ((HybridSwitchSender) sender).handleOCSPathSetupMessage(m, inport);

    }

    public void requestOCSCircuit(OCSRoute ocsRoute, boolean permanent, Time time) {
        ((HybridSwitchSender) sender).requestOCSCircuit(ocsRoute, permanent, currentTime);
    }

    public void teardDownOCSCircuit(Entity ent, int wavelength, GridOutPort port, Time time) {
        simulator.putLog(currentTime, id + " is an OCS Switch and cannot tear down an OCS circuit on its own ", Logger.RED, -1, -1);
    }

    public Sender getSender() {
        return sender;
    }

    @Override
    public boolean supportsOBS() {
        return true;
    }

    @Override
    public boolean supportsOCS() {
        return true;
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        if (!inited) {
            super.init();
        }

    }

    public void route() {
        //sets the routingmap for this object
        OBSSender obs = (OBSSender) ((HybridSwitchSender) sender).getObsSender();
        obs.setRoutingMap(gridSim.getRouting().getRoutingTable(this));
    }

    public void putLog(Time time, String log, int color, double size, int wavelength) {
//        StringBuffer buffer = new StringBuffer();
//        buffer.append("<FONT COLOR=");
//        switch (color) {
//            case Logger.BLACK:
//                buffer.append("BLACK");
//                break;
//            case Logger.BLUE:
//                buffer.append("BLUE");
//                break;
//            case Logger.GREEN:
//                buffer.append("GREEN");
//                break;
//            case Logger.ORANGE:
//                buffer.append("ORANGE");
//                break;
//            case Logger.YELLOW:
//                buffer.append("YELLOW");
//                break;
//            case Logger.RED:
//                buffer.append("RED");
//                break;
//            case Logger.BROWN:
//                buffer.append("BROWN");
//                break;
//            }
//        buffer.append(">");
//        buffer.append(log);
//        buffer.append(" (");
//        buffer.append(wavelength);
//        buffer.append(")");
//        buffer.append(" (");
//        buffer.append(format.format(size));
//        buffer.append(")");
//
//        buffer.append("</FONT>");
//        writer.println(format.format(time.getTime()) + ": " + buffer.toString() +
//                "<br>");
        StringBuffer buffer = new StringBuffer();
        buffer.append(time);
        buffer.append("\t");
        buffer.append(wavelength);
        buffer.append("\t");
        buffer.append(log);
        writer.println(buffer.toString());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        writer.close();
    }
}
