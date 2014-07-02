/*
 * This class represents a CPU of a @link{ResourceNode}. 
 */
package Grid.Interfaces;

import Grid.Interfaces.Messages.JobMessage;
import Grid.Jobs.QueuedJob;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public interface CPU extends Comparable {

    /**
     * Return the number of jobs this cpu has already processed.
     *
     * @return The number of jobs which this cpu has already processed.
     */
    public int getNrOfservedJobs();

    /**
     * Return the id of this CPU.
     * @return The id of this CPU.
     */
    public String getId();

    /**
     * Checks if this cpu can handle a new job.
     *
     * @return True if it is full, else false.
     */
    public boolean isBusy();

    /**
     * Return the time the last job of the queue has ended and is as such
     * also the time a new job enters the cpu. This is thus a new TimeSlot.
     * We expect that every CPU has at least one buffercapacity.
     *
     * @return A timeslot for a new job.
     */
    public Time returnNewTimeSlot();

    /**
     * Removes the job which is executing now.
     *
     */
    public QueuedJob removeJob();

    /**
     * Checks whether this cpu contains a specified Job.
     *
     * @param msg The JobInfoMessage containing the job.
     */
    public boolean contains(JobMessage msg);

    /**
     * Adds a job to his queue.
     */
    public void addJob(QueuedJob job);

    /**
     * Return the capacity of this CPU.
     * @return The capacity of this cpu in flops/s = Hz.
     */
    public double getCpuCapacity();

    /**
     * Sets the cpucapacity.
     **/
    public abstract void setCpuCapacity(double cpuCapacity);

    /**
     * Return the job it is executing now
     * @return The job which is executing.
     */
    public QueuedJob getExecutingJob();



    /**
     * Marks that this CPU is working and is not idle.
     * @param isExecuting 
     */
    public void setIsExecuting(boolean isExecuting);
}
