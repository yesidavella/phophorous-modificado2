package Grid.Nodes;

import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.Messages.GeneratorMessage;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.JobAckMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.JobRequestMessage;
import Grid.Interfaces.Messages.JobResultMessage;
import Grid.Interfaces.ServiceNode;
import Grid.Sender.Sender;
import java.util.ArrayList;
import java.util.List;
import simbase.Port.SimBaseInPort;
import simbase.Stats.Logger;
import simbase.Stats.SimBaseStats.Stat;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public abstract class AbstractClient extends ClientNode {

    /**
     * The ServideNode of this client.
     */
    protected ServiceNode broker;
    /**
     * The list containing all job request that were send by this client.
     */
    protected List<JobRequestMessage> activeJobRequests = new ArrayList();
    /**
     * The sender for this client.
     */
    protected Sender sender;

    /**
     * Constructor of the client class.
     *
     * @param id The id of this Entity (ClientOBSImpl)
     * @param simulator The simulator to which is belongs.
     */
    public AbstractClient(String id, GridSimulator simulator) {
        super(id, simulator);
    }

    /**
     * Constructor of the client class.
     *
     * @param id The id of this Entity (ClientOBSImpl)
     * @param simulator The simulator to which is belongs.
     */
    public AbstractClient(String id, GridSimulator simulator, ServiceNode broker) {
        super(id, simulator);
        this.broker = broker;

    }

    /**
     * Sends out a job, from the current job state, to the resource broker which
     * task is to shedule the jobs.
     */
    @Override
    public void sendJob() {


        //Make job request 
        JobRequestMessage job = state.generateJob(this, id + "-job_" + JobRequestMessage.jobCounter++,
                new Time(currentTime.getTime()));
        //simulator.putLog(currentTime, "New job request created at " + job.getSource() + " : " + job.getId(), Logger.BLUE, job.getSize(), job.getWavelengthID());
        job.setDestination(broker);
        job.addHop(this);
        simulator.addStat(this, Stat.CLIENT_CREATED_REQ);
        // send out the job request
        if (sender.send(job, currentTime, true)) {
            activeJobRequests.add(job);
            if (job.getTypeOfMessage() == GridMessage.MessageType.OBSMESSAGE) {
                // simulator.putLog(currentTime, "---> Job request (OBS) " + job.getId() + " sent to " + job.getDestination().getId() + " by " + job.getSource(), Logger.BLUE, job.getSize(), job.getWavelengthID());
            } else {
                //simulator.putLog(currentTime, "---> Job request (OCS) " + job.getId() + " sent to " + job.getDestination().getId() + " by " + job.getSource(), Logger.BLUE, job.getSize(), job.getWavelengthID());
            }
            simulator.addStat(this, Stat.CLIENT_REQ_SENT);
        } else {
            simulator.addStat(this, Stat.CLIENT_NO_REQ_SENT);
            simulator.putLog(currentTime, "FAIL: " + id + " could not send " + job.getId(), Logger.RED, job.getWavelengthID(), (int) job.getSize());
        }

    }

    /**
     * Will handle the message from the ServiceNode indicating the job has been
     * scheduled. Will send out the actual job to the resource designated by the
     * ServiceNode.
     *
     * @param inPort the inPort on which the message was received
     * @param ackMsg the incoming message
     */
    protected void handleJobAckMessage(SimBaseInPort inPort, JobAckMessage ackMsg) {
        // ServiceNode has reserved resources!
        // simulator.putLog(currentTime, "<-- Job Ack received by " + id + ". (" + msg.getId() + ")", Logger.BLUE, msg.getSize(), msg.getWavelengthID());
        simulator.addStat(this, Stat.CLIENT_REQ_ACK_RECEIVED);
        if (isRequested(ackMsg.getRequestMessage())) {
            // assemble the JobInfoMessage
            JobMessage jobMsg = new JobMessage(ackMsg, new Time(currentTime.getTime()));
            jobMsg.setDestination(ackMsg.getResource());
            jobMsg.setDomainPCE(ackMsg.getDomainPCE());
            
            if (ackMsg.getResource() != null) {
                jobMsg.addHop(this);
                //Adds to jogMsg the estimated markovian costs sets by PCE.
                jobMsg.setEstimatedMarkovianCost(ackMsg.getEstimatedMarkovianCost());
                // send it out
                if (sender.send(jobMsg, currentTime, true)) {
                    if (jobMsg.getTypeOfMessage() == GridMessage.MessageType.OBSMESSAGE) {
                        simulator.putLog(currentTime, "--> Job  (" + jobMsg.getId() + ")sent into OBS network by " + id + " to "
                                + ackMsg.getResource(), Logger.BLUE, jobMsg.getSize(), jobMsg.getWavelengthID());
                    } else {
                        simulator.putLog(currentTime, "--> Job  (" + jobMsg.getId() + ")sent into OCS network by " + id + " to "
                                + ackMsg.getResource(), Logger.BLUE, jobMsg.getSize(), jobMsg.getWavelengthID());
                    }
                    simulator.addStat(this, Stat.CLIENT_JOB_SENT);

                } else {
                    simulator.putLog(currentTime, "FAIL: " + id + " could not send " + jobMsg.getId(), Logger.RED, -1, -1);
                    simulator.addStat(this, Stat.CLIENT_SENDING_FAILED);

                }
            } else {
                simulator.putLog(currentTime, id + " got a REQ-ACK message with no resource (all resources busy)>", Logger.BLUE, ackMsg.getSize(), ackMsg.getWavelengthID());
                simulator.addStat(this, Stat.CLIENT_RESOURCES_BUSY_MSG);

            }
        }
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * This methode initialises the client. This means filling in his routing
     * tables, making the generatormessage for creating the first event...
     */
    @Override
    public void init() {
        //Fills in the routingtables
        if (!inited) {
            // generate message
            GeneratorMessage message = new GeneratorMessage(getId(), currentTime);
            message.setWavelengthID(-1);
            Time time = new Time(simulator.getMasterClock().getTime());
            Double offSet = state.getJobIntervalSample();
            time.addTime(offSet);
            sendSelf(message, time);
            if (broker == null) {
                throw new NullPointerException(id + " has no broker set...");
            }
            super.init();
            return;
        } else {
            return;
        }

    }

    /**
     * Checks whether or not a given job was requested by this ClientNode
     *
     * @param msg the job to be checked for
     * @return true if the job was requested by this client; false if it wasn't,
     * or has been handled
     */
    public boolean isRequested(JobRequestMessage msg) {
        if (activeJobRequests.contains(msg)) {
            activeJobRequests.remove(msg);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Handles the results of a job.
     *
     * @param inPort The port on which the message got trough
     * @param m the Resultmessage.
     */
    protected void handleJobResultMessage(SimBaseInPort inPort, JobResultMessage m) {
        simulator.addStat(this, Stat.CLIENT_RESULTS_RECEIVED);
        simulator.putLog(currentTime, "<-- Job results " + m.getId() + " arrived at " + id + ".", Logger.BLUE, 0, 0);
    }

    /**
     * Will handle incoming GeneratorMessages, and generate a job request.
     *
     * @param inPort the inPort on which the message was received
     * @param msg the incoming message
     */
    protected void handleGeneratorMessage(SimBaseInPort inPort,
            GeneratorMessage msg) {
        if (inPort.getSource().getOwner() != this) {
            return;
        }
        // 1. Generate and send out a new job
        sendJob();

        // 2. Schedule next job using the current state's parameters
        GeneratorMessage generate = new GeneratorMessage(id, currentTime);
        generate.setWavelengthID(-1);
        Time time = new Time(simulator.getMasterClock());
        time.addTime(state.getJobIntervalSample());
        sendSelf(generate, time);
        // simulator.putLog(currentTime, "New job creation scheduled at " + time.getTime() + " in " + id + ".", Logger.BLUE, 0, 0);
    }

    /**
     * Returns the main service node for which this client is registrerd with.
     *
     * @return The main service node for which this client is registred.
     */
    public ServiceNode getServiceNode() {
        return broker;
    }

    /**
     * Sets the servicenode for this client.
     *
     * @param serviceNode The new ServiceNode for this client.
     */
    public void setServiceNode(ServiceNode serviceNode) {
        this.broker = serviceNode;
    }

    /**
     * Return the name of the main service node for which this client is
     * registred with.
     *
     * @return The name of the main service node.
     */
    public String getServiceNodeName() {
        return broker.getID();
    }

    public Sender getSender() {
        return sender;
    }
}
