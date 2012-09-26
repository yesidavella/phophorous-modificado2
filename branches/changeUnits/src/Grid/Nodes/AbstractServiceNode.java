/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.Messages.JobAckMessage;
import Grid.Interfaces.Messages.JobRequestMessage;
import Grid.Interfaces.Messages.ResourceRegistrationMessage;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ResourceSelector;
import Grid.Interfaces.ServiceNode;
import Grid.Nodes.ResourceScheduler.RoundRobinResourceSelector;
import Grid.Nodes.ResourceScheduler.StandardSelector;
import Grid.Nodes.ResourceScheduler.UniformSelector;
import Grid.OCS.OCSRoute;
import Grid.Sender.Sender;
import java.util.ArrayList;
import java.util.List;
import simbase.Exceptions.StopException;
import simbase.Port.SimBaseInPort;
import simbase.SimBaseMessage;
import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public abstract class AbstractServiceNode extends ServiceNode {

    /**
     * List of resources registered with this ServiceNode
     */
    protected List<ResourceNode> resources = new ArrayList<ResourceNode>();
    /**
     * The selector which selects which resources gets selected.
     */
    protected ResourceSelector resourceSelector;
    /**
     * The sender of this service node.
     */
     protected Sender sender;

    /**
     * Constructor. Will generate default in- and outports
     *
     * @param id
     *            the internal id of the object, used by the code
     * @param sim
     *            the {@link SimBaseSimulator} object this object is registered
     *            with
     */
    public AbstractServiceNode(String id, GridSimulator sim) {
        super(id, sim);
        resourceSelector = new RoundRobinResourceSelector(resources);
    }

    public void setResourceSelector(ResourceSelector resourceSelector) {
        this.resourceSelector = resourceSelector;
    }
    public void loadDefaultResourceSelector()
    {
          resourceSelector = new RoundRobinResourceSelector(resources);
    }
    public void loadStandarSelector()
    {
        StandardSelector standardSelector = new StandardSelector(resources, this, gridSim);
        this.resourceSelector =standardSelector;
    }


    /**
     * The receive method. Will call the appropriate handler routine as it
     * receives incoming messages.
     */
    @Override
    public void receive(SimBaseInPort inPort, SimBaseMessage msg) throws StopException {
        // incoming control message!
        if (msg instanceof JobRequestMessage) {
            handleJobRequestMessage(inPort, (JobRequestMessage) msg);
        } else if (msg instanceof ResourceRegistrationMessage) {
            handleResourceRegistrationMessage(inPort, (ResourceRegistrationMessage) msg);
        } else {
            throw new StopException(this.getId() + " received an unknown message " + msg.getId());
        }
    }

    /**
     * Will find a free resource. (Uniformally distributed).
     * @return The best resource to send the job to, null if nothing is found
     */
    protected ResourceNode findBestResource(Entity sourceNode,  double jobFlops, JobAckMessage job) 
    {
        ResourceNode resource = resourceSelector.findBestResource(sourceNode,resources, jobFlops,pce, job);
        
        
        
        if (resource == null) {
            simulator.addStat(this, Stat.SERVICENODE_NO_FREE_RESOURCE);
            return null;
        } else {
            return resource;
        }
    }

    /**
     * Registers a resource locally by adding it to the list of resources
     *
     * @param inPort
     *            the inPort on which the message was received
     * @param msg
     *            the incoming message
     */
    protected void handleResourceRegistrationMessage(SimBaseInPort inPort,
            ResourceRegistrationMessage msg) {
        // add ResourceInfo from the message to the resource list
        resources.add(msg.getResource());
        simulator.putLog(currentTime, "<-- Resource reg for " + msg.getResource().getID() + " received by " + id + ".", Logger.BROWN, 0, 0);
        simulator.addStat(this, Stat.SERVICENODE_REGISTRATION_RECEIVED);
    }

    /**
     * Return the id of this Service node.
     */
    public String getID() {
        return this.getId();
    }

    public List<ResourceNode> getResources() {
        return resources;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void handleJobRequestMessage(SimBaseInPort inPort, JobRequestMessage msg) {
        //simulator.putLog(currentTime, "<-- Job Req recieved by " + this.getID() + " from " + msg.getSource().getId() +
        //        " . (" + msg.getId() + ")", Logger.BROWN, msg.getSize(), msg.getWavelengthID());
        simulator.addStat(this, Stat.SERVICENODE_REQ_RECIEVED);
        if (!resources.isEmpty()) {
            JobAckMessage ackMsg = new JobAckMessage(msg);
        
            ackMsg.setDestination(msg.getSource());
            ackMsg.setSource(this);            
            ackMsg.setSize(msg.getSize());
            ackMsg.setResource(findBestResource(  msg.getSource(),  msg.getFlops(),ackMsg));
            if (msg.getWavelengthID() == -1) {
                ackMsg.setWavelengthID(-1);
            }
            ackMsg.addHop(this);
            if (sender.send(ackMsg, currentTime,true)) {
               // simulator.putLog(currentTime, "--t> Job Ack sent by " + id + ". (" + ackMsg.getId() + ")", Logger.BROWN, msg.getSize(), msg.getWavelengthID());
                simulator.addStat(this, Stat.SERVICENODE_REQ_ACK_SENT);
            } else {
                simulator.putLog(currentTime, "FAIL " + inPort.getOwner().getId() + "could not send " + msg.getId(), Logger.RED, msg.getSize(), msg.getWavelengthID());
                simulator.addStat(this, Stat.SERVICENODE_SENDING_FAILED);
            }
        }
    }

    @Override
    public void endSimulation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void requestOCSCircuit(OCSRoute ocsRoute,boolean permanent,Time time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
