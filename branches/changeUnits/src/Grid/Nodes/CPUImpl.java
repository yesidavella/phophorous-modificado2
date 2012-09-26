/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes;

import Grid.GridSimulator;
import Grid.Interfaces.CPU;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Interfaces.ResourceNode;
import Grid.Jobs.QueuedJob;
import java.io.Serializable;


import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class CPUImpl implements CPU, Serializable {

    /**
     * The jobs it is handling at the moment.
     */
    protected QueuedJob executedJob;
    /**
     * Flag which tells if this CPU is executing.
     */
    protected boolean isExecuting;
    /**
     * The owner of the CPU.
     */
    protected ResourceNode owner;
    /**
     * The simbase simulator
     */
    protected GridSimulator simulator;
    /**
     * The number of jobs it can hold in the queue.
     */
    protected int maxQueueSize;
    /**
     * The id of the CPU
     */
    protected String id;
    /**
     * Class variable which states the number of CPUS which already have been made by the simulator.
     */
    private static int cpuNR = 0;
    /**
     * The number of jobs served by this CPU.
     */
    private int nrOfservedJobs = 0;
    /**
     * The CPU capacity
     */
    protected double cpuCapacity;

    /**
     * Constructor. Creates a CPU for a given resource node.
     *
     * @param owner the resource node the CPU belongs to
     * @param simulator The simulator
     * @param maxQueueSize The maximum queuesize.
     */
    public CPUImpl(ResourceNode owner, GridSimulator simulator,
            int maxQueueSize) {
        super();
        this.owner = owner;
        this.simulator = simulator;
        this.maxQueueSize = maxQueueSize;
        StringBuffer buffer = new StringBuffer(owner.getID());
        buffer.append(" ");
        buffer.append(cpuNR);
        cpuNR++;
        id = buffer.toString();
    }

    /**
     * Constructor. Creates CPU for a given resource node.
     *
     * @param owner The owner of this CPU.
     * @param simulator The simulator.
     */
    public CPUImpl(ResourceNode owner, GridSimulator simulator) {
        super();
        this.owner = owner;
        this.simulator = simulator;
        StringBuffer buffer = new StringBuffer(owner.getID());
        buffer.append(" ");
        buffer.append(cpuNR);
        cpuNR++;
        id = buffer.toString();
    }

    public void setIsExecuting(boolean isExecuting) {
        this.isExecuting = isExecuting;
    }

    public QueuedJob getExecutingJob() {
        return executedJob;
    }


    public int getNrOfservedJobs() {
        return nrOfservedJobs;
    }

    public ResourceNode getOwner() {
        return owner;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public boolean isBusy() {
        return isExecuting;
    }

    public double getCpuCapacity() {
        return cpuCapacity;
    }

    public boolean contains(JobMessage msg) {
        return executedJob.getMsg().equals(msg);
    }

    /**
     * The toString method, returns a string representation of this cpu.
     * @return A string representation of the cpu.
     */
    @Override
    public String toString() {
        return id;

    }

    public QueuedJob removeJob() {
        isExecuting = false;
        return executedJob;
    }

    public Time returnNewTimeSlot() {
        if (executedJob == null) {
            return null;
        } else {
            return executedJob.getEndTime();
        }
    }

    public void addJob(QueuedJob job) {
        executedJob = job;
        isExecuting=true;
    }

    public String getId() {
        return id;
    }

    public void setCpuCapacity(double cpuCapacity) {
        this.cpuCapacity = cpuCapacity;
    }

    /**
     * Compares two Cpu's with each other.
     */
    public int compareTo(Object cpuInfo) {
        CPUImpl c = (CPUImpl) cpuInfo;
        return this.executedJob.getEndTime().compareTo(c.getExecutingJob().getEndTime());
    }
}
