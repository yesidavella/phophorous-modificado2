/**
 * The QueuedJob class contains the job info and its timestamps (addition to
 * queue, processing start time, processing end time) of job at the resource
 * node.
 * 
 * @version 2.0
 */
package Grid.Jobs;

import Grid.Interfaces.CPU;
import Grid.Interfaces.Messages.JobMessage;
import java.io.Serializable;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class QueuedJob  implements Comparable, Serializable{

    /**
     * The time the job was added to the queue
     */
    private Time queueTime = new Time();
    /**
     * The time the job actually started
     */
    private Time startTime = new Time();
    /**
     * The time the job finished
     */
    private Time endTime = new Time();
    /**
     * The job description
     */
    private JobMessage msg = null;
    /**
     * The CPU this job has been added to.
     */
    private CPU cpu = null;

    /**
     * Constructor
     *
     * @param msg
     *            the JobInfoMessage from which this QueuedJob will be created.
     *            Contains all job info
     */
    public QueuedJob(JobMessage msg) {
        this.msg = msg;
    }
    
    

    /**
     * Returns the time this job was added to the job queue
     * 
     * @return the time this job was added to the job queue
     */
    public Time getQueueTime() {
        return queueTime;
    }

    /**
     * Sets the time the job got added to a queue.
     * @param queueTime The time of adding to the queue.
     */
    public void setQueueTime(Time queueTime) {
        this.queueTime = queueTime;
    }

    /**
     * Sets the time at which execution of the job will start.
     * 
     * @param startTime
     *            the time at which execution of the job will start
     */
    public void setStartTime(Time startTime) {
        this.startTime= startTime;
    }

    /**
     * Sets the time at which the execution of the job will be complete.
     * 
     * @param endTime
     *            the time at which the execution of the job will be complete
     */
    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the time at which execution of the job will start.
     * 
     * @return the time at which execution of the job will start
     */
    public Time getStartTime() {
        return startTime;
    }

    /**
     * Returns the time at which the execution of the job will be complete.
     * 
     * @return the time at which the execution of the job will be complete
     */
    public Time getEndTime() {
        return endTime;
    }

    /**
     * Return the jobmessage of this queuedjob. (The acutal job thus)
     * @return The job.
     */
    public JobMessage getMsg() {
        return msg;
    }

    /**
     * Sets the message of this queuedjob.
     * @param msg
     */
    public void setMsg(JobMessage msg) {
        this.msg = msg;
    }

    /**
     * Sets the cpu this queuedjob is added to.
     * @param cpu The cpu to which this job should be queued on.
     */
    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }

    /**
     * Return the cpu this queuedjob is added to.
     * @return The cpu where this job is added to;
     */
    public CPU getCpu() {
        return cpu;
    }

    public int compareTo(Object o) {
        QueuedJob job = (QueuedJob)o;
        return this.getQueueTime().compareTo(job.getQueueTime());
    }
    
    
    
    
}
