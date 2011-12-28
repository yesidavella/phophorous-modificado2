/**
 * The ResourceNode class represents a resource node in the grid network both
 * computational and data/storage resources are represented by this class.
 * ResourceNode objects will receive jobs from the clients, process them and
 * return the results. Like the ClientNode class, the
 * ResourceNode contains inPort and outPort fields, and a receive method which,
 * depending on the type of the incoming message, will call one of the handler
 * methods.
 * 
 * @version 2.1
 */
package Grid.Interfaces;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.JobCompletedMessage;
import Grid.Jobs.QueuedJob;
import Grid.Utilities.SampleAverage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import simbase.Stats.Logger;
import simbase.Time;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public abstract class ResourceNode extends Entity {

    /**
     * The list of service nodes for which this resource sends updates
     */
    protected List<ServiceNode> serviceNodes = new ArrayList();
    /**
     * The set containing all the cpus of this resource.
     */
    protected List<CPU> cpuSet = new ArrayList();
    /**
     * The storage capacity
     */
    protected long storageCount = 0;
    /**
     * The number of CPUs
     */
    protected int cpuCount = 0;

    
    /**
     * The number of free places in the queues of the cpu's.
     */
    protected long cpuFreeCount = 0;
    /**
     * The maximum queue size
     */
    protected int maxQueueSize = 0;
    /**
     * PARAMETERS FOR THE QUEUE
     */
    /**
     * The total system time of all jobs = queutime + service time
     */
    private SampleAverage systemTime = new SampleAverage();
    private SampleAverage waitingTime = new SampleAverage();
    private SampleAverage systemPopulation = new SampleAverage();
    private SampleAverage waitTime = new SampleAverage();
    private SampleAverage queuePopulation = new SampleAverage();

    /**
     * constructor
     * @param id The id of this resource
     * @param gridSim The simulator to which this belongs.
     */
    public ResourceNode(String id, GridSimulator gridSim) {
        super(id, gridSim);

    }

    /**
     * Returns the list witch the cpu's.
     * @return
     */
    public List getCpuSet() {
        return cpuSet;
    }

    /**
     * Return the number of cpu's of this resource.
     * @return the number of cpu's of this resource.
     */
    public long getCpuCount() {
        return cpuCount;
    }

    /**
     * Sets the amount of storage place.
     * @param storageCount the new amount of storage place
     */
    public void setStorageCount(long storageCount) {
        this.storageCount = storageCount;
    }

    /**
     * Return  how many places there are still in the queue, counting free cpu's with it.
     * @return Place over in the resource.
     */
    public abstract int getQueuingSpace();

    /**
     * Return the storage place.
     * @return
     */
    public long getStorageCount() {
        return storageCount;
    }

    /**
     * Return the nr free cpu's
     */
    public abstract int getNrOfFreeCpus();

    /**
     * This executes a job. It calculates all timings and send the job done message.
     * @param job The job to exectue.
     * @param cpu The cpu which will execute the job.
     */
    protected void executeJob(QueuedJob job, CPU cpu, Time submitTime) {
        cpu.addJob(job);
        job.setCpu(cpu);
        job.setStartTime(new Time(submitTime.getTime()));

        double executionTime = job.getMsg().getFlops() / cpu.getCpuCapacity();
        simulator.addStat(this, Stat.RESOURCE_BUSY_TIME,
                executionTime);

        Time endTime = new Time(submitTime.getTime() + executionTime);
        job.setEndTime(endTime);
        //endtime already been added by reservetimeslot so no setting endtime needed
        JobCompletedMessage donemsg = new JobCompletedMessage(this, job.getMsg().getId() +
                "-done", job.getMsg(), endTime);
        donemsg.setSize(job.getMsg().getSize());
        donemsg.setQueuedJob(job);
        simulator.putLog(currentTime, "Job " + job.getMsg().getId() +
                " scheduled (start: " + job.getStartTime() +
                ", completion: " + endTime + " by " + cpu.getId() + ")." + "(" + this.getNrOfJobsInQueue() + "/" + this.maxQueueSize + ")", Logger.GREEN, 0, 0);
        sendSelf(donemsg, endTime);
    }

    /**
     * Sets the total number of processing units in this resource node
     * 
     * @param cpuCount
     *            the total number of processing units in this resource node
     * @param cpuCapacity 
     *            the capacity of the cpus.
     */
    public abstract void setCpuCount(int cpuCount, double cpuCapacity);

    /**
     * Returns the capacity per processing unit. A capacity of x means each cpu
     * in the resource will process x units of the
     * {@link JobMessage}#flops processing requirement variable} in one unit
     * of Time
     * 
     * @param cpuId The id of the cpu
     * @return the capacity per processing unit
     */
    public abstract double getCpuCapacity(String cpuId);

    /**
     * Sets the capacity per processing unit. A capacity of x means each cpu in
     * the resource will process x units of the
     * {@link JobMessage} #flops processing requirement variable} in one unit of Time
     * 
     * @param cpuCapacity
     *            the capacity per processing unit.
     * @param cpuId 
     *            the id of the cpu for which the capacity has to be set.  
     */
    public abstract void setCpuCapacity(double cpuCapacity, String cpuId);

    /**
     * Returns the number of jobs in the queue of the resource node.
     * 
     * @return the queue size of the resource node.
     */
    public abstract int getNrOfJobsInQueue();

    /**
     * Sets the queue size of the resource node.
     * 
     * @param maxQueueSize
     *            the queue size of the resource node.
     * @param cpuId The id of the cpu for which the queusize has to be set.
     * 
     * @return true if was succesful, false otherwise
     */
    public abstract boolean setQueueSize(int queueSize);

    /**
     * Returns an arraylist with @link{ServiceNode} objects this resource is 
     * registered with
     * 
     * @return the ServiceNode object this resource is registered with
     */
    public List<ServiceNode> getServiceNodes() {
        return serviceNodes;
    }

    /**
     * Return the id.
     */
    public String getID() {
        return this.getId();
    }

    /**
     * sets the cpu capacity for all the cpu's.
     */
    public void setCpuCapacity(double cpuCapcacity) {
        Iterator<CPU> it = cpuSet.iterator();
        while (it.hasNext()) {
            it.next().setCpuCapacity(cpuCapcacity);
        }
    }
    

    /**
     * Adds a service node to the list of service nodes which serve this resource.
     */
    public abstract void addServiceNode(ServiceNode node);

    /**
     * Adds a service node to the list of service nodes on a specified time.
     */
    public abstract void addServiceNode(ServiceNode node, Time time);

    public SampleAverage getQueuePopulation() {
        return queuePopulation;
    }

    public SampleAverage getSystemPopulation() {
        return systemPopulation;
    }

    public SampleAverage getSystemTime() {
        return systemTime;
    }

    public SampleAverage getWaitTime() {
        return waitTime;
    }

    public SampleAverage getWaitingTime() {
        return waitingTime;
    }

    public void handleQueuedJob() {
        queuePopulation.addSample(this.getNrOfJobsInQueue());
    }

    public void handleInOutComing() {
        double nrOfBusyCpus = 0;
        Iterator<CPU> it = cpuSet.iterator();
        while (it.hasNext()) {
            CPU cpu = it.next();
            if (cpu.isBusy()) {
                nrOfBusyCpus++;
            }
        }
        systemPopulation.addSample(this.getNrOfJobsInQueue() + nrOfBusyCpus);
    }

    public void handleSystemTime(QueuedJob job) {
        double timeSample = job.getEndTime().getTime() - job.getQueueTime().getTime();
        systemTime.addSample(timeSample);
    }

    @Override
    public boolean supportSwitching() {
        return false;
    }

    

    public long getCpuFreeCount() {
        return cpuFreeCount;
    }

   

    public int getMaxQueueSize() {
        return maxQueueSize;
    }
    
}
