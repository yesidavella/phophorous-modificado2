package Grid.Nodes;

import Distributions.DiscreteDistribution;
import Grid.GridSimulator;
import Grid.Interfaces.CPU;
import Grid.Interfaces.CpuSelector;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.JobCompletedMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.Messages.JobResultMessage;
import Grid.Interfaces.Messages.ResourceRegistrationMessage;
import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import Grid.Jobs.QueuedJob;
import Grid.Nodes.Queueing.TimeComparator;
import Grid.Port.GridOutPort;
import Grid.Sender.Sender;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import simbase.Port.SimBaseInPort;
import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public abstract class AbstractResourceNode extends ResourceNode {

    private ArrayList<Double> valuesRelativeCPU = new ArrayList<Double>();
    private ArrayList<Double> valuesRelativeBuffer = new ArrayList<Double>();
    private double relativeBusyCPU = 0.0000000000D;
    private double averageLastCPU = 0.0000000000D;
    private double averageLastBuffer = 0.0000000000D;
    /**
     * The large queue of this resource.
     */
    protected Queue queue;
    /**
     * The selector which selects the cpu if needed.
     */
    protected CpuSelector selector;
    /**
     * The sender of this resource node.
     */
    protected Sender sender;

    public Sender getSender() {
        return sender;
    }

    /**
     * Constructor
     *
     * @param id The id of this resource serviceNode.
     * @param gridSim The simulator to which it belongs
     */
    public AbstractResourceNode(String id, GridSimulator gridSim) {
        super(id, gridSim);
      
               
    }

    /**
     * Handles incoming job messages.
     *
     * @param inPort The inport on which the job message was received.
     * @param message The jobmessage itself (the actual job).
     */
    public double getAverageCPU() {

        synchronized (valuesRelativeCPU) {

            double addAverage = 0D;
            double average = 0D;
            for (Double value : valuesRelativeCPU) {
                addAverage += value.doubleValue();
            }
            if (valuesRelativeCPU.size() > 0) {
                average = addAverage / valuesRelativeCPU.size();
                valuesRelativeCPU.clear();
            } else {
                return averageLastCPU;
            }
            averageLastCPU = average;
            return average;
        }
    }

    public double getAverageBuffer() {

        synchronized (valuesRelativeBuffer) {

            double addAverage = 0D;
            double average = 0D;
            for (Double value : valuesRelativeBuffer) {
                addAverage += value.doubleValue();
            }
            if (valuesRelativeBuffer.size() > 0) {
                average = addAverage / valuesRelativeBuffer.size();
                valuesRelativeBuffer.clear();
            } else {
                return averageLastBuffer;
            }
            averageLastBuffer = average;
            return average;
        }
    }

    protected void handleJobMessage(SimBaseInPort inPort, JobMessage message) {


        handleInOutComing();
        GridOutPort outPort = (GridOutPort) inPort.getSource();

        Time ETA = new Time(message.getSize() / outPort.getSwitchingSpeed());
        ETA.addTime(currentTime);
        //check if there are cpus free which can take this job.
        if (message.getTypeOfMessage() == GridMessage.MessageType.OBSMESSAGE) {
            simulator.putLog(currentTime, "<-- OBS First part of Job inf arrived at " + id
                    + " for job: " + message.getId() + ". ETA of full job: " + ETA, Logger.GREEN, message.getSize(), message.getWavelengthID());
        } else {
            simulator.putLog(currentTime, "<-- OCS Job inf arrived at " + id
                    + " for job: " + message.getId() + ". ETA of full job: " + ETA, Logger.GREEN, message.getSize(), message.getWavelengthID());
        }
        simulator.addStat(this, Stat.RESOURCE_JOB_RECEIVED);

        CPU cpu = selector.getCPU(cpuSet);
        QueuedJob job = new QueuedJob(message);
        job.setQueueTime(new Time(currentTime.getTime()));

        if (cpu != null) {
            //a cpu is found to take the job            
            executeJob(job, cpu, ETA);
        } else {
            //no free cpu is found
            //check wheter this queue is full
            if (maxQueueSize > 0 && queue.size() < maxQueueSize) {
                //Stil some place free in the queue
                handleQueuedJob();
                if (queue.add(job)) {
                    simulator.putLog(currentTime, job.getMsg().getId() + " has been queued in the "
                            + "resource : " + this.getID() + "  " + queue.size() + "/" + maxQueueSize, Logger.GREEN, message.getSize(), message.getWavelengthID());
                } else {
                    simulator.putLog(currentTime, "FAIL: " + job.getMsg().getId() + " " + job.getQueueTime() + " has not been queued in the "
                            + "resource : " + this.getID() + "  " + queue.size() + "/" + maxQueueSize, Logger.RED, message.getSize(), message.getWavelengthID());
                }
            } else {
                // No place in the queue and no free CPU
                simulator.addStat(this, Stat.RESOURCE_FAIL_NO_FREE_PLACE);
                simulator.putLog(currentTime, "FAIL : No free CPU/No queue space for : " + message.getId(), Logger.RED, message.getSize(), message.getWavelengthID());

            }
        }

        double queueSize = getQueue().size();
        synchronized (valuesRelativeBuffer) {
            valuesRelativeBuffer.add(queueSize);
        }
        synchronized (valuesRelativeCPU) {
            double totalCPU = cpuSet.size();
            double countBusyCPU = 0;

            for (CPU cpu1 : cpuSet) {
                if (cpu1.isBusy()) {
                    countBusyCPU++;
                }
            }
            relativeBusyCPU = countBusyCPU / totalCPU;
            valuesRelativeCPU.add(relativeBusyCPU);
        }
    }
    
     
       
    /**
     * Handling method for when a job is completed.
     *
     * @param msg The jobcompletedmessage.
     */
    protected void handleJobCompletedMessage(JobCompletedMessage msg) {
        handleSystemTime(msg.getQueuedJob());
        handleQueuedJob();

        //Remove queuedjob from queue
        msg.getQueuedJob().getCpu().removeJob();

        JobResultMessage jobResultMsg = new JobResultMessage(msg, currentTime);
        jobResultMsg.setDomainPCE(msg.getJob().getDomainPCE());
        jobResultMsg.setSize(msg.getJob().getResultSize());

        jobResultMsg.addHop(this);
        if (sender.send(jobResultMsg, currentTime, true)) {
            if (jobResultMsg.getTypeOfMessage() == GridMessage.MessageType.OBSMESSAGE) {
                simulator.putLog(currentTime,
                        "--> OBS Execution results sent back to client by " + id
                        + " for job: " + msg.getJob().getId() + ".", Logger.GREEN, jobResultMsg.getSize(), jobResultMsg.getWavelengthID());
            } else {
                simulator.putLog(currentTime,
                        "--> OCS Execution results sent back to client by " + id
                        + " for job: " + msg.getJob().getId() + ".", Logger.GREEN, jobResultMsg.getSize(), jobResultMsg.getWavelengthID());
            }
            simulator.addStat(this, Stat.RESOURCE_RESULTS_SENT);
        } else {
            simulator.addStat(this,
                    Stat.RESOURCE_SENDING_FAILED);
        }

        //check if there is a job in the queue which can be executed
        if (maxQueueSize > 0 && queue.size() > 0) {
            CPU cpu = msg.getQueuedJob().getCpu();
            QueuedJob job = (QueuedJob) queue.poll();
            simulator.putLog(currentTime, job.getMsg().getId()
                    + " got out of the queue and is being sheduled for execution. queue: "
                    + queue.size() + "/" + maxQueueSize, Logger.YELLOW, msg.getSize(), msg.getWavelengthID());
            executeJob(job, cpu, currentTime);

        }
    }

    /**
     * Adds a service node to this resource at the current time.
     *
     * @param node
     */
    @Override
    public void addServiceNode(ServiceNode node) {
        addServiceNode(node, currentTime);
    }

    /**
     * Adds a service serviceNode to this resource, with a specified time.
     *
     * @param serviceNode The service node to add.
     * @param time The time this
     */
    @Override
    public void addServiceNode(ServiceNode serviceNode, Time time) {
        ResourceRegistrationMessage reg = new ResourceRegistrationMessage(time, serviceNode,
                this);
        reg.setDestination(serviceNode);
        reg.addHop(this);
        if (sendNow(serviceNode, reg, time)) {
            serviceNodes.add(serviceNode);
            simulator.putLog(currentTime,
                    "Service node registration send by " + id
                    + " to  " + serviceNode + ".", Logger.GREEN, reg.getSize(), reg.getWavelengthID());
        } else {
            simulator.putLog(currentTime,
                    "Sending Failed" + id
                    + " to  " + serviceNode + ".", Logger.RED, reg.getSize(), reg.getWavelengthID());
        }

    }

    /**
     * Returns the number of free places for this resource. This number is the
     * numbfer of free cpu's plus the number of places available in the queue.
     *
     * @return the number of jobs this resource still can accept.
     */
    @Override
    public int getQueuingSpace() {
        if (maxQueueSize <= 0) {
            return getNrOfFreeCpus();

        } else {
            int queuePlace = maxQueueSize - queue.size();
            return getNrOfFreeCpus() + queuePlace;
        }
    }

    @Override
    public String getID() {
        return this.getId();
    }

    /**
     * Return the capacity of a given cpu.
     *
     * @param cpuId The id of the cpu.
     * @return It's capacity.
     */
    @Override
    public double getCpuCapacity(String cpuId) {
        Iterator<CPU> it = cpuSet.iterator();
        while (it.hasNext()) {
            CPU cpu = it.next();
            if (cpu.getId().equals(cpuId)) {
                return cpu.getCpuCapacity();
            }
        }
        return -1;
    }

    @Override
    public long getCpuCount() {
        return this.cpuCount;
    }

    /**
     * Return the number of jobs in the queue.
     *
     * @return the number of jobs waiting in the queue.
     */
    @Override
    public int getNrOfJobsInQueue() {
        if (maxQueueSize <= 0) {
            return 0;
        } else {
            return this.queue.size();
        }
    }

    /**
     * Return the number of free cpu's.
     *
     * @return
     */
    @Override
    public int getNrOfFreeCpus() {
        Iterator<CPU> it = cpuSet.iterator();
        int nrOfFreeCPUS = 0;
        while (it.hasNext()) {
            CPU cpu = it.next();
            if (!cpu.isBusy()) {
                nrOfFreeCPUS++;
            }
        }
        return nrOfFreeCPUS;
    }

    /**
     * Sets the cpu capacity of a cpu.
     *
     * @param cpuCapacity The new capacity
     * @param cpuId The id of the cpu which has to be updated.
     */
    @Override
    public void setCpuCapacity(double cpuCapacity, String cpuId) {
        Iterator<CPU> it = cpuSet.iterator();
        while (it.hasNext()) {
            CPU cpu = it.next();
            if (cpu.getId().equals(cpuId)) {
                cpu.setCpuCapacity(cpuCapacity);
            }
        }
    }

    /**
     * Sets the number of cpu's for this resource node.
     *
     * @param cpuCount The number of cpu's.
     * @param cpuCapacity The capacity for these cpus's.
     */
    @Override
    public void setCpuCount(int cpuCount, double cpuCapacity) {
        this.cpuCount = cpuCount;
        cpuSet.clear();
        for (int i = 0; i < cpuCount; i++) {
            CPU running = new CPUImpl(this, (GridSimulator) simulator, 1);
            running.setCpuCapacity(cpuCapacity);
            cpuSet.add(running);
        }
    }

    @Override
    public boolean setQueueSize(int queueSize) {
        this.maxQueueSize = queueSize;
        if (queueSize <= 0) {
            queue = null;
        } else {
            queue = new PriorityQueue(maxQueueSize, new TimeComparator());
        }
        return true;
    }

    public Queue getQueue() {
        return queue;
    }
}
